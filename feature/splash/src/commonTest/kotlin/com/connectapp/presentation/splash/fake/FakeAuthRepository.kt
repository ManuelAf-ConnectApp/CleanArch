package com.connectapp.presentation.splash.fake

import com.connectapp.domain.model.User
import com.connectapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

internal class FakeAuthRepository(
    private val hasActiveSessionResult: Boolean,
) : AuthRepository {
    override suspend fun login(email: String, password: String, rememberSession: Boolean): Result<User> {
        error("Not used by SplashViewModelTest")
    }

    override suspend fun register(name: String, email: String, password: String): Result<Boolean> {
        error("Not used by SplashViewModelTest")
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        error("Not used by SplashViewModelTest")
    }

    override suspend fun hasActiveSession(): Boolean = hasActiveSessionResult

    override fun persistRememberedSession(email: String) {
        error("Not used by SplashViewModelTest")
    }

    override fun clearRememberedSession() {
        error("Not used by SplashViewModelTest")
    }

    override fun observeProfile(): Flow<User?> {
        error("Not used by SplashViewModelTest")
    }

    override fun logout() {
        error("Not used by SplashViewModelTest")
    }

    override fun observeProfileFetchedAt(): Flow<Long?> {
        error("Not used by SplashViewModelTest")
    }

    override suspend fun updateProfile(user: User): Result<Unit> {
        error("Not used by SplashViewModelTest")
    }
}
