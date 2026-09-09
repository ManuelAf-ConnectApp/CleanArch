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
import com.connectapp.domain.model.User
import com.connectapp.domain.usecase.LoginUseCase
import com.connectapp.domain.usecase.SetSessionRememberedUseCase
import com.connectapp.domain.usecase.TrackFlowEventUseCase
import com.connectapp.domain.usecase.UpdateProfileUseCase
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val trackFlowEventUseCase: TrackFlowEventUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val setSessionRememberedUseCase: SetSessionRememberedUseCase,
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
            // TEMPORARY DEV BYPASS — no backend is deployed for this project (README/AppConfig).
            // A single fixed local account logs in without calling loginUseCase, so the rest of
            // the app can be exercised end to end. Every other credential still goes through the
            // real LoginUseCase below and gets the real success/error result — see
            // specs/006-remove-viewmodel-dev-shortcuts for why an unconditional bypass was
            // removed; this one is scoped to one fixed credential pair instead of every attempt.
            // Remove this block once a real backend is configured (AuthRepositoryImpl.login()
            // and its tests are unchanged and still never fabricate a user).
            if (state.value.email == DEV_ADMIN_EMAIL && state.value.password == DEV_ADMIN_PASSWORD) {
                // Seeds the same profile cache a real login would (AuthRepositoryImpl.login()),
                // so Profile/EditProfile have something to show instead of "user not found" —
                // updateProfile() is the same write path EditProfileViewModel already uses, not
                // a second/new persistence mechanism.
                updateProfileUseCase(DEV_ADMIN_USER)
                // This path never calls loginUseCase (no network involved), so it applies the
                // "keep session alive" checkbox itself — same preference a real login honors.
                setSessionRememberedUseCase(state.value.rememberSessionEnabled, DEV_ADMIN_EMAIL)
                updateState { it.copy(isLoading = false, isLoggedIn = true, errorMessage = null) }
                emitEffect(LoginEffect.NavigateToHome)
                return@launch
            }

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

    private companion object {
        const val DEV_ADMIN_EMAIL = "admin@admin.com"
        const val DEV_ADMIN_PASSWORD = "Admin@2026"
        val DEV_ADMIN_USER = User(
            firstName = "Admin",
            lastName = "Dev",
            email = DEV_ADMIN_EMAIL,
            phone = "+1 555 0100",
        )
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
