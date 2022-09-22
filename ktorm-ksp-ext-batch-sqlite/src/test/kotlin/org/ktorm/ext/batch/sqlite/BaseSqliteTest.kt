package org.ktorm.ext.batch.sqlite

import org.ktorm.database.Database
import org.ktorm.ksp.ext.batch.tests.BaseTest
import java.sql.Connection
import java.sql.DriverManager

public open class BaseSqliteTest: BaseTest() {
    override val database: Database by lazy {
        val connection = DriverManager.getConnection("jdbc:sqlite::memory:")
        Database.connect {
            object : Connection by connection {
                override fun close() {
                    // do nothing...
                }
            }
        }
    }
}
