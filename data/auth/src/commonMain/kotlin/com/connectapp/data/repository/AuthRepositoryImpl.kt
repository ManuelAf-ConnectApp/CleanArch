package com.connectapp.data.repository

import com.connectapp.core.database.currentEpochMillis
import com.connectapp.data.datasource.AuthLocalDataSource
import com.connectapp.data.datasource.AuthRemoteDataSource
import com.connectapp.data.datasource.ProfileLocalDataSource
import com.connectapp.data.mapper.toDomain
import com.connectapp.domain.model.User
import com.connectapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val authLocalDataSource: AuthLocalDataSource,
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val profileLocalDataSource: ProfileLocalDataSource,
) : AuthRepository {
    override suspend fun login(
        email: String,
        password: String,
        rememberSession: Boolean,
    ): Result<User> {
        // No fabricated/cached User is ever returned here: this method always requires real
        // credentials and always returns the real result of trying them against the backend.
        // "Resume session without re-entering credentials" (spec.md US1 Acceptance Scenario 2) is
        // handled in `presentation` (splash/startup) by checking AuthLocalDataSource token
        // presence to decide whether to skip the Login screen — not by fabricating a User here.
        return authRemoteDataSource.login(email, password).map {
            it.toDomain()
        }.onSuccess { user ->
            // Seeds the offline profile cache — see
            // specs/010-offline-cache-layer/contracts/auth-repository-profile-logout-contract.md
            // (research.md D6: this is the only place the cache is ever written, there is no
            // network "refresh profile" call).
            profileLocalDataSource.upsert(
                firstName = user.firstName,
                lastName = user.lastName,
                email = user.email,
                phone = user.phone,
                fetchedAt = currentEpochMillis(),
            )
            // "Keep session alive" checkbox: only a remembered login leaves a session marker
            // behind, so a login without it can never inherit a stale marker from a previous
            // remembered session.
            if (rememberSession) persistRememberedSession(user.email) else clearRememberedSession()
        }
    }

    override fun persistRememberedSession(email: String) {
        authLocalDataSource.saveToken(email)
    }

    override fun clearRememberedSession() {
        authLocalDataSource.clearSession()
    }

    override fun observeProfile(): Flow<User?> =
        profileLocalDataSource.observe().map { it?.toDomain() }

    override suspend fun updateProfile(user: User): Result<Unit> = runCatching {
        profileLocalDataSource.upsert(
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
            phone = user.phone,
            fetchedAt = currentEpochMillis(),
        )
    }

    override fun logout() {
        authLocalDataSource.clearSession()
        profileLocalDataSource.clear()
    }

    override fun observeProfileFetchedAt(): Flow<Long?> =
        profileLocalDataSource.observe().map { it?.fetchedAt }

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<Boolean> {
        return authRemoteDataSource.register(
            name = name,
            email = email,
            password = password
        )
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        return authRemoteDataSource.forgotPassword(email)
    }

    override suspend fun hasActiveSession(): Boolean {
        return authLocalDataSource.getToken() != null
    }

}