package com.connectapp.presentation.settings

import com.connectapp.domain.analytics.AnalyticsReporter
import com.connectapp.domain.analytics.FailureCategory
import com.connectapp.domain.analytics.FlowOutcome
import com.connectapp.domain.analytics.KeyFlow
import com.connectapp.domain.model.Order
import com.connectapp.domain.model.User
import com.connectapp.domain.repository.AuthRepository
import com.connectapp.domain.repository.OrdersRepository
import com.connectapp.domain.provider.AppVersionProvider
import com.connectapp.domain.provider.NotificationPermissionProvider
import com.connectapp.domain.provider.NotificationPermissionStatus
import com.connectapp.domain.repository.SettingsRepository
import com.connectapp.domain.usecase.LogoutUseCase
import com.connectapp.domain.usecase.ObserveAnalyticsConsentUseCase
import com.connectapp.domain.usecase.ObserveProfileUseCase
import com.connectapp.domain.usecase.SetAnalyticsConsentUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FakeAuthRepository(user: User? = null) : AuthRepository {
    var logoutCallCount: Int = 0
        private set

    private val profileFlow = MutableStateFlow(user)

    override suspend fun login(email: String, password: String, rememberSession: Boolean): Result<User> =
        error("Not used by SettingsViewModelTest")
    override suspend fun register(name: String, email: String, password: String): Result<Boolean> =
        error("Not used by SettingsViewModelTest")
    override suspend fun forgotPassword(email: String): Result<Unit> = error("Not used by SettingsViewModelTest")
    override suspend fun hasActiveSession(): Boolean = error("Not used by SettingsViewModelTest")
    override fun persistRememberedSession(email: String) = error("Not used by SettingsViewModelTest")
    override fun clearRememberedSession() = error("Not used by SettingsViewModelTest")
    override fun observeProfile(): Flow<User?> = profileFlow
    override fun logout() {
        logoutCallCount++
    }

    override fun observeProfileFetchedAt(): Flow<Long?> = MutableStateFlow(null)
    override suspend fun updateProfile(user: User): Result<Unit> = error("Not used by SettingsViewModelTest")
}

/** See specs/007-persist-apply-appearance-notifications/contracts/settings-repository-contract.md. */
private class FakeSettingsRepository(
    initialDarkModeEnabled: Boolean? = null,
    private val initialNotificationsEnabled: Boolean? = null,
) : SettingsRepository {
    private val darkModeFlow = MutableStateFlow(initialDarkModeEnabled)

    var setDarkModeCallCount: Int = 0
        private set
    var setNotificationsCallCount: Int = 0
        private set

    override fun observeDarkModeEnabled(): Flow<Boolean?> = darkModeFlow

    override suspend fun setDarkModeEnabled(enabled: Boolean) {
        setDarkModeCallCount++
        darkModeFlow.value = enabled
    }

    override suspend fun isNotificationsEnabled(): Boolean? = initialNotificationsEnabled

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        setNotificationsCallCount++
    }
}

private class FakeOrdersRepository : OrdersRepository {
    var clearCacheCallCount: Int = 0
        private set

    override fun observeOrders(): Flow<List<Order>> = MutableStateFlow(emptyList())
    override suspend fun refreshOrders(): Result<Unit> = Result.success(Unit)
    override suspend fun clearCache() {
        clearCacheCallCount++
    }

    override fun observeLastFetchedAt(): Flow<Long?> = MutableStateFlow(null)
}

private class FakeAppVersionProvider(override val versionName: String = "1.0-test") : AppVersionProvider

/** See specs/015-notification-permissions/contracts/notification-permission-provider-contract.md. */
private class FakeNotificationPermissionProvider(
    var status: NotificationPermissionStatus = NotificationPermissionStatus.GRANTED,
    private val requestResult: NotificationPermissionStatus = NotificationPermissionStatus.GRANTED,
) : NotificationPermissionProvider {
    var checkStatusCallCount: Int = 0
        private set
    var requestCallCount: Int = 0
        private set
    var openAppSettingsCallCount: Int = 0
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

private class FakeAnalyticsReporter : AnalyticsReporter {
    private val consentFlow = MutableStateFlow(false)
    override fun setConsent(granted: Boolean) {
        consentFlow.value = granted
    }
    override fun observeConsent(): Flow<Boolean> = consentFlow
    override fun setCurrentScreen(screen: String) = Unit
    override fun trackFlowEvent(flow: KeyFlow, outcome: FlowOutcome, failureCategory: FailureCategory?) = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

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
        authRepository: FakeAuthRepository = FakeAuthRepository(),
        ordersRepository: FakeOrdersRepository = FakeOrdersRepository(),
        analyticsReporter: FakeAnalyticsReporter = FakeAnalyticsReporter(),
        settingsRepository: FakeSettingsRepository = FakeSettingsRepository(),
        appVersionProvider: FakeAppVersionProvider = FakeAppVersionProvider(),
        notificationPermissionProvider: FakeNotificationPermissionProvider = FakeNotificationPermissionProvider(),
    ) = SettingsViewModel(
        LogoutUseCase(authRepository, ordersRepository),
        ObserveAnalyticsConsentUseCase(analyticsReporter),
        SetAnalyticsConsentUseCase(analyticsReporter),
        ObserveProfileUseCase(authRepository),
        settingsRepository,
        appVersionProvider,
        notificationPermissionProvider,
    )

    // The OS can't be told to revoke an already-granted permission (FR-006), so unchecking the
    // switch must not flip the state to false — it would show the toggle as off while
    // notifications stay actually enabled.
    @Test
    fun `NotificationsToggled(false) leaves the state untouched`() {
        val viewModel = viewModel()

        viewModel.onIntent(SettingsIntent.NotificationsToggled(false))

        assertTrue(viewModel.state.value.notificationsEnabled)
    }

    @Test
    fun `DarkModeToggled updates the state synchronously`() {
        val viewModel = viewModel()

        viewModel.onIntent(SettingsIntent.DarkModeToggled(true))

        assertTrue(viewModel.state.value.darkModeEnabled)
    }

    @Test
    fun `AnalyticsConsentToggled persists consent and is reflected back into state`() = runTest {
        val analyticsReporter = FakeAnalyticsReporter()
        val viewModel = viewModel(analyticsReporter = analyticsReporter)

        viewModel.onIntent(SettingsIntent.AnalyticsConsentToggled(true))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.analyticsConsentEnabled)
    }

    @Test
    fun `appVersion is populated synchronously from AppVersionProvider`() {
        val viewModel = viewModel(appVersionProvider = FakeAppVersionProvider("2.5.1"))

        assertEquals("2.5.1", viewModel.state.value.appVersion)
    }

    @Test
    fun `initial load populates user from the observed profile`() = runTest {
        val user = User(firstName = "Grace", lastName = "Hopper", email = "grace@example.com", phone = "+1-555-0199")
        val viewModel = viewModel(authRepository = FakeAuthRepository(user))

        advanceUntilIdle()

        assertEquals("Grace Hopper", viewModel.state.value.userName)
        assertEquals("grace@example.com", viewModel.state.value.userEmail)
    }

    @Test
    fun `initial load seeds darkModeEnabled from SettingsRepository and notificationsEnabled from NotificationPermissionProvider`() =
        runTest {
            val settingsRepository = FakeSettingsRepository(initialDarkModeEnabled = true)
            val notificationPermissionProvider =
                FakeNotificationPermissionProvider(status = NotificationPermissionStatus.DENIABLE)
            val viewModel = viewModel(
                settingsRepository = settingsRepository,
                notificationPermissionProvider = notificationPermissionProvider,
            )

            advanceUntilIdle()

            assertTrue(viewModel.state.value.darkModeEnabled)
            assertFalse(viewModel.state.value.notificationsEnabled)
            assertEquals(1, notificationPermissionProvider.checkStatusCallCount)
        }

    // specs/015-notification-permissions US3: the toggle picks up permission changes made outside
    // the app (Ajustes del sistema operativo) as soon as Settings comes back to the foreground.
    @Test
    fun `ScreenResumed re-syncs notificationsEnabled when the OS permission changed externally`() = runTest {
        val notificationPermissionProvider =
            FakeNotificationPermissionProvider(status = NotificationPermissionStatus.GRANTED)
        val viewModel = viewModel(notificationPermissionProvider = notificationPermissionProvider)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.notificationsEnabled)

        notificationPermissionProvider.status = NotificationPermissionStatus.DENIABLE
        viewModel.onIntent(SettingsIntent.ScreenResumed)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.notificationsEnabled)
    }

    @Test
    fun `no saved dark mode preference keeps the system default`() = runTest {
        val settingsRepository = FakeSettingsRepository(initialDarkModeEnabled = null)
        val viewModel = viewModel(settingsRepository = settingsRepository)

        advanceUntilIdle()

        assertFalse(viewModel.state.value.darkModeEnabled)
    }

    @Test
    fun `DarkModeToggled persists the new value via SettingsRepository`() = runTest {
        val settingsRepository = FakeSettingsRepository()
        val viewModel = viewModel(settingsRepository = settingsRepository)

        viewModel.onIntent(SettingsIntent.DarkModeToggled(true))
        advanceUntilIdle()

        assertEquals(1, settingsRepository.setDarkModeCallCount)
    }

    // Nothing is actually being disabled (FR-006), so there's nothing new to persist.
    @Test
    fun `NotificationsToggled(false) does not write to SettingsRepository`() = runTest {
        val settingsRepository = FakeSettingsRepository()
        val viewModel = viewModel(settingsRepository = settingsRepository)
        advanceUntilIdle() // let init's own refreshNotificationsState() (US3) settle first
        val callCountBeforeToggle = settingsRepository.setNotificationsCallCount

        viewModel.onIntent(SettingsIntent.NotificationsToggled(false))
        advanceUntilIdle()

        assertEquals(callCountBeforeToggle, settingsRepository.setNotificationsCallCount)
    }

    // specs/015-notification-permissions US1: activating the toggle defers to the real OS permission.
    @Test
    fun `NotificationsToggled(true) with GRANTED status activates immediately without requesting`() = runTest {
        val notificationPermissionProvider =
            FakeNotificationPermissionProvider(status = NotificationPermissionStatus.GRANTED)
        val viewModel = viewModel(notificationPermissionProvider = notificationPermissionProvider)

        viewModel.onIntent(SettingsIntent.NotificationsToggled(true))
        advanceUntilIdle()

        assertEquals(0, notificationPermissionProvider.requestCallCount)
        assertTrue(viewModel.state.value.notificationsEnabled)
    }

    @Test
    fun `NotificationsToggled(true) with DENIABLE status requests permission and reflects acceptance`() = runTest {
        val notificationPermissionProvider = FakeNotificationPermissionProvider(
            status = NotificationPermissionStatus.DENIABLE,
            requestResult = NotificationPermissionStatus.GRANTED,
        )
        val viewModel = viewModel(notificationPermissionProvider = notificationPermissionProvider)

        viewModel.onIntent(SettingsIntent.NotificationsToggled(true))
        advanceUntilIdle()

        assertEquals(1, notificationPermissionProvider.requestCallCount)
        assertTrue(viewModel.state.value.notificationsEnabled)
    }

    @Test
    fun `NotificationsToggled(true) with DENIABLE status requests permission and reflects rejection`() = runTest {
        val notificationPermissionProvider = FakeNotificationPermissionProvider(
            status = NotificationPermissionStatus.DENIABLE,
            requestResult = NotificationPermissionStatus.DENIABLE,
        )
        val viewModel = viewModel(notificationPermissionProvider = notificationPermissionProvider)

        viewModel.onIntent(SettingsIntent.NotificationsToggled(true))
        advanceUntilIdle()

        assertEquals(1, notificationPermissionProvider.requestCallCount)
        assertFalse(viewModel.state.value.notificationsEnabled)
    }

    // specs/015-notification-permissions US2: the OS won't show its dialog again — go to Settings instead.
    @Test
    fun `NotificationsToggled(true) with PERMANENTLY_DENIED status opens app settings without requesting`() = runTest {
        val notificationPermissionProvider =
            FakeNotificationPermissionProvider(status = NotificationPermissionStatus.PERMANENTLY_DENIED)
        val viewModel = viewModel(notificationPermissionProvider = notificationPermissionProvider)

        viewModel.onIntent(SettingsIntent.NotificationsToggled(true))
        advanceUntilIdle()

        assertEquals(1, notificationPermissionProvider.openAppSettingsCallCount)
        assertEquals(0, notificationPermissionProvider.requestCallCount)
        assertFalse(viewModel.state.value.notificationsEnabled)
    }

    // specs/015-notification-permissions US4: disabling never touches the OS permission (FR-006).
    @Test
    fun `NotificationsToggled(false) never calls NotificationPermissionProvider`() = runTest {
        val notificationPermissionProvider = FakeNotificationPermissionProvider()
        val viewModel = viewModel(notificationPermissionProvider = notificationPermissionProvider)
        advanceUntilIdle() // let init's own refreshNotificationsState() (US3) settle first
        val checkStatusCallCountBeforeToggle = notificationPermissionProvider.checkStatusCallCount

        viewModel.onIntent(SettingsIntent.NotificationsToggled(false))
        advanceUntilIdle()

        assertEquals(checkStatusCallCountBeforeToggle, notificationPermissionProvider.checkStatusCallCount)
        assertEquals(0, notificationPermissionProvider.requestCallCount)
        assertEquals(0, notificationPermissionProvider.openAppSettingsCallCount)
        assertTrue(viewModel.state.value.notificationsEnabled)
    }

    // The toggle can never actually revoke the OS permission (FR-006), so disabling it must warn
    // the user that the OS Settings screen is the only real way to do that.
    @Test
    fun `NotificationsToggled(false) emits ShowNotificationsDisabledNotice`() = runTest {
        val viewModel = viewModel()
        val effects = mutableListOf<SettingsEffect>()
        val job = launch { viewModel.effect.toList(effects) }

        viewModel.onIntent(SettingsIntent.NotificationsToggled(false))
        advanceUntilIdle()

        assertTrue(effects.any { it is SettingsEffect.ShowNotificationsDisabledNotice })
        job.cancel()
    }

    @Test
    fun `NotificationsToggled(true) does not emit ShowNotificationsDisabledNotice`() = runTest {
        val notificationPermissionProvider =
            FakeNotificationPermissionProvider(status = NotificationPermissionStatus.GRANTED)
        val viewModel = viewModel(notificationPermissionProvider = notificationPermissionProvider)
        val effects = mutableListOf<SettingsEffect>()
        val job = launch { viewModel.effect.toList(effects) }

        viewModel.onIntent(SettingsIntent.NotificationsToggled(true))
        advanceUntilIdle()

        assertFalse(effects.any { it is SettingsEffect.ShowNotificationsDisabledNotice })
        job.cancel()
    }

    @Test
    fun `LogoutClicked invokes LogoutUseCase clears loading and emits NavigateToLogin`() = runTest {
        val authRepository = FakeAuthRepository()
        val ordersRepository = FakeOrdersRepository()
        val viewModel = viewModel(authRepository, ordersRepository)
        val effects = mutableListOf<SettingsEffect>()
        val job = launch { viewModel.effect.toList(effects) }

        viewModel.onIntent(SettingsIntent.LogoutClicked)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertTrue(effects.any { it is SettingsEffect.NavigateToLogin })
        assertEquals(1, authRepository.logoutCallCount)
        assertEquals(1, ordersRepository.clearCacheCallCount)
        job.cancel()
    }
}
