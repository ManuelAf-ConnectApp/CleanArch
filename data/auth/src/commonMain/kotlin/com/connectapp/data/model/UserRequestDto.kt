package com.connectapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserRequestDto(
    val email: String,
    val password: String
)
