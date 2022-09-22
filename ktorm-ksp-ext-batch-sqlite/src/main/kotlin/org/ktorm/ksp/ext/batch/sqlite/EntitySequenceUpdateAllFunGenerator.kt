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
package org.ktorm.ksp.ext.batch.sqlite

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.ktorm.entity.EntitySequence
import org.ktorm.ksp.codegen.TableGenerateContext
import org.ktorm.ksp.codegen.TopLevelFunctionGenerator
import org.ktorm.ksp.codegen.definition.ColumnDefinition
import org.ktorm.ksp.codegen.definition.TableDefinition

public class EntitySequenceUpdateAllFunGenerator : TopLevelFunctionGenerator {

    private val batchUpdateFun = MemberName("org.ktorm.dsl", "batchUpdate", true)
    private val eqFun: MemberName = MemberName("org.ktorm.dsl", "eq", true)
    private val andFun: MemberName = MemberName("org.ktorm.dsl", "and", true)

    override fun generate(context: TableGenerateContext): List<FunSpec> {
        val (table, _, logger, _) = context
        val primaryKeys = table.columns.filter { it.isPrimaryKey }
        if (primaryKeys.isEmpty()) {
            logger.info(
                "skip the entity sequence updateAll method of table ${table.entityClassName} " +
                        "because it does not have a primary key column"
            )
            return emptyList()
        }
        val funSpec = FunSpec.builder("updateAll")
            .receiver(EntitySequence::class.asClassName().parameterizedBy(table.entityClassName, table.tableClassName))
            .addParameter("entities", Iterable::class.asClassName().parameterizedBy(table.entityClassName))
            .returns(typeNameOf<IntArray>())
            .addKdoc("""
                Batch update given entities based on primary key 
                @return the effected row counts for each sub-operation.
            """.trimIndent())
            .addCode(CodeFactory.buildCheckDmlCode())
            .addCode(buildBatchUpdateCode(table, primaryKeys))
            .build()
        return listOf(funSpec)
    }

    private fun buildBatchUpdateCode(table: TableDefinition, primaryKeys: List<ColumnDefinition>) = buildCodeBlock {
        beginControlFlow("return·database.%M(%T)", batchUpdateFun, table.tableClassName)
        beginControlFlow("for (entity in entities)")
        beginControlFlow("item")
        for (column in table.columns) {
            if (!column.isPrimaryKey) {
                val valueCode = CodeFactory.buildColumnValueCode(column)
                addStatement("set(it.%L,·%L)", column.tablePropertyName.simpleName, valueCode)
            }
        }
        beginControlFlow("where")
        primaryKeys.forEachIndexed { index, column ->
            if (index == 0) {
                val conditionTemperate = if (primaryKeys.size == 1) {
                    "it.%L·%M·entity.%L%L"
                } else {
                    "(it.%L·%M·entity.%L%L)"
                }
                addStatement(
                    conditionTemperate,
                    column.tablePropertyName.simpleName,
                    eqFun,
                    column.entityPropertyName.simpleName,
                    if (column.isNullable) "!!" else ""
                )
            } else {
                addStatement(
                    ".%M(it.%L·%M·entity.%L%L)",
                    andFun,
                    column.tablePropertyName.simpleName,
                    eqFun,
                    column.entityPropertyName.simpleName,
                    if (column.isNullable) "!!" else ""
                )
            }
        }
        endControlFlow()
        endControlFlow()
        endControlFlow()
        endControlFlow()
    }

}
