package com.connectapp.domain.usecase

import com.connectapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class ObserveProfileFetchedAtUseCase(
    private val repository: AuthRepository,
) {
    operator fun invoke(): Flow<Long?> = repository.observeProfileFetchedAt()
}
