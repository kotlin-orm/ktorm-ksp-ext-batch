plugins {
    kotlin("jvm")
}

dependencies {
    api(libs.ktorm.core)
    api(libs.ktorm.ksp.compiler)
    api(libs.junit)
    api(libs.assertj.core)
    api(libs.kotlinCompileTesting)
    api(libs.kotlinCompileTesting.ksp)
    api(libs.slf4j.simple)
}

