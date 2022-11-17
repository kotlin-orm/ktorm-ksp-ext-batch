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

import com.squareup.kotlinpoet.CodeBlock
import org.ktorm.ksp.spi.definition.ColumnDefinition

public object CodeFactory {

    public fun buildCheckDmlCode(): CodeBlock {
        return CodeBlock.of(
            """
            val isModified =
                expression.where != null ||
                    expression.groupBy.isNotEmpty() ||
                    expression.having != null ||
                    expression.isDistinct ||
                    expression.orderBy.isNotEmpty() ||
                    expression.offset != null ||
                    expression.limit != null
        
            if (isModified) {
                val msg =
                    "Entity manipulation functions are not supported by this sequence object. " +
                        "Please call on the origin sequence returned from database.sequenceOf(table)"
                throw UnsupportedOperationException(msg)
            }
            
            
        """.trimIndent()
        )
    }

    public fun buildColumnValueCode(column: ColumnDefinition): CodeBlock {
        return if (column.isReferences) {
            val referenceColumn = column.referencesColumn!!
            val nullableOperator = if (column.isNullable) "?" else ""
            "entity.${column.entityPropertyName.simpleName}$nullableOperator.${referenceColumn.entityPropertyName.simpleName}"
            CodeBlock.of(
                "entity.%N%L.%N",
                column.entityPropertyName.simpleName,
                nullableOperator,
                referenceColumn.entityPropertyName.simpleName
            )
        } else {
            CodeBlock.of("entity.%N", column.entityPropertyName.simpleName)
        }
    }
}
