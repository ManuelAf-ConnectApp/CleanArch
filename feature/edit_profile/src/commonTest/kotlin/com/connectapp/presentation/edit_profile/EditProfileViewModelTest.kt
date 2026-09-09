package com.connectapp.presentation.edit_profile

import com.connectapp.domain.model.User
import com.connectapp.domain.repository.AuthRepository
import com.connectapp.domain.usecase.ObserveProfileUseCase
import com.connectapp.domain.usecase.UpdateProfileUseCase
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeAuthRepository(
    initialUser: User?,
    private val updateProfileResult: Result<Unit> = Result.success(Unit),
) : AuthRepository {
    private val profileFlow = MutableStateFlow(initialUser)

    var lastUpdatedProfile: User? = null
        private set

    override suspend fun login(email: String, password: String, rememberSession: Boolean): Result<User> = error("Not used")
    override suspend fun register(name: String, email: String, password: String): Result<Boolean> = error("Not used")
    override suspend fun forgotPassword(email: String): Result<Unit> = error("Not used")
    override suspend fun hasActiveSession(): Boolean = error("Not used")
    override fun persistRememberedSession(email: String) = error("Not used")
    override fun clearRememberedSession() = error("Not used")
    override fun observeProfile(): Flow<User?> = profileFlow
    override fun logout() = error("Not used")
    override fun observeProfileFetchedAt(): Flow<Long?> = MutableStateFlow(null)

    override suspend fun updateProfile(user: User): Result<Unit> {
        lastUpdatedProfile = user
        if (updateProfileResult.isSuccess) profileFlow.value = user
        return updateProfileResult
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class EditProfileViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val currentUser = User(firstName = "Jane", lastName = "Doe", email = "jane@example.com", phone = "+1")

    private fun viewModel(repository: AuthRepository) =
        EditProfileViewModel(ObserveProfileUseCase(repository), UpdateProfileUseCase(repository))

    @Test
    fun `seeds the form once with the current profile`() = runTest {
        val viewModel = viewModel(FakeAuthRepository(currentUser))

        advanceUntilIdle()

        assertEquals(currentUser.firstName, viewModel.state.value.firstName)
        assertEquals(currentUser.lastName, viewModel.state.value.lastName)
        assertEquals(currentUser.email, viewModel.state.value.email)
        assertEquals(currentUser.phone, viewModel.state.value.phone)
    }

    @Test
    fun `SaveClicked with a blank required field shows an error and does not save`() = runTest {
        val repository = FakeAuthRepository(currentUser)
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.onIntent(EditProfileIntent.FirstNameChanged(""))
        viewModel.onIntent(EditProfileIntent.SaveClicked)
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.errorMessage)
        assertEquals(null, repository.lastUpdatedProfile)
    }

    @Test
    fun `SaveClicked with valid fields saves and emits NavigateToProfile`() = runTest {
        val repository = FakeAuthRepository(currentUser)
        val viewModel = viewModel(repository)
        advanceUntilIdle()
        val effects = mutableListOf<EditProfileEffect>()
        val job = launch { viewModel.effect.toList(effects) }

        viewModel.onIntent(EditProfileIntent.FirstNameChanged("Janet"))
        viewModel.onIntent(EditProfileIntent.SaveClicked)
        advanceUntilIdle()

        assertEquals("Janet", repository.lastUpdatedProfile?.firstName)
        assertTrue(effects.any { it is EditProfileEffect.NavigateToProfile })
        assertFalse(viewModel.state.value.isLoading)
        job.cancel()
    }

    @Test
    fun `CancelClicked emits NavigateBack without saving`() = runTest {
        val repository = FakeAuthRepository(currentUser)
        val viewModel = viewModel(repository)
        advanceUntilIdle()
        val effects = mutableListOf<EditProfileEffect>()
        val job = launch { viewModel.effect.toList(effects) }

        viewModel.onIntent(EditProfileIntent.FirstNameChanged("Janet"))
        viewModel.onIntent(EditProfileIntent.CancelClicked)
        advanceUntilIdle()

        assertNull(repository.lastUpdatedProfile)
        assertTrue(effects.any { it is EditProfileEffect.NavigateBack })
        job.cancel()
    }
}
