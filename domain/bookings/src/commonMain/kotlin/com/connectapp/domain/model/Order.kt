package com.connectapp.domain.model

data class Booking(
    val id: String,
    val title: String,
    val status: BookingStatus,
    val date: String,
    val guests: Int = 2,
    val totalCents: Long = 0,
    val paymentMethod: DemoPaymentMethod = DemoPaymentMethod.CARD,
    val cancellationPolicy: CancellationPolicy = CancellationPolicy.freeUntilHoursBefore(24),
    val cancellationAllowed: Boolean = true,
)

val Booking.canBeCancelled: Boolean
    get() = status == BookingStatus.CONFIRMED && cancellationAllowed

enum class BookingStatus {
    CONFIRMED,
    CANCELLED,
}

enum class DemoPaymentMethod {
    CARD,
    PAYPAL,
    GOOGLE_PAY,
    APPLE_PAY,
}

data class CancellationPolicy(
    val freeCancellationUntilHoursBefore: Int,
) {
    init {
        require(freeCancellationUntilHoursBefore >= 0)
    }

    companion object {
        fun freeUntilHoursBefore(hours: Int): CancellationPolicy = CancellationPolicy(hours)
    }
}
