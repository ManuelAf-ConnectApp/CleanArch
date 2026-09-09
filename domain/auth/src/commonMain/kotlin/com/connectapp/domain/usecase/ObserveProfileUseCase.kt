package com.connectapp.domain.usecase

import com.connectapp.domain.model.User
import com.connectapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class ObserveProfileUseCase(
    private val repository: AuthRepository,
) {
    operator fun invoke(): Flow<User?> = repository.observeProfile()
}
