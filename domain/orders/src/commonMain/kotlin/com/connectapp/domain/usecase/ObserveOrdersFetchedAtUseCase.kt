package com.connectapp.domain.usecase

import com.connectapp.domain.repository.OrdersRepository
import kotlinx.coroutines.flow.Flow

class ObserveOrdersFetchedAtUseCase(
    private val repository: OrdersRepository,
) {
    operator fun invoke(): Flow<Long?> = repository.observeLastFetchedAt()
}
