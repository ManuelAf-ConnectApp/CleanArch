package com.connectapp.core.analytics

// No-op placeholder: iOS crash/analytics reporting is deferred, not implemented here.
// sentry-kotlin-multiplatform is experimental support for Compose Multiplatform and requires
// CocoaPods or Swift Package Manager in iosApp.xcodeproj, which this project doesn't use today
// (it consumes the Kotlin/Native XCFramework directly). See
// specs/011-crash-analytics-reporting/plan.md § Complexity Tracking and research.md D1.

actual fun initSentry(dsn: String) = Unit

actual fun trackEvent(name: String, data: Map<String, String>) = Unit

actual fun setTag(key: String, value: String) = Unit
