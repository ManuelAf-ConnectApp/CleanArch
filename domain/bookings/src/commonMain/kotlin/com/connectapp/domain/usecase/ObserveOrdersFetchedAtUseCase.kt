package com.connectapp.domain.usecase

import com.connectapp.domain.repository.BookingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveBookingsFetchedAtUseCase(
    private val repository: BookingsRepository,
) {
    operator fun invoke(): Flow<Long?> = repository.observeLastFetchedAt()
}
