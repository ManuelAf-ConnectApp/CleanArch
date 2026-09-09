import cleanarch.convention.library

// Base Compose Multiplatform setup on top of cleanarch.kmp.base: compiler plugin plus the
// common UI libraries every Compose module needs.
plugins {
    id("cleanarch.kmp.base")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(library("compose-runtime"))
            implementation(library("compose-foundation"))
            implementation(library("compose-material3"))
            implementation(library("compose-ui"))
            implementation(library("compose-components-resources"))
        }
    }
}
