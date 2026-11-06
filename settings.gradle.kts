rootProject.name = "RestaurantApp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")

    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":composeApp")
include(":androidApp")
include(":domain:auth")
include(":domain:bookings")
include(":domain:settings")
include(":domain:analytics")
include(":presentation")
include(":commonResources")
include(":core:network")
include(":core:storage")
include(":core:database")
include(":core:mvi")
include(":core:analytics")
include(":core:notifications")
include(":data:auth")
include(":data:bookings")
include(":data:settings")
include(":data:analytics")
include(":feature:splash")
include(":feature:login")
include(":feature:register")
include(":feature:forgot_password")
include(":feature:home")
include(":feature:settings")
include(":feature:profile")
include(":feature:bookings")
include(":feature:edit_profile")
include(":feature:privacy_policy")
