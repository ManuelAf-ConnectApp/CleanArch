package com.connectapp.presentation.register

import com.connectapp.domain.model.AuthError
import com.connectapp.domain.usecase.RegisterUseCase
import com.connectapp.presentation.register.fake.FakeAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun ViewModel(result: Result<Boolean>) = RegisterViewModel(RegisterUseCase(FakeAuthRepository(result)))

    private fun fillValidForm(viewModel: RegisterViewModel) {
        viewModel.onIntent(RegisterIntent.NameChanged("Jane Doe"))
        viewModel.onIntent(RegisterIntent.EmailChanged("jane@example.com"))
        viewModel.onIntent(RegisterIntent.PasswordChanged("secret123"))
        viewModel.onIntent(RegisterIntent.ConfirmPasswordChanged("secret123"))
    }

    @Test
    fun `RegisterClicked with valid data updates state and emits NavigateToHome`() = runTest {
        val viewModel = ViewModel(Result.success(true))
        val effects = mutableListOf<RegisterEffect>()
        val job = launch { viewModel.effect.toList(effects) }

        fillValidForm(viewModel)
        viewModel.onIntent(RegisterIntent.RegisterClicked)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isRegistered)
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(effects.any { it is RegisterEffect.NavigateToHome })
        job.cancel()
    }

    @Test
    fun `RegisterClicked with repository failure shows error and does not register`() = runTest {
        val viewModel = ViewModel(Result.failure(AuthError.EmailAlreadyRegistered))
        val effects = mutableListOf<RegisterEffect>()
        val job = launch { viewModel.effect.toList(effects) }

        fillValidForm(viewModel)
        viewModel.onIntent(RegisterIntent.RegisterClicked)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isRegistered)
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.errorMessage != null)
        job.cancel()
    }

    @Test
    fun `RegisterClicked with mismatched passwords does not call the use case`() = runTest {
        val viewModel = ViewModel(Result.success(true))

        viewModel.onIntent(RegisterIntent.NameChanged("Jane Doe"))
        viewModel.onIntent(RegisterIntent.EmailChanged("jane@example.com"))
        viewModel.onIntent(RegisterIntent.PasswordChanged("secret123"))
        viewModel.onIntent(RegisterIntent.ConfirmPasswordChanged("different"))
        viewModel.onIntent(RegisterIntent.RegisterClicked)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isRegistered)
        assertFalse(viewModel.state.value.isLoading)
    }
}
