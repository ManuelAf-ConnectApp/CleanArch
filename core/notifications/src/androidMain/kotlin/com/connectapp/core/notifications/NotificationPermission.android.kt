package com.connectapp.core.notifications

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Holds the Android application [Context] and the currently foreground [ComponentActivity],
 * both captured automatically via [NotificationPermissionContextInitializer] — same "auto-init
 * ContentProvider" pattern as core:storage's KVaultProvider/core:analytics's AnalyticsClient,
 * extended here with `Application.ActivityLifecycleCallbacks` since requesting the runtime
 * permission needs a live Activity, not just a Context. See
 * specs/015-notification-permissions/research.md §4.
 */
internal object AndroidContextHolder {
    lateinit var applicationContext: Context
        private set

    var foregroundActivity: ComponentActivity? = null
        private set

    internal fun install(context: Context) {
        if (::applicationContext.isInitialized) return
        applicationContext = context.applicationContext
        (applicationContext as? Application)?.registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityResumed(activity: Activity) {
                    if (activity is ComponentActivity) foregroundActivity = activity
                }

                override fun onActivityPaused(activity: Activity) {
                    if (foregroundActivity === activity) foregroundActivity = null
                }

                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
                override fun onActivityStarted(activity: Activity) = Unit
                override fun onActivityStopped(activity: Activity) = Unit
                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
                override fun onActivityDestroyed(activity: Activity) = Unit
            }
        )
    }
}

/** Registered in this module's AndroidManifest.xml; merged into the host app's manifest automatically. */
class NotificationPermissionContextInitializer : ContentProvider() {

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

private const val PREFS_NAME = "connectapp_core_notifications"
private const val KEY_HAS_REQUESTED = "has_requested_notification_permission"
private const val REQUEST_KEY = "connectapp_notification_permission_request"

/**
 * Distinguishes "never requested" from "permanently denied" — `shouldShowRequestPermissionRationale`
 * alone can't tell them apart (both return `false`). See research.md §3. Deliberately not KVault:
 * `:core:*` cannot depend on `:core:storage` (module boundaries, spec 013).
 */
private fun prefs(): SharedPreferences =
    AndroidContextHolder.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

private fun hasRequestedBefore(): Boolean = prefs().getBoolean(KEY_HAS_REQUESTED, false)

private fun markRequested() {
    prefs().edit().putBoolean(KEY_HAS_REQUESTED, true).apply()
}

actual suspend fun checkNotificationPermissionState(): NotificationPermissionState {
    // Pre-33: no runtime permission exists to gate on (FR-008) — always GRANTED.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return NotificationPermissionState.GRANTED

    val context = AndroidContextHolder.applicationContext
    val activity = AndroidContextHolder.foregroundActivity
    val canShowRationale = activity != null &&
        ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)

    return when {
        NotificationManagerCompat.from(context).areNotificationsEnabled() -> NotificationPermissionState.GRANTED
        !hasRequestedBefore() -> NotificationPermissionState.DENIABLE
        canShowRationale -> NotificationPermissionState.DENIABLE
        else -> NotificationPermissionState.PERMANENTLY_DENIED
    }
}

actual suspend fun requestNotificationPermission(): NotificationPermissionState {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return NotificationPermissionState.GRANTED

    val activity = AndroidContextHolder.foregroundActivity

    return if (activity == null) {
        checkNotificationPermissionState()
    } else {
        markRequested()

        // Raw ActivityResultRegistry.register (not rememberLauncherForActivityResult/
        // ComponentActivity.registerForActivityResult) so this can be called as a plain suspend
        // function from SettingsViewModel, not tied to a Composable's lifecycle. See research.md §4.
        val granted = suspendCancellableCoroutine { continuation ->
            lateinit var launcher: androidx.activity.result.ActivityResultLauncher<String>
            launcher = activity.activityResultRegistry.register(
                REQUEST_KEY,
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                launcher.unregister()
                if (continuation.isActive) continuation.resume(isGranted)
            }
            continuation.invokeOnCancellation { launcher.unregister() }
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (granted) NotificationPermissionState.GRANTED else checkNotificationPermissionState()
    }
}

actual fun openNotificationSettings() {
    val context = AndroidContextHolder.applicationContext
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}
