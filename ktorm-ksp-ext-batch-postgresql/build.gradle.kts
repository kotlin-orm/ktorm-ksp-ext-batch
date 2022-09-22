plugins {
    kotlin("jvm")
}

dependencies {
    api(libs.ktorm.ksp.spi)
    testImplementation(project(":ktorm-ksp-ext-batch-tests"))
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.ktorm.support.postgresql)
}

configureMavenPublishing()
