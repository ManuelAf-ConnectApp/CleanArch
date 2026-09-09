package com.connectapp.domain.model

data class Order(
    val id: String,
    val title: String,
    val status: OrderStatus,
    val date: String,
)

enum class OrderStatus {
    PENDING,
    SHIPPED,
    DELIVERED,
}
