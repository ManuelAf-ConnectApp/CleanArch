package com.connectapp.presentation.register.fake

import com.connectapp.domain.model.User
import com.connectapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

internal class FakeAuthRepository(
    private val registerResult: Result<Boolean> = Result.success(true),
) : AuthRepository {
    override suspend fun login(email: String, password: String, rememberSession: Boolean): Result<User> {
        error("Not used by RegisterViewModelTest")
    }

    override suspend fun register(name: String, email: String, password: String): Result<Boolean> = registerResult

    override suspend fun forgotPassword(email: String): Result<Unit> {
        error("Not used by RegisterViewModelTest")
    }

    override suspend fun hasActiveSession(): Boolean {
        error("Not used by RegisterViewModelTest")
    }

    override fun persistRememberedSession(email: String) {
        error("Not used by RegisterViewModelTest")
    }

    override fun clearRememberedSession() {
        error("Not used by RegisterViewModelTest")
    }

    override fun observeProfile(): Flow<User?> {
        error("Not used by RegisterViewModelTest")
    }

    override fun logout() {
        error("Not used by RegisterViewModelTest")
    }

    override fun observeProfileFetchedAt(): Flow<Long?> {
        error("Not used by RegisterViewModelTest")
    }

    override suspend fun updateProfile(user: User): Result<Unit> {
        error("Not used by RegisterViewModelTest")
    }
}
