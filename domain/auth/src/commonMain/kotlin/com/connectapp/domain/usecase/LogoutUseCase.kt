package com.connectapp.domain.usecase

import com.connectapp.domain.repository.AuthRepository
import com.connectapp.domain.repository.OrdersRepository

/**
 * The only place in `domain` that knows "logging out" touches more than one repository's cache —
 * see specs/010-offline-cache-layer/contracts/auth-repository-profile-logout-contract.md
 * (research.md D7: repositories only ever clear their own cache, orchestration lives here).
 */
class LogoutUseCase(
    private val authRepository: AuthRepository,
    private val ordersRepository: OrdersRepository,
) {
    suspend operator fun invoke() {
        authRepository.logout()
        ordersRepository.clearCache()
    }
}
