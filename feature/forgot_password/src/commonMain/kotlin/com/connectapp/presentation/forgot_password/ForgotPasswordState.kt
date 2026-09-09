package com.connectapp.presentation.forgot_password

import org.jetbrains.compose.resources.StringResource

data class ForgotPasswordState(
    val email: String = "",
    val isLoading: Boolean = false,
    val isEmailSent: Boolean = false,
    val errorMessage: StringResource? = null
)
