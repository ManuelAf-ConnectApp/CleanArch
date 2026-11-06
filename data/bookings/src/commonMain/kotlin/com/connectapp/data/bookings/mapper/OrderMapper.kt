package com.connectapp.data.bookings.mapper

import com.connectapp.data.bookings.database.CachedOrderEntity
import com.connectapp.data.bookings.model.OrderDto
import com.connectapp.domain.model.Booking
import com.connectapp.domain.model.BookingStatus
import com.connectapp.domain.model.CancellationPolicy
import com.connectapp.domain.model.DemoPaymentMethod

fun OrderDto.toDomain(): Booking = Booking(
    id = id,
    title = title,
    status = status.toOrderStatus(),
    date = date,
    guests = guests,
    totalCents = totalCents,
    paymentMethod = paymentMethod.toPaymentMethod(),
    cancellationPolicy = CancellationPolicy.freeUntilHoursBefore(cancellationWindowHours),
)

fun CachedOrderEntity.toDomain(): Booking = Booking(
    id = id,
    title = title,
    status = status.toOrderStatus(),
    date = date,
    guests = guests.toInt(),
    totalCents = totalCents,
    paymentMethod = paymentMethod.toPaymentMethod(),
    cancellationPolicy = CancellationPolicy.freeUntilHoursBefore(cancellationWindowHours.toInt()),
)

fun Booking.toCacheEntity(fetchedAt: Long): CachedOrderEntity = CachedOrderEntity(
    id = id,
    title = title,
    status = status.name,
    date = date,
    guests = guests.toLong(),
    totalCents = totalCents,
    paymentMethod = paymentMethod.name,
    cancellationWindowHours = cancellationPolicy.freeCancellationUntilHoursBefore.toLong(),
    fetchedAt = fetchedAt,
)

private fun String.toOrderStatus(): BookingStatus =
    BookingStatus.entries.firstOrNull { it.name.equals(this, ignoreCase = true) } ?: BookingStatus.CONFIRMED

private fun String.toPaymentMethod(): DemoPaymentMethod =
    DemoPaymentMethod.entries.firstOrNull { it.name.equals(this, ignoreCase = true) } ?: DemoPaymentMethod.CARD
