package com.connectapp.data.orders.model

import kotlinx.serialization.Serializable

@Serializable
data class OrderDto(
    val id: String,
    val title: String,
    val status: String,
    val date: String,
)
