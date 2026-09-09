package com.connectapp.presentation.forgot_password.fake

import com.connectapp.domain.model.User
import com.connectapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

internal class FakeAuthRepository(
    private val forgotPasswordResult: Result<Unit> = Result.success(Unit),
) : AuthRepository {
    override suspend fun login(email: String, password: String, rememberSession: Boolean): Result<User> {
        error("Not used by ForgotPasswordViewModelTest")
    }

    override suspend fun register(name: String, email: String, password: String): Result<Boolean> {
        error("Not used by ForgotPasswordViewModelTest")
    }

    override suspend fun forgotPassword(email: String): Result<Unit> = forgotPasswordResult

    override suspend fun hasActiveSession(): Boolean {
        error("Not used by ForgotPasswordViewModelTest")
    }

    override fun persistRememberedSession(email: String) {
        error("Not used by ForgotPasswordViewModelTest")
    }

    override fun clearRememberedSession() {
        error("Not used by ForgotPasswordViewModelTest")
    }

    override fun observeProfile(): Flow<User?> {
        error("Not used by ForgotPasswordViewModelTest")
    }

    override fun logout() {
        error("Not used by ForgotPasswordViewModelTest")
    }

    override fun observeProfileFetchedAt(): Flow<Long?> {
        error("Not used by ForgotPasswordViewModelTest")
    }

    override suspend fun updateProfile(user: User): Result<Unit> {
        error("Not used by ForgotPasswordViewModelTest")
    }
}
