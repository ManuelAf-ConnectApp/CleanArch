package com.connectapp.domain.usecase

import com.connectapp.domain.model.Booking
import com.connectapp.domain.repository.BookingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveBookingsUseCase(
    private val repository: BookingsRepository,
) {
    operator fun invoke(): Flow<List<Booking>> = repository.observeBookings()
}
