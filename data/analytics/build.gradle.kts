plugins {
    id("cleanarch.kmp.base")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            // api (not implementation): AnalyticsConsentLocalDataSourceImpl's public constructor
            // takes a KVault directly, same criterion as data:auth's AuthLocalDataSourceImpl.
            api(libs.kvault)
            implementation(projects.domain.analytics)
            implementation(projects.core.analytics)
            implementation(projects.core.storage)
            // specs/003-decentralize-domain-data-di: data/analytics/di/AnalyticsDataModule.kt
            // declares its own Koin module instead of composeApp constructing these classes
            // directly.
            implementation(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
