package com.connectapp.presentation.login

import com.connectapp.commonresources.login_error_invalid_credentials
import com.connectapp.domain.analytics.AnalyticsReporter
import com.connectapp.domain.analytics.FailureCategory
import com.connectapp.domain.analytics.FlowOutcome
import com.connectapp.domain.analytics.KeyFlow
import com.connectapp.domain.model.AuthError
import com.connectapp.domain.model.User
import com.connectapp.domain.usecase.LoginUseCase
import com.connectapp.domain.usecase.SetSessionRememberedUseCase
import com.connectapp.domain.usecase.TrackFlowEventUseCase
import com.connectapp.domain.usecase.UpdateProfileUseCase
import com.connectapp.presentation.login.fake.FakeAuthRepository
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

private class FakeAnalyticsReporter : AnalyticsReporter {
    val trackedEvents = mutableListOf<Triple<KeyFlow, FlowOutcome, FailureCategory?>>()
    override fun setConsent(granted: Boolean) = Unit
    override fun observeConsent(): Flow<Boolean> = MutableStateFlow(false)
    override fun setCurrentScreen(screen: String) = Unit
    override fun trackFlowEvent(flow: KeyFlow, outcome: FlowOutcome, failureCategory: FailureCategory?) {
        trackedEvents += Triple(flow, outcome, failureCategory)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val testUser = User(firstName = "Jane", lastName = "Doe", email = "jane@example.com", phone = "+1")

    private fun viewModel(
        repository: FakeAuthRepository,
        analyticsReporter: AnalyticsReporter,
    ) = LoginViewModel(
        LoginUseCase(repository),
        TrackFlowEventUseCase(analyticsReporter),
        UpdateProfileUseCase(repository),
        SetSessionRememberedUseCase(repository),
    )

    @Test
    fun `LoginClicked with valid credentials updates state and emits NavigateToHome`() = runTest {
        val analyticsReporter = FakeAnalyticsReporter()
        val repository = FakeAuthRepository(loginResult = Result.success(testUser))
        val viewModel = viewModel(repository, analyticsReporter)
        val effects = mutableListOf<LoginEffect>()
        val job = launch { viewModel.effect.toList(effects) }

        viewModel.onIntent(LoginIntent.EmailChanged("jane@example.com"))
        viewModel.onIntent(LoginIntent.PasswordChanged("secret"))
        viewModel.onIntent(LoginIntent.LoginClicked)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isLoggedIn)
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(effects.any { it is LoginEffect.NavigateToHome })
        assertEquals(
            listOf<Triple<KeyFlow, FlowOutcome, FailureCategory?>>(Triple(KeyFlow.LOGIN, FlowOutcome.SUCCESS, null)),
            analyticsReporter.trackedEvents,
        )
        job.cancel()
    }

    @Test
    fun `LoginClicked with the dev admin credentials logs in and seeds a profile without calling login`() = runTest {
        val analyticsReporter = FakeAnalyticsReporter()
        // loginResult is a failure here on purpose: if the dev bypass ever regressed into
        // calling login(), this test would then see isLoggedIn == false and fail.
        val repository = FakeAuthRepository(loginResult = Result.failure(AuthError.InvalidCredentials))
        val viewModel = viewModel(repository, analyticsReporter)
        val effects = mutableListOf<LoginEffect>()
        val job = launch { viewModel.effect.toList(effects) }

        viewModel.onIntent(LoginIntent.EmailChanged("admin@admin.com"))
        viewModel.onIntent(LoginIntent.PasswordChanged("Admin@2026"))
        viewModel.onIntent(LoginIntent.LoginClicked)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isLoggedIn)
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(effects.any { it is LoginEffect.NavigateToHome })
        assertTrue(analyticsReporter.trackedEvents.isEmpty())
        assertEquals("admin@admin.com", repository.lastUpdatedProfile?.email)
        job.cancel()
    }

    @Test
    fun `LoginClicked with repository failure shows error and does not log in`() = runTest {
        val analyticsReporter = FakeAnalyticsReporter()
        val repository = FakeAuthRepository(loginResult = Result.failure(AuthError.InvalidCredentials))
        val viewModel = viewModel(repository, analyticsReporter)
        val effects = mutableListOf<LoginEffect>()
        val job = launch { viewModel.effect.toList(effects) }

        viewModel.onIntent(LoginIntent.LoginClicked)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoggedIn)
        assertEquals(login_error_invalid_credentials, viewModel.state.value.errorMessage)
        assertTrue(effects.any { it is LoginEffect.ShowError })
        assertEquals(
            listOf<Triple<KeyFlow, FlowOutcome, FailureCategory?>>(
                Triple(KeyFlow.LOGIN, FlowOutcome.FAILURE, FailureCategory.INVALID_CREDENTIALS)
            ),
            analyticsReporter.trackedEvents,
        )
        job.cancel()
    }

    @Test
    fun `RememberSessionToggled updates the state`() = runTest {
        val viewModel = viewModel(FakeAuthRepository(), FakeAnalyticsReporter())

        viewModel.onIntent(LoginIntent.RememberSessionToggled(true))

        assertTrue(viewModel.state.value.rememberSessionEnabled)
    }

    // The "keep session alive" checkbox must reach AuthRepository.login() as-is, whether checked
    // or not (the default), so a real login honors exactly what the user chose.
    @Test
    fun `LoginClicked passes rememberSessionEnabled through to login`() = runTest {
        val repository = FakeAuthRepository(loginResult = Result.success(testUser))
        val viewModel = viewModel(repository, FakeAnalyticsReporter())
        viewModel.onIntent(LoginIntent.RememberSessionToggled(true))

        viewModel.onIntent(LoginIntent.LoginClicked)
        advanceUntilIdle()

        assertEquals(true, repository.lastRememberSessionArg)
    }

    @Test
    fun `LoginClicked without checking remember session passes false through to login`() = runTest {
        val repository = FakeAuthRepository(loginResult = Result.success(testUser))
        val viewModel = viewModel(repository, FakeAnalyticsReporter())

        viewModel.onIntent(LoginIntent.LoginClicked)
        advanceUntilIdle()

        assertEquals(false, repository.lastRememberSessionArg)
    }

    // The dev bypass never calls AuthRepository.login() (no network involved), so it must apply
    // the checkbox itself via SetSessionRememberedUseCase to honor the same preference.
    @Test
    fun `dev admin bypass with remember session checked persists a session marker for the dev email`() = runTest {
        val repository = FakeAuthRepository(loginResult = Result.failure(AuthError.InvalidCredentials))
        val viewModel = viewModel(repository, FakeAnalyticsReporter())
        viewModel.onIntent(LoginIntent.EmailChanged("admin@admin.com"))
        viewModel.onIntent(LoginIntent.PasswordChanged("Admin@2026"))
        viewModel.onIntent(LoginIntent.RememberSessionToggled(true))

        viewModel.onIntent(LoginIntent.LoginClicked)
        advanceUntilIdle()

        assertEquals("admin@admin.com", repository.lastRememberedEmail)
        assertEquals(0, repository.clearRememberedSessionCallCount)
    }

    @Test
    fun `dev admin bypass without remember session checked clears any session marker`() = runTest {
        val repository = FakeAuthRepository(loginResult = Result.failure(AuthError.InvalidCredentials))
        val viewModel = viewModel(repository, FakeAnalyticsReporter())
        viewModel.onIntent(LoginIntent.EmailChanged("admin@admin.com"))
        viewModel.onIntent(LoginIntent.PasswordChanged("Admin@2026"))

        viewModel.onIntent(LoginIntent.LoginClicked)
        advanceUntilIdle()

        assertEquals(1, repository.clearRememberedSessionCallCount)
        assertEquals(null, repository.lastRememberedEmail)
    }
}
