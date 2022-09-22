package org.ktorm.ext.batch.mysql

import org.ktorm.database.Database
import org.ktorm.ksp.ext.batch.tests.BaseTest
import org.testcontainers.containers.MySQLContainer
import kotlin.concurrent.thread

public open class BaseMysqlTest : BaseTest() {

    override val database: Database by lazy {
        Database.connect(jdbcUrl, driverClassName, username, password)
    }

    public companion object : MySQLContainer<Companion>("mysql:8") {
        init {
            // Start the container when it's first used.
            start()
            // Stop the container when the process exits.
            Runtime.getRuntime().addShutdownHook(thread(start = false) { stop() })
        }
    }
}
