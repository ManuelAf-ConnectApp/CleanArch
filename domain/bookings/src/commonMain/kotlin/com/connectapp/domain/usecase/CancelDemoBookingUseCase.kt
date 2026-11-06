package com.connectapp.domain.usecase

import com.connectapp.domain.repository.BookingsRepository

/** Marks a locally simulated reservation as cancelled after the user confirms the action. */
class CancelDemoBookingUseCase(
    private val repository: BookingsRepository,
) {
    suspend operator fun invoke(bookingId: String): Result<Unit> = repository.cancelDemoBooking(bookingId)
}
