plugins {
    kotlin("jvm")
}

dependencies {
    api(libs.ktorm.core)
    api(libs.ktorm.ksp.spi)
    testImplementation(project(":ktorm-ksp-ext-batch-tests"))
    testImplementation(libs.testcontainers.msyql)
    testImplementation(libs.ktorm.support.mysql)

}

configureMavenPublishing()
