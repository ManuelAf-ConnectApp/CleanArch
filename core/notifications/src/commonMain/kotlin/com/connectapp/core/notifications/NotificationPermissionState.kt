package com.connectapp.core.notifications

/**
 * Own enum, distinct from `:domain`'s `NotificationPermissionStatus` — `:core:*` modules cannot
 * depend on `:domain` (module boundaries, specs/013-enforce-module-boundaries). The 1:1 mapping
 * between the two lives solely in composeApp's `NotificationPermissionProviderImpl`.
 * See specs/015-notification-permissions/research.md §2.
 */
enum class NotificationPermissionState { GRANTED, DENIABLE, PERMANENTLY_DENIED }

/** Pure query — never triggers a dialog or navigation. See data-model.md. */
expect suspend fun checkNotificationPermissionState(): NotificationPermissionState

/**
 * Only meaningful when [checkNotificationPermissionState] returned `DENIABLE` — triggers the
 * platform's native permission dialog and suspends for the user's response.
 */
expect suspend fun requestNotificationPermission(): NotificationPermissionState

/** Fire-and-forget navigation to this app's OS-level notification settings screen. */
expect fun openNotificationSettings()
