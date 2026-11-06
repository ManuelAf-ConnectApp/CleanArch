package com.connectapp.presentation.login

import org.jetbrains.compose.resources.StringResource

data class LoginState(
    val email: String = "demo@restaurantapp.local",
    val password: String = "demo1234",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val rememberSessionEnabled: Boolean = false,
    val errorMessage: StringResource? = null
)
