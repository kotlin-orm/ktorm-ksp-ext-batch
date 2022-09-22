package org.ktorm.ext.batch.postgresql

import org.ktorm.database.Database
import org.ktorm.ksp.ext.batch.tests.BaseTest
import org.testcontainers.containers.PostgreSQLContainer
import kotlin.concurrent.thread

public open class BasePostgresqlTest : BaseTest() {

    override val database: Database by lazy {
        Database.connect(jdbcUrl, driverClassName, username, password)
    }

    public companion object : PostgreSQLContainer<Companion>("postgres:13-alpine") {
        init {
            // Start the container when it's first used.
            start()
            // Stop the container when the process exits.
            Runtime.getRuntime().addShutdownHook(thread(start = false) { stop() })
        }
    }
}
