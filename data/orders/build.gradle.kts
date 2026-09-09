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
            implementation(libs.sqldelight.coroutines.extensions)
            implementation(projects.domain.orders)
            implementation(projects.core.network)
            implementation(projects.core.database)
            // specs/003-decentralize-domain-data-di: data/orders/di/OrdersDataModule.kt declares
            // its own Koin module instead of composeApp constructing these classes directly.
            implementation(libs.koin.core)
        }
    }
}

sqldelight {
    databases {
        create("OrdersDatabase") {
            packageName.set("com.connectapp.data.orders.database")
        }
    }
}
