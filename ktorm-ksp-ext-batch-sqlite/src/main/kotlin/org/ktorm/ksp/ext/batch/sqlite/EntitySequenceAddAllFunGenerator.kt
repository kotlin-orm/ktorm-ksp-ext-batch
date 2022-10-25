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
import org.ktorm.entity.EntityExtensionsApi
import org.ktorm.entity.EntitySequence
import org.ktorm.ksp.codegen.TableGenerateContext
import org.ktorm.ksp.codegen.TopLevelFunctionGenerator
import org.ktorm.ksp.codegen.definition.KtormEntityType
import org.ktorm.ksp.codegen.definition.TableDefinition

public class EntitySequenceAddAllFunGenerator : TopLevelFunctionGenerator {

    private val bulkInsertFun = MemberName("org.ktorm.support.sqlite", "bulkInsert", true)

    override fun generate(context: TableGenerateContext): List<FunSpec> {
        val (table, _, _, _) = context
        val funSpec = FunSpec.builder("addAll")
            .receiver(EntitySequence::class.asClassName().parameterizedBy(table.entityClassName, table.tableClassName))
            .addParameter("entities", Iterable::class.asClassName().parameterizedBy(table.entityClassName))
            .returns(typeNameOf<Int>())
            .addKdoc("""
                Bulk insert the given entities into this sequence.             
                @return the affected record number. 
            """.trimIndent())
            .addCode(CodeFactory.buildCheckDmlCode())
            .addCode(buildBulkInsertCode(table))
            .build()
        return listOf(funSpec)
    }

    private fun buildBulkInsertCode(table: TableDefinition) = buildCodeBlock {
        beginControlFlow("return·database.%M(%T)", bulkInsertFun, table.tableClassName)
        val isInterfaceEntity = table.ktormEntityType == KtormEntityType.ENTITY_INTERFACE
        if (isInterfaceEntity) {
            beginControlFlow("with(%T())", typeNameOf<EntityExtensionsApi>())
        }
        beginControlFlow("for (entity in entities)")
        beginControlFlow("item")
        for (column in table.columns) {
            if (isInterfaceEntity) {
                val valueType = if (column.isReferences) {
                    column.referencesColumn!!.propertyTypeName.copy(nullable = true)
                } else {
                    column.propertyTypeName.copy(nullable = true)
                }
                addStatement(
                    "set(it.%N,·entity.getColumnValue(it.%N.binding!!) as %T)",
                    column.tablePropertyName.simpleName,
                    column.entityPropertyName.simpleName,
                    valueType
                )
            } else {
                val valueCode = CodeFactory.buildColumnValueCode(column)
                addStatement("set(it.%N,·%L)", column.tablePropertyName.simpleName, valueCode)
            }
        }
        endControlFlow()
        endControlFlow()
        if (isInterfaceEntity) {
            endControlFlow()
        }
        endControlFlow()
    }
}
