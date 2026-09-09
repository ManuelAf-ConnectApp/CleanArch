package com.connectapp.presentation.forgot_password

import androidx.lifecycle.viewModelScope
import com.connectapp.commonresources.error_empty_email
import com.connectapp.commonresources.error_network
import com.connectapp.commonresources.error_server
import com.connectapp.commonresources.error_unexpected
import com.connectapp.commonresources.forgot_password_error_check_email
import com.connectapp.core.mvi.MviViewModel
import com.connectapp.domain.model.AuthError
import com.connectapp.domain.usecase.ForgotPasswordUseCase
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

class ForgotPasswordViewModel(
    private val forgotPasswordUseCase: ForgotPasswordUseCase
) : MviViewModel<ForgotPasswordState, ForgotPasswordIntent, ForgotPasswordEffect>(ForgotPasswordState()) {

    override fun reduce(intent: ForgotPasswordIntent) {
        when (intent) {
            is ForgotPasswordIntent.EmailChanged -> updateState { it.copy(email = intent.email) }
            ForgotPasswordIntent.SendEmailClicked -> sendEmail()
            ForgotPasswordIntent.BackToLoginClicked -> emitEffect(ForgotPasswordEffect.NavigateToLogin)
        }
    }

    private fun sendEmail() {
        if (state.value.email.isBlank()) {
            updateState { it.copy(errorMessage = error_empty_email) }
            return
        }

        updateState { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            forgotPasswordUseCase(state.value.email)
                .onSuccess {
                    updateState { it.copy(isLoading = false, isEmailSent = true) }
                }
                .onFailure { error ->
                    val message = error.toForgotPasswordErrorMessage()
                    updateState { it.copy(isLoading = false, errorMessage = message) }
                }
        }
    }

    private fun Throwable.toForgotPasswordErrorMessage(): StringResource = when (this) {
        // Per spec.md Assumptions (Historia 3): industry-standard privacy policy is to
        // neither confirm nor deny account existence, so this intentionally reuses the
        // same neutral copy shown on success rather than a distinct "account not found"
        // message (unlike Login/Register's analogous failure cases).
        is AuthError.AccountNotFound -> forgot_password_error_check_email
        is AuthError.Network -> error_network
        is AuthError.Server -> error_server
        is AuthError.InvalidCredentials -> error_unexpected
        is AuthError.EmailAlreadyRegistered -> error_unexpected
        is AuthError.SessionExpired -> error_unexpected
        is AuthError.Unknown -> error_unexpected
        else -> error_unexpected
    }
}
