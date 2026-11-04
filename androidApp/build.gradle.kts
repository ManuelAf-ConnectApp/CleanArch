plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.detekt)
}

val releaseVersionName = providers.gradleProperty("releaseVersionName").orNull ?: "1.0"

android {
    namespace = "com.connectapp.cleanarch"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.connectapp.cleanarch"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = providers.gradleProperty("releaseVersionCode").orNull?.toIntOrNull() ?: 1
        versionName = releaseVersionName
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            buildConfigField("String", "APP_ENVIRONMENT", "\"debug\"")
        }
        create("staging") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            matchingFallbacks += listOf("debug")
            buildConfigField("String", "APP_ENVIRONMENT", "\"staging\"")
        }
        getByName("release") {
            buildConfigField("String", "APP_ENVIRONMENT", "\"release\"")
        }
    }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(projects.composeApp)
    implementation(libs.androidx.activity.compose)

    detektPlugins(libs.detekt.formatting)
}

detekt {
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    source.setFrom(files("src/main/kotlin"))
}
