package com.connectapp.presentation

import com.connectapp.domain.analytics.AnalyticsReporter
import com.connectapp.domain.analytics.FailureCategory
import com.connectapp.domain.analytics.FlowOutcome
import com.connectapp.domain.analytics.KeyFlow
import com.connectapp.domain.provider.NotificationPermissionProvider
import com.connectapp.domain.provider.NotificationPermissionStatus
import com.connectapp.domain.repository.SettingsRepository
import com.connectapp.presentation.navigation.NavigationRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeAnalyticsReporter : AnalyticsReporter {
    val screens = mutableListOf<String>()
    override fun setConsent(granted: Boolean) = Unit
    override fun observeConsent(): Flow<Boolean> = MutableStateFlow(false)
    override fun setCurrentScreen(screen: String) {
        screens += screen
    }
    override fun trackFlowEvent(flow: KeyFlow, outcome: FlowOutcome, failureCategory: FailureCategory?) = Unit
}

/** See specs/007-persist-apply-appearance-notifications/contracts/settings-repository-contract.md. */
private class FakeSettingsRepository(initialDarkModeEnabled: Boolean? = null) : SettingsRepository {
    private val darkModeFlow = MutableStateFlow(initialDarkModeEnabled)
    override fun observeDarkModeEnabled(): Flow<Boolean?> = darkModeFlow
    override suspend fun setDarkModeEnabled(enabled: Boolean) {
        darkModeFlow.value = enabled
    }
    var notificationsEnabled: Boolean? = null
        private set
    var setNotificationsCallCount: Int = 0
        private set
    override suspend fun isNotificationsEnabled(): Boolean? = notificationsEnabled
    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        setNotificationsCallCount++
        notificationsEnabled = enabled
    }
}

/** Same shape as feature/settings' own fake — see SettingsViewModelTest.kt. */
private class FakeNotificationPermissionProvider(
    var status: NotificationPermissionStatus = NotificationPermissionStatus.GRANTED,
    private val requestResult: NotificationPermissionStatus = NotificationPermissionStatus.GRANTED,
) : NotificationPermissionProvider {
    var requestCallCount: Int = 0
        private set
    var openAppSettingsCallCount: Int = 0
        private set
    var checkStatusCallCount: Int = 0
        private set

    override suspend fun checkStatus(): NotificationPermissionStatus {
        checkStatusCallCount++
        return status
    }

    override suspend fun requestPermission(): NotificationPermissionStatus {
        requestCallCount++
        status = requestResult
        return requestResult
    }

    override fun openAppSettings() {
        openAppSettingsCallCount++
    }
}

private const val DISABLED_NOTICE_MESSAGE = "Revoke it from OS Settings"

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(
        analyticsReporter: AnalyticsReporter = FakeAnalyticsReporter(),
        settingsRepository: SettingsRepository = FakeSettingsRepository(),
        notificationPermissionProvider: NotificationPermissionProvider = FakeNotificationPermissionProvider(),
    ) = MainViewModel(analyticsReporter, settingsRepository, notificationPermissionProvider)

    @Test
    fun `updateNavigationState updates the current route`() {
        val viewModel = viewModel()

        viewModel.updateNavigationState(NavigationRoute.HomeRoute)

        assertEquals(NavigationRoute.HomeRoute, viewModel.navState.value.route)
    }

    @Test
    fun `updateNavigationState reports the new screen to analytics`() {
        val analyticsReporter = FakeAnalyticsReporter()
        val viewModel = viewModel(analyticsReporter = analyticsReporter)

        viewModel.updateNavigationState(NavigationRoute.HomeRoute)

        assertEquals(listOf("HomeRoute"), analyticsReporter.screens)
    }

    @Test
    fun `showSnackBar populates the snack bar state`() {
        val viewModel = viewModel()

        viewModel.showSnackBar(message = "Hello", actionLabel = "Undo")

        assertEquals("Hello", viewModel.snackBarState.value?.message)
        assertEquals("Undo", viewModel.snackBarState.value?.actionLabel)
    }

    @Test
    fun `dismissSnackBar clears the snack bar state`() {
        val viewModel = viewModel()
        viewModel.showSnackBar(message = "Hello")

        viewModel.dismissSnackBar()

        assertNull(viewModel.snackBarState.value)
    }

    // specs/007-persist-apply-appearance-notifications FR-003: no saved preference -> null,
    // so App() falls back to isSystemInDarkTheme() instead of a hardcoded light/dark default.
    @Test
    fun `darkModeEnabled is null with no saved preference`() = runTest {
        val viewModel = viewModel(settingsRepository = FakeSettingsRepository(initialDarkModeEnabled = null))

        advanceUntilIdle()

        assertNull(viewModel.darkModeEnabled.value)
    }

    @Test
    fun `darkModeEnabled reflects the persisted preference`() = runTest {
        val viewModel = viewModel(settingsRepository = FakeSettingsRepository(initialDarkModeEnabled = true))

        advanceUntilIdle()

        assertTrue(viewModel.darkModeEnabled.value == true)
    }

    @Test
    fun `onNotificationBellClicked with permission already granted does not request or open settings`() = runTest {
        val provider = FakeNotificationPermissionProvider(status = NotificationPermissionStatus.GRANTED)
        val settingsRepository = FakeSettingsRepository()
        val viewModel = viewModel(settingsRepository = settingsRepository, notificationPermissionProvider = provider)

        viewModel.onNotificationBellClicked(disabledNoticeMessage = DISABLED_NOTICE_MESSAGE)
        advanceUntilIdle()

        assertEquals(0, provider.requestCallCount)
        assertEquals(0, provider.openAppSettingsCallCount)
        assertEquals(true, settingsRepository.notificationsEnabled)
    }

    @Test
    fun `onNotificationBellClicked with a deniable permission triggers the native dialog`() = runTest {
        val provider = FakeNotificationPermissionProvider(
            status = NotificationPermissionStatus.DENIABLE,
            requestResult = NotificationPermissionStatus.GRANTED,
        )
        val settingsRepository = FakeSettingsRepository()
        val viewModel = viewModel(settingsRepository = settingsRepository, notificationPermissionProvider = provider)

        viewModel.onNotificationBellClicked(disabledNoticeMessage = DISABLED_NOTICE_MESSAGE)
        advanceUntilIdle()

        assertEquals(1, provider.requestCallCount)
        assertEquals(0, provider.openAppSettingsCallCount)
        assertEquals(true, settingsRepository.notificationsEnabled)
    }

    @Test
    fun `onNotificationBellClicked with a permanently denied permission opens OS app settings, never navigates`() =
        runTest {
            val provider = FakeNotificationPermissionProvider(status = NotificationPermissionStatus.PERMANENTLY_DENIED)
            val settingsRepository = FakeSettingsRepository()
            val viewModel = viewModel(settingsRepository = settingsRepository, notificationPermissionProvider = provider)

            viewModel.onNotificationBellClicked(disabledNoticeMessage = DISABLED_NOTICE_MESSAGE)
            advanceUntilIdle()

            assertEquals(0, provider.requestCallCount)
            assertEquals(1, provider.openAppSettingsCallCount)
            assertEquals(false, settingsRepository.notificationsEnabled)
            assertEquals(NavigationRoute.SplashRoute, viewModel.navState.value.route)
        }

    // Drives CustomTopBar's bell icon (Notifications vs NotificationsOff) — see MainScreen.kt.
    @Test
    fun `notificationsEnabled reflects a granted permission after init`() = runTest {
        val provider = FakeNotificationPermissionProvider(status = NotificationPermissionStatus.GRANTED)
        val viewModel = viewModel(notificationPermissionProvider = provider)

        advanceUntilIdle()

        assertEquals(true, viewModel.notificationsEnabled.value)
    }

    @Test
    fun `notificationsEnabled reflects a denied permission after init`() = runTest {
        val provider = FakeNotificationPermissionProvider(status = NotificationPermissionStatus.PERMANENTLY_DENIED)
        val viewModel = viewModel(notificationPermissionProvider = provider)

        advanceUntilIdle()

        assertEquals(false, viewModel.notificationsEnabled.value)
    }

    @Test
    fun `onScreenResumed re-syncs notificationsEnabled with the current OS permission`() = runTest {
        val provider = FakeNotificationPermissionProvider(status = NotificationPermissionStatus.GRANTED)
        val viewModel = viewModel(notificationPermissionProvider = provider)
        advanceUntilIdle()
        assertEquals(true, viewModel.notificationsEnabled.value)

        // User revoked the OS permission outside the app (e.g. system Settings) and came back.
        provider.status = NotificationPermissionStatus.PERMANENTLY_DENIED
        viewModel.onScreenResumed()
        advanceUntilIdle()

        assertEquals(false, viewModel.notificationsEnabled.value)
    }

    @Test
    fun `onNotificationBellClicked updates notificationsEnabled once the permission is granted`() = runTest {
        val provider = FakeNotificationPermissionProvider(
            status = NotificationPermissionStatus.DENIABLE,
            requestResult = NotificationPermissionStatus.GRANTED,
        )
        val viewModel = viewModel(notificationPermissionProvider = provider)
        advanceUntilIdle()
        assertEquals(false, viewModel.notificationsEnabled.value)

        viewModel.onNotificationBellClicked(disabledNoticeMessage = DISABLED_NOTICE_MESSAGE)
        advanceUntilIdle()

        assertEquals(true, viewModel.notificationsEnabled.value)
    }

    // Android has no API to revoke an already-granted permission (FR-006) — tapping the bell
    // while it's showing enabled must NOT flip the icon (it would lie about notifications being
    // off); it only warns via the snackbar, same semantics as SettingsViewModel's
    // `NotificationsToggled(false) leaves the state untouched`.
    @Test
    fun `onNotificationBellClicked while already enabled leaves it enabled and warns via snackbar`() =
        runTest {
            val provider = FakeNotificationPermissionProvider(status = NotificationPermissionStatus.GRANTED)
            val settingsRepository = FakeSettingsRepository()
            val viewModel = viewModel(settingsRepository = settingsRepository, notificationPermissionProvider = provider)
            advanceUntilIdle()
            assertEquals(true, viewModel.notificationsEnabled.value)
            val checkStatusCallCountBeforeClick = provider.checkStatusCallCount
            val setNotificationsCallCountBeforeClick = settingsRepository.setNotificationsCallCount

            viewModel.onNotificationBellClicked(disabledNoticeMessage = DISABLED_NOTICE_MESSAGE)
            advanceUntilIdle()

            assertEquals(true, viewModel.notificationsEnabled.value)
            assertEquals(true, settingsRepository.notificationsEnabled)
            assertEquals(checkStatusCallCountBeforeClick, provider.checkStatusCallCount)
            assertEquals(setNotificationsCallCountBeforeClick, settingsRepository.setNotificationsCallCount)
            assertEquals(0, provider.requestCallCount)
            assertEquals(0, provider.openAppSettingsCallCount)
            assertEquals(DISABLED_NOTICE_MESSAGE, viewModel.snackBarState.value?.message)
        }

    // Enabling never revokes anything, so it must not show the "revoke it from OS Settings" notice.
    @Test
    fun `onNotificationBellClicked while disabled does not show the disabled notice`() = runTest {
        val provider = FakeNotificationPermissionProvider(status = NotificationPermissionStatus.GRANTED)
        val viewModel = viewModel(notificationPermissionProvider = provider)

        viewModel.onNotificationBellClicked(disabledNoticeMessage = DISABLED_NOTICE_MESSAGE)
        advanceUntilIdle()

        assertNull(viewModel.snackBarState.value)
    }
}
