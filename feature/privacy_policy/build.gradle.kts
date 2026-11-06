plugins {
    id("restaurantapp.kmp.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.icons.extended)

            implementation(projects.commonResources)
        }
    }
}
