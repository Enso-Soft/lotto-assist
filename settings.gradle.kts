pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "lotto-assist"
include(":app")
include(":core:di")
include(":core:data")
include(":core:domain")
include(":core:network")
include(":core:database")
include(":core:util")
include(":core:ui")
include(":feature:home")
include(":feature:qrscan")
