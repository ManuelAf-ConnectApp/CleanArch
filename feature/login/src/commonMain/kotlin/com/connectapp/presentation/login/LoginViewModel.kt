package com.connectapp.presentation.login

import androidx.lifecycle.viewModelScope
import com.connectapp.commonresources.error_network
import com.connectapp.commonresources.error_server
import com.connectapp.commonresources.error_unexpected
import com.connectapp.commonresources.login_error_invalid_credentials
import com.connectapp.commonresources.login_error_session_expired
import com.connectapp.core.mvi.MviViewModel
import com.connectapp.domain.analytics.FailureCategory
import com.connectapp.domain.analytics.FlowOutcome
import com.connectapp.domain.analytics.KeyFlow
import com.connectapp.domain.model.AuthError
import com.connectapp.domain.usecase.LoginUseCase
import com.connectapp.domain.usecase.TrackFlowEventUseCase
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val trackFlowEventUseCase: TrackFlowEventUseCase,
) : MviViewModel<LoginState, LoginIntent, LoginEffect>(LoginState()) {

    override fun reduce(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.EmailChanged -> {
                updateState { it.copy(email = intent.email) }
            }

            is LoginIntent.PasswordChanged -> {
                updateState { it.copy(password = intent.password) }
            }

            LoginIntent.LoginClicked -> {
                login()
            }

            LoginIntent.RegisterClicked -> {
                emitEffect(LoginEffect.NavigateToRegister)
            }

            LoginIntent.TogglePasswordVisibility -> {
                updateState { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }

            is LoginIntent.RememberSessionToggled -> {
                updateState { it.copy(rememberSessionEnabled = intent.enabled) }
            }
        }
    }

    private fun login() {
        updateState { it.copy(isLoading = true) }
        viewModelScope.launch {
            loginUseCase(state.value.email, state.value.password, state.value.rememberSessionEnabled)
                .onSuccess {
                    updateState { it.copy(isLoading = false, isLoggedIn = true) }
                    trackFlowEventUseCase(KeyFlow.LOGIN, FlowOutcome.SUCCESS)
                    emitEffect(LoginEffect.NavigateToHome)
                }.onFailure { error ->
                    val message = error.toLoginErrorMessage()
                    updateState { it.copy(isLoading = false, errorMessage = message) }
                    trackFlowEventUseCase(KeyFlow.LOGIN, FlowOutcome.FAILURE, error.toFailureCategory())
                    emitEffect(LoginEffect.ShowError(message))
                }
        }
    }

    private fun Throwable.toLoginErrorMessage(): StringResource = when (this) {
        is AuthError.InvalidCredentials -> login_error_invalid_credentials
        is AuthError.Network -> error_network
        is AuthError.Server -> error_server
        is AuthError.SessionExpired -> login_error_session_expired
        is AuthError.EmailAlreadyRegistered -> error_unexpected
        is AuthError.AccountNotFound -> error_unexpected
        is AuthError.Unknown -> error_unexpected
        else -> error_unexpected
    }

    private fun Throwable.toFailureCategory(): FailureCategory = when (this) {
        is AuthError.InvalidCredentials -> FailureCategory.INVALID_CREDENTIALS
        is AuthError.Network -> FailureCategory.NETWORK
        is AuthError.Server -> FailureCategory.SERVER
        else -> FailureCategory.UNKNOWN
    }
}
