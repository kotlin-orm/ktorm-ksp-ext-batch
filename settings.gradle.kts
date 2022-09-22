rootProject.name = "ktorm-ksp-ext-batch"

pluginManagement {
    val kotlinVersion: String by settings
    val googleKspVersion: String by settings
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "org.jetbrains.kotlin.jvm" -> useVersion(kotlinVersion)
                "com.google.devtools.ksp" -> useVersion(googleKspVersion)
            }
        }
    }
}

enableFeaturePreview("VERSION_CATALOGS")
include("ktorm-ksp-ext-batch-postgresql")
include("ktorm-ksp-ext-batch-mysql")
include("ktorm-ksp-ext-batch-sqlite")
include("ktorm-ksp-ext-batch-tests")
