package com.connectapp.core.analytics

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ApplicationInfo
import android.database.Cursor
import android.net.Uri
import io.sentry.Sentry
import io.sentry.android.core.SentryAndroid

/**
 * Holds the Android application [Context] needed to initialize the Sentry Android SDK.
 *
 * Same "auto-init ContentProvider" pattern as core:storage's KVaultProvider/core:database's
 * DatabaseDriverFactory — captured automatically at process start via
 * [AnalyticsContextInitializer], a manifest-declared ContentProvider, so the app module never
 * has to wire a Context through Koin for this module either.
 */
internal object AndroidContextHolder {
    lateinit var applicationContext: Context
        private set

    internal fun install(context: Context) {
        if (!::applicationContext.isInitialized) {
            applicationContext = context.applicationContext
        }
    }
}

/** Registered in this module's AndroidManifest.xml; merged into the host app's manifest automatically. */
class AnalyticsContextInitializer : ContentProvider() {

    override fun onCreate(): Boolean {
        context?.let { AndroidContextHolder.install(it) }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0
}

/**
 * `environment` ("debug"/"release", FR-010's Build Channel) is derived from
 * [ApplicationInfo.FLAG_DEBUGGABLE] rather than threaded in as a parameter — it's a fact about
 * the running app's own build type, this module already has the [Context] to ask for it, and it
 * keeps every caller of [initSentry] from having to plumb it through from `composeApp`.
 */
actual fun initSentry(dsn: String) {
    if (dsn.isBlank()) return
    runCatching {
        val context = AndroidContextHolder.applicationContext
        val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        SentryAndroid.init(context) { options ->
            options.dsn = dsn
            options.environment = if (isDebuggable) "debug" else "release"
        }
    }
}

actual fun trackEvent(name: String, data: Map<String, String>) {
    runCatching {
        Sentry.withScope { scope ->
            data.forEach { (key, value) -> scope.setTag(key, value) }
            Sentry.captureMessage(name)
        }
    }
}

actual fun setTag(key: String, value: String) {
    runCatching { Sentry.setTag(key, value) }
}
