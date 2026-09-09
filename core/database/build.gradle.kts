import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("cleanarch.kmp.base")
}

kotlin {
    // SQLDelight's native-driver (touchlab/sqliter) links against the system libsqlite3 dylib;
    // Kotlin/Native doesn't add it automatically when this module's own iOS framework is linked
    // standalone (as every module here does, per cleanarch.kmp.base.gradle.kts) — without this,
    // linkDebugFrameworkIosArm64/-SimulatorArm64 fail with "Undefined symbols: _sqlite3_bind_*".
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.withType<Framework>().configureEach {
            linkerOpts += "-lsqlite3"
        }
    }

    sourceSets {
        commonMain.dependencies {
            // api (not implementation): createSqlDriver() returns this type directly to callers.
            api(libs.sqldelight.runtime)
        }

        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }

        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
    }
}
