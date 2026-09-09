plugins {
    id("cleanarch.kmp.base")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // api (not implementation): SettingsRepositoryImpl's public constructor takes a
            // KVault directly, part of this module's own public constructor surface.
            api(libs.kvault)
            implementation(projects.domain.settings)
            implementation(projects.core.storage)
            implementation(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
