/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ktorm.ksp.ext.batch.tests

import com.tschuchort.compiletesting.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.ktorm.database.Database
import org.ktorm.database.use
import org.ktorm.ksp.compiler.KtormProcessorProvider
import org.ktorm.logging.ConsoleLogger
import org.ktorm.logging.LogLevel
import org.ktorm.schema.BaseTable
import org.ktorm.schema.Table
import java.io.File
import kotlin.reflect.full.functions

public abstract class BaseTest {
    @Rule
    @JvmField
    public val temporaryFolder: TemporaryFolder = TemporaryFolder()

    public abstract val database: Database


    protected open fun createCompiler(vararg sourceFiles: SourceFile): KotlinCompilation {
        return KotlinCompilation().apply {
            workingDir = temporaryFolder.root
            sources = sourceFiles.toList()
            inheritClassPath = true
            messageOutputStream = System.out
        }
    }

    protected fun KotlinCompilation.Result.getBaseTable(className: String): BaseTable<*> {
        val cls = classLoader.loadClass("$className\$Companion")
        return cls.kotlin.objectInstance as BaseTable<*>
    }

    protected fun KotlinCompilation.Result.getTable(className: String): Table<*> {
        val cls = classLoader.loadClass("$className\$Companion")
        return cls.kotlin.objectInstance as Table<*>
    }

    private fun Any.reflectionInvoke(methodName: String, vararg args: Any?): Any? {
        return this::class.functions.first { it.name == methodName }.call(this, *args)
    }

    protected fun KotlinCompilation.Result.invokeBridge(methodName: String, vararg args: Any?): Any? {
        val bridgeClass = this.classLoader.loadClass("TestBridge")
        val bridge = bridgeClass.kotlin.objectInstance!!
        return bridge.reflectionInvoke(methodName, *args)
    }

    protected open fun createKspCompiler(vararg sourceFiles: SourceFile, useKsp: Boolean = true): KotlinCompilation {
        return KotlinCompilation().apply {
            workingDir = temporaryFolder.root
            sources = sourceFiles.toList()
            if (useKsp) {
                symbolProcessorProviders = listOf(KtormProcessorProvider())
            }
            inheritClassPath = true
            messageOutputStream = System.out
            kspIncremental = true
        }
    }

    /**
     * The first compilation uses ksp to generate code.
     * The second compilation verifies the code generated by ksp.
     */
    protected fun twiceCompile(
        vararg sourceFiles: SourceFile,
        sourceFileBlock: (String) -> Unit = {},
    ): Pair<KotlinCompilation.Result, KotlinCompilation.Result> {
        val compiler1 = createKspCompiler(*sourceFiles)
        val result1 = compiler1.compile()
        val result2 =
            createKspCompiler(
                *(compiler1.kspGeneratedSourceFiles + sourceFiles).toTypedArray(),
                useKsp = false
            ).compile()
        compiler1.kspGeneratedFiles.forEach { sourceFileBlock(it.readText()) }
        return result1 to result2
    }

    protected fun compile(
        vararg sourceFiles: SourceFile,
        printKspGenerateFile: Boolean = false
    ): KotlinCompilation.Result {
        val compilation = createKspCompiler(*sourceFiles)
        val result = compilation.compile()
        if (printKspGenerateFile) {
            compilation.kspSourcesDir.walkTopDown()
                .filter { it.extension == "kt" }
                .forEach { println(it.readText()) }
        }
        return result
    }

    protected val KotlinCompilation.kspGeneratedSourceFiles: List<SourceFile>
        get() = kspSourcesDir.resolve("kotlin")
            .walk()
            .filter { it.isFile }
            .map { SourceFile.fromPath(it.absoluteFile) }
            .toList()

    protected val KotlinCompilation.kspGeneratedFiles: List<File>
        get() = kspSourcesDir.resolve("kotlin")
            .walk()
            .filter { it.isFile }
            .toList()

    @Before
    public fun init() {
        execSqlScript("init-data.sql")
    }

    @After
    public fun destroy() {
        execSqlScript("drop-data.sql")
    }

    private fun execSqlScript(filename: String) {
        database.useConnection { conn ->
            conn.createStatement().use { statement ->
                javaClass.classLoader
                    ?.getResourceAsStream(filename)
                    ?.bufferedReader()
                    ?.use { reader ->
                        for (sql in reader.readText().split(';')) {
                            if (sql.any { it.isLetterOrDigit() }) {
                                statement.executeUpdate(sql)
                            }
                        }
                    }
            }
        }
    }
}