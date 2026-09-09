package com.connectapp.domain.usecase

import com.connectapp.domain.repository.AuthRepository

/**
 * Checks whether a local session token is present, so Splash can skip Login for a
 * previously started and still-valid session (spec.md, User Story 1, Acceptance Scenario 2).
 *
 * This does not validate the session against a backend (out of scope per FR-010) — it is a
 * local presence check only.
 */
class GetSessionStatusUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): Boolean = repository.hasActiveSession()
}
