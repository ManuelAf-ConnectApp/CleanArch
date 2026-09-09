package com.connectapp.presentation.login

import org.jetbrains.compose.resources.StringResource

sealed interface LoginEffect {
    data class ShowError(val message: StringResource) : LoginEffect
    object NavigateToHome : LoginEffect
    object NavigateToRegister : LoginEffect
    object NavigateToForgotPassword : LoginEffect
}
