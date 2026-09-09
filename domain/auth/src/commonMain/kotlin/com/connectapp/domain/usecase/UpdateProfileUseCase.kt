package com.connectapp.domain.usecase

import com.connectapp.domain.model.User
import com.connectapp.domain.repository.AuthRepository

class UpdateProfileUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(user: User): Result<Unit> = authRepository.updateProfile(user)
}
