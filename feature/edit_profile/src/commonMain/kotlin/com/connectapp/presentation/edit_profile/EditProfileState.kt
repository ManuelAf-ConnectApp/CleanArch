package com.connectapp.presentation.edit_profile

import org.jetbrains.compose.resources.StringResource

data class EditProfileState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val isLoading: Boolean = false,
    val errorMessage: StringResource? = null,
)
