import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("cleanarch.kmp.base")
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    // See core/database/build.gradle.kts — same libsqlite3 linker requirement, needed here too
    // since this module's own iOS framework links app.cash.sqldelight's native-driver.
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.withType<Framework>().configureEach {
            linkerOpts += "-lsqlite3"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.sqldelight.coroutines.extensions)
            // api (not implementation): AuthServiceImpl's/AuthLocalDataSourceImpl's public
            // constructors take an HttpClient/KVault directly, part of this module's own public
            // constructor surface.
            api(libs.ktor.client.core)
            api(libs.kvault)
            implementation(projects.domain.auth)
            implementation(projects.core.network)
            implementation(projects.core.database)
            // specs/003-decentralize-domain-data-di: data/di/AuthDataModule.kt now constructs
            // its own KVault via createKVault() instead of composeApp doing it.
            implementation(projects.core.storage)
            implementation(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(libs.ktor.client.mock)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

sqldelight {
    databases {
        create("AuthDatabase") {
            packageName.set("com.connectapp.data.database")
        }
    }
}
