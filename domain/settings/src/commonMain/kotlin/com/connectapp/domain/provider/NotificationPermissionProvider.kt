package com.connectapp.domain.provider

/**
 * See specs/015-notification-permissions/contracts/notification-permission-provider-contract.md.
 * `GRANTED` also covers platforms/versions that don't require an explicit runtime permission
 * (e.g. Android < 13) — there is no separate "not applicable" state (spec.md Assumptions).
 */
enum class NotificationPermissionStatus { GRANTED, DENIABLE, PERMANENTLY_DENIED }

/**
 * Bridges Settings' notifications toggle to the real OS-level notification permission —
 * see specs/015-notification-permissions. Backed by `:core:notifications` (`expect`/`actual`),
 * with a single implementation wired in `composeApp` (same pattern as [AppVersionProvider]).
 */
interface NotificationPermissionProvider {
    suspend fun checkStatus(): NotificationPermissionStatus
    suspend fun requestPermission(): NotificationPermissionStatus
    fun openAppSettings()
}
