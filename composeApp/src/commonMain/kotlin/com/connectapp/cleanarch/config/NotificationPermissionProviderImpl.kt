package com.connectapp.cleanarch.config

import com.connectapp.core.notifications.NotificationPermissionState
import com.connectapp.core.notifications.checkNotificationPermissionState
import com.connectapp.core.notifications.openNotificationSettings
import com.connectapp.core.notifications.requestNotificationPermission
import com.connectapp.domain.provider.NotificationPermissionProvider
import com.connectapp.domain.provider.NotificationPermissionStatus

/**
 * Backs [NotificationPermissionProvider] by delegating to `:core:notifications`'s `expect`
 * functions — same placement/pattern as [AppVersionProviderImpl]. See
 * specs/015-notification-permissions/contracts/notification-permission-provider-contract.md.
 */
internal class NotificationPermissionProviderImpl : NotificationPermissionProvider {
    override suspend fun checkStatus(): NotificationPermissionStatus =
        checkNotificationPermissionState().toDomain()

    override suspend fun requestPermission(): NotificationPermissionStatus =
        requestNotificationPermission().toDomain()

    override fun openAppSettings() = openNotificationSettings()

    private fun NotificationPermissionState.toDomain(): NotificationPermissionStatus = when (this) {
        NotificationPermissionState.GRANTED -> NotificationPermissionStatus.GRANTED
        NotificationPermissionState.DENIABLE -> NotificationPermissionStatus.DENIABLE
        NotificationPermissionState.PERMANENTLY_DENIED -> NotificationPermissionStatus.PERMANENTLY_DENIED
    }
}
