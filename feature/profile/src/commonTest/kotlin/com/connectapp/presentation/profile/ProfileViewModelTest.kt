package com.connectapp.presentation.profile

import com.connectapp.domain.model.Order
import com.connectapp.domain.model.User
import com.connectapp.domain.repository.AuthRepository
import com.connectapp.domain.repository.OrdersRepository
import com.connectapp.domain.usecase.LogoutUseCase
import com.connectapp.domain.usecase.ObserveProfileFetchedAtUseCase
import com.connectapp.domain.usecase.ObserveProfileUseCase
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** No login/register/forgotPassword/hasActiveSession call is exercised by ProfileViewModel. */
private class FakeAuthRepository(user: User?) : AuthRepository {
    private val profileFlow = MutableStateFlow(user)

    var logoutCallCount: Int = 0
        private set

    override suspend fun login(email: String, password: String, rememberSession: Boolean): Result<User> =
        error("Not used by ProfileViewModelTest")
    override suspend fun register(name: String, email: String, password: String): Result<Boolean> =
        error("Not used by ProfileViewModelTest")
    override suspend fun forgotPassword(email: String): Result<Unit> = error("Not used by ProfileViewModelTest")
    override suspend fun hasActiveSession(): Boolean = error("Not used by ProfileViewModelTest")
    override fun persistRememberedSession(email: String) = error("Not used by ProfileViewModelTest")
    override fun clearRememberedSession() = error("Not used by ProfileViewModelTest")

    override fun observeProfile(): Flow<User?> = profileFlow

    override fun logout() {
        logoutCallCount++
        profileFlow.value = null
    }

    override fun observeProfileFetchedAt(): Flow<Long?> = MutableStateFlow(null)

    override suspend fun updateProfile(user: User): Result<Unit> = error("Not used by ProfileViewModelTest")
}

/** ProfileViewModel doesn't read orders directly, but LogoutUseCase needs one to clear. */
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

private val SAMPLE_USER = User(firstName = "Grace", lastName = "Hopper", email = "grace@example.com", phone = "+1-555-0199")

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

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
        authRepository: FakeAuthRepository = FakeAuthRepository(SAMPLE_USER),
        ordersRepository: FakeOrdersRepository = FakeOrdersRepository(),
    ) = ProfileViewModel(
        ObserveProfileUseCase(authRepository),
        LogoutUseCase(authRepository, ordersRepository),
        ObserveProfileFetchedAtUseCase(authRepository),
    )

    @Test
    fun `initial load finishes with the cached user and clears isLoading`() = runTest {
        val viewModel = viewModel()

        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertNotNull(viewModel.state.value.user)
        assertEquals(SAMPLE_USER, viewModel.state.value.user)
    }

    @Test
    fun `LogoutClicked invokes LogoutUseCase clears loading and emits NavigateToLogin`() = runTest {
        val authRepository = FakeAuthRepository(SAMPLE_USER)
        val ordersRepository = FakeOrdersRepository()
        val viewModel = viewModel(authRepository, ordersRepository)
        advanceUntilIdle()
        val effects = mutableListOf<ProfileEffect>()
        val job = launch { viewModel.effect.toList(effects) }

        viewModel.onIntent(ProfileIntent.LogoutClicked)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertTrue(effects.any { it is ProfileEffect.NavigateToLogin })
        assertEquals(1, authRepository.logoutCallCount)
        assertEquals(1, ordersRepository.clearCacheCallCount)
        job.cancel()
    }

    @Test
    fun `BackClicked emits NavigateBack`() = runTest {
        val viewModel = viewModel()
        val effects = mutableListOf<ProfileEffect>()
        val job = launch { viewModel.effect.toList(effects) }

        viewModel.onIntent(ProfileIntent.BackClicked)
        advanceUntilIdle()

        assertTrue(effects.any { it is ProfileEffect.NavigateBack })
        job.cancel()
    }
}
