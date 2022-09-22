plugins {
    kotlin("jvm")
}

dependencies {
    api(libs.ktorm.ksp.spi)
    testImplementation(project(":ktorm-ksp-ext-batch-tests"))
    testImplementation(libs.ktorm.support.sqlite)
}

configureMavenPublishing()
