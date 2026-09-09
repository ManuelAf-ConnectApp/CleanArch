package com.connectapp.data.orders.mapper

import com.connectapp.data.orders.database.CachedOrderEntity
import com.connectapp.data.orders.model.OrderDto
import com.connectapp.domain.model.Order
import com.connectapp.domain.model.OrderStatus

fun OrderDto.toDomain(): Order = Order(
    id = id,
    title = title,
    status = status.toOrderStatus(),
    date = date,
)

fun CachedOrderEntity.toDomain(): Order = Order(
    id = id,
    title = title,
    status = status.toOrderStatus(),
    date = date,
)

fun Order.toCacheEntity(fetchedAt: Long): CachedOrderEntity = CachedOrderEntity(
    id = id,
    title = title,
    status = status.name,
    date = date,
    fetchedAt = fetchedAt,
)

private fun String.toOrderStatus(): OrderStatus =
    OrderStatus.entries.firstOrNull { it.name.equals(this, ignoreCase = true) } ?: OrderStatus.PENDING
