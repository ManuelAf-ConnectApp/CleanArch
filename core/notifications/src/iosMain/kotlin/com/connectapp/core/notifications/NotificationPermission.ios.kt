package com.connectapp.core.notifications

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatus
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

actual suspend fun checkNotificationPermissionState(): NotificationPermissionState =
    suspendCancellableCoroutine { continuation ->
        UNUserNotificationCenter.currentNotificationCenter()
            .getNotificationSettingsWithCompletionHandler { settings ->
                continuation.resume(settings?.authorizationStatus.toPermissionState())
            }
    }

/**
 * Unlike Android, iOS has no "ask again" — once the user responds to this dialog, the OS never
 * shows it again for this app; a later `false` here means the same as a later `checkStatus()`
 * returning `denied` (research.md §5), so it's reported as `PERMANENTLY_DENIED` directly.
 */
actual suspend fun requestNotificationPermission(): NotificationPermissionState =
    suspendCancellableCoroutine { continuation ->
        val options = UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound
        UNUserNotificationCenter.currentNotificationCenter()
            .requestAuthorizationWithOptions(options) { granted, _ ->
                continuation.resume(
                    if (granted) {
                        NotificationPermissionState.GRANTED
                    } else {
                        NotificationPermissionState.PERMANENTLY_DENIED
                    }
                )
            }
    }

actual fun openNotificationSettings() {
    val url = NSURL(string = UIApplicationOpenSettingsURLString)
    UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any?>(), completionHandler = null)
}

private fun UNAuthorizationStatus?.toPermissionState(): NotificationPermissionState = when (this) {
    UNAuthorizationStatusAuthorized,
    UNAuthorizationStatusProvisional,
    UNAuthorizationStatusEphemeral -> NotificationPermissionState.GRANTED
    UNAuthorizationStatusDenied -> NotificationPermissionState.PERMANENTLY_DENIED
    else -> NotificationPermissionState.DENIABLE // notDetermined, or unrecognized/null
}
