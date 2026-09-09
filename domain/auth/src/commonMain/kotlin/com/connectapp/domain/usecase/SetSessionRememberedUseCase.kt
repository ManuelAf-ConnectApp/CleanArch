package com.connectapp.domain.usecase

import com.connectapp.domain.repository.AuthRepository

/**
 * Applies the "keep session alive" preference outside of a real [LoginUseCase] call — used by
 * LoginViewModel's temporary DEV_ADMIN_BYPASS path (no backend/login call involved), so it honors
 * the same preference a real login would via [AuthRepository.login]'s `rememberSession` param.
 */
class SetSessionRememberedUseCase(
    private val repository: AuthRepository,
) {
    operator fun invoke(remembered: Boolean, email: String) {
        if (remembered) repository.persistRememberedSession(email) else repository.clearRememberedSession()
    }
}
