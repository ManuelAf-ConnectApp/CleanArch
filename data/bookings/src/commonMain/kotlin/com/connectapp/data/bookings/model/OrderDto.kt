package com.connectapp.data.bookings.model

import kotlinx.serialization.Serializable

@Serializable
data class OrderDto(
    val id: String,
    val title: String,
    val status: String,
    val date: String,
    val guests: Int = 2,
    val totalCents: Long = 0,
    val paymentMethod: String = "CARD",
    val cancellationWindowHours: Int = 24,
)
