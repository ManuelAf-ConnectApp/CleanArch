package com.connectapp.domain.usecase

import com.connectapp.domain.repository.BookingsRepository

class RefreshBookingsUseCase(
    private val repository: BookingsRepository,
) {
    suspend operator fun invoke(): Result<Unit> = repository.refreshBookings()
}
