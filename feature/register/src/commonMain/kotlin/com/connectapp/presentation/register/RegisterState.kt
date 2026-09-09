package com.connectapp.presentation.register

import org.jetbrains.compose.resources.StringResource

data class RegisterState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isRegistered: Boolean = false,
    val errorMessage: StringResource? = null
)
