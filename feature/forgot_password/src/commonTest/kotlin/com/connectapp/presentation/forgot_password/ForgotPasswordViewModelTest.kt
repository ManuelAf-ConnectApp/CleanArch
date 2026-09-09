package com.connectapp.presentation.forgot_password

import com.connectapp.domain.model.AuthError
import com.connectapp.domain.usecase.ForgotPasswordUseCase
import com.connectapp.presentation.forgot_password.fake.FakeAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `SendEmailClicked with repository success marks the email as sent`() = runTest {
        val viewModel = ForgotPasswordViewModel(ForgotPasswordUseCase(FakeAuthRepository(Result.success(Unit))))

        viewModel.onIntent(ForgotPasswordIntent.EmailChanged("jane@example.com"))
        viewModel.onIntent(ForgotPasswordIntent.SendEmailClicked)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isEmailSent)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `SendEmailClicked with repository failure shows error and does not mark as sent`() = runTest {
        val viewModel = ForgotPasswordViewModel(
            ForgotPasswordUseCase(FakeAuthRepository(Result.failure(AuthError.Server(500))))
        )

        viewModel.onIntent(ForgotPasswordIntent.EmailChanged("jane@example.com"))
        viewModel.onIntent(ForgotPasswordIntent.SendEmailClicked)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isEmailSent)
        assertFalse(viewModel.state.value.isLoading)
        assertNotNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `SendEmailClicked with blank email does not call the use case`() = runTest {
        val viewModel = ForgotPasswordViewModel(ForgotPasswordUseCase(FakeAuthRepository(Result.success(Unit))))

        viewModel.onIntent(ForgotPasswordIntent.SendEmailClicked)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isEmailSent)
        assertNotNull(viewModel.state.value.errorMessage)
    }
}
