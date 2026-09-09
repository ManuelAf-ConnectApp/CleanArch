package com.connectapp.presentation.login.fake

import com.connectapp.domain.model.User
import com.connectapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

internal class FakeAuthRepository(
    private val loginResult: Result<User> = Result.success(
        User(firstName = "Test", lastName = "User", email = "test@example.com", phone = "")
    ),
) : AuthRepository {
    var lastUpdatedProfile: User? = null
        private set
    var lastRememberSessionArg: Boolean? = null
        private set
    var lastRememberedEmail: String? = null
        private set
    var clearRememberedSessionCallCount: Int = 0
        private set

    override suspend fun login(email: String, password: String, rememberSession: Boolean): Result<User> {
        lastRememberSessionArg = rememberSession
        return loginResult
    }

    override suspend fun register(name: String, email: String, password: String): Result<Boolean> {
        error("Not used by LoginViewModelTest")
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        error("Not used by LoginViewModelTest")
    }

    override suspend fun hasActiveSession(): Boolean {
        error("Not used by LoginViewModelTest")
    }

    override fun persistRememberedSession(email: String) {
        lastRememberedEmail = email
    }

    override fun clearRememberedSession() {
        clearRememberedSessionCallCount++
    }

    override fun observeProfile(): Flow<User?> {
        error("Not used by LoginViewModelTest")
    }

    override fun logout() {
        error("Not used by LoginViewModelTest")
    }

    override fun observeProfileFetchedAt(): Flow<Long?> {
        error("Not used by LoginViewModelTest")
    }

    override suspend fun updateProfile(user: User): Result<Unit> {
        lastUpdatedProfile = user
        return Result.success(Unit)
    }
}
