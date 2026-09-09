package com.connectapp.presentation.register

import androidx.lifecycle.viewModelScope
import com.connectapp.commonresources.error_network
import com.connectapp.commonresources.error_passwords_not_match
import com.connectapp.commonresources.error_server
import com.connectapp.commonresources.error_unexpected
import com.connectapp.commonresources.register_error_email_already_registered
import com.connectapp.core.mvi.MviViewModel
import com.connectapp.domain.model.AuthError
import com.connectapp.domain.usecase.RegisterUseCase
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : MviViewModel<RegisterState, RegisterIntent, RegisterEffect>(RegisterState()) {

    override fun reduce(intent: RegisterIntent) {
        when (intent) {
            is RegisterIntent.NameChanged -> updateState { it.copy(name = intent.name) }
            is RegisterIntent.EmailChanged -> updateState { it.copy(email = intent.email) }
            is RegisterIntent.PasswordChanged -> updateState { it.copy(password = intent.password) }
            is RegisterIntent.ConfirmPasswordChanged -> updateState { it.copy(confirmPassword = intent.confirmPassword) }
            RegisterIntent.RegisterClicked -> register()
            RegisterIntent.LoginClicked -> emitEffect(RegisterEffect.NavigateToLogin)
        }
    }

    private fun register() {
        if (state.value.password != state.value.confirmPassword) {
            updateState { it.copy(errorMessage = error_passwords_not_match) }
            return
        }

        updateState { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            registerUseCase(state.value.name, state.value.email, state.value.password)
                .onSuccess {
                    updateState { it.copy(isLoading = false, isRegistered = true) }
                    emitEffect(RegisterEffect.NavigateToHome)
                }
                .onFailure { error ->
                    val message = error.toRegisterErrorMessage()
                    updateState { it.copy(isLoading = false, errorMessage = message) }
                }
        }
    }

    private fun Throwable.toRegisterErrorMessage(): StringResource = when (this) {
        is AuthError.EmailAlreadyRegistered -> register_error_email_already_registered
        is AuthError.Network -> error_network
        is AuthError.Server -> error_server
        is AuthError.InvalidCredentials -> error_unexpected
        is AuthError.AccountNotFound -> error_unexpected
        is AuthError.SessionExpired -> error_unexpected
        is AuthError.Unknown -> error_unexpected
        else -> error_unexpected
    }
}
