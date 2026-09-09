package com.connectapp.domain.repository

import com.connectapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /**
     * [rememberSession] controls whether a successful login persists the local session marker
     * (so [hasActiveSession] returns true on a later app launch) or leaves it cleared — see
     * [rememberSession] (the function below) / [forgetSession]. Defaults to `false` (today's
     * behavior: every launch requires a fresh login) for callers that don't care.
     */
    suspend fun login(email: String, password: String, rememberSession: Boolean = false): Result<User>
    suspend fun register(name: String, email: String, password: String): Result<Boolean>
    suspend fun forgotPassword(email: String): Result<Unit>

    /**
     * Whether a local session token is present.
     *
     * This is a local presence check only — it does not validate the token against a
     * backend (out of scope per FR-010, no backend connected). Used by Splash routing to
     * decide whether the user can skip Login (spec.md, User Story 1, Acceptance Scenario 2).
     */
    suspend fun hasActiveSession(): Boolean

    /**
     * Persists the local "keep session alive" marker for [email], independent of a real login
     * network call. [login] calls this internally when `rememberSession` is true; also called
     * directly by LoginViewModel's temporary DEV_ADMIN_BYPASS path (no backend call involved),
     * so both paths honor the same preference. Never touches the cached profile — see [logout].
     */
    fun persistRememberedSession(email: String)

    /**
     * Clears the local "keep session alive" marker without touching the cached profile — the
     * counterpart to [persistRememberedSession]. [login] calls this when `rememberSession` is
     * false, so a login without the checkbox never leaves a stale marker from a previous
     * remembered session.
     */
    fun clearRememberedSession()

    /**
     * See specs/010-offline-cache-layer/contracts/auth-repository-profile-logout-contract.md.
     * Emits the [User] cached at the last successful [login], or `null` if none is cached (never
     * calls the network — there is no profile-fetch endpoint to call, research.md D6).
     */
    fun observeProfile(): Flow<User?>

    /**
     * Overwrites the cached profile with [user] — same cache [observeProfile] reads from
     * (specs/008-complete-edit-profile-privacy/research.md D1), never a second store. Never
     * touches the network (no backend profile-update endpoint — spec.md Assumptions).
     */
    suspend fun updateProfile(user: User): Result<Unit>

    /** Clears the session token and the cached profile. Never fails, never touches the network. */
    fun logout()

    /**
     * When the cached profile was last successfully synced (i.e. the last successful [login]),
     * or `null` if nothing is cached. See specs/010-offline-cache-layer/spec.md User Story 3.
     */
    fun observeProfileFetchedAt(): Flow<Long?>
}
