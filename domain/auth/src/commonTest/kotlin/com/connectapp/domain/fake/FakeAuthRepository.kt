package com.connectapp.domain.fake

import com.connectapp.domain.model.User
import com.connectapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeAuthRepository(
    private val loginResult: Result<User> = Result.success(
        User(firstName = "Test", lastName = "User", email = "test@example.com", phone = "")
    ),
    private val registerResult: Result<Boolean> = Result.success(true),
    private val forgotPasswordResult: Result<Unit> = Result.success(Unit),
    private val hasActiveSessionResult: Boolean = false,
    private val updateProfileResult: Result<Unit> = Result.success(Unit),
) : AuthRepository {
    private val profileFlow = MutableStateFlow<User?>(null)

    var updateProfileCallCount: Int = 0
        private set
    var lastUpdatedProfile: User? = null
        private set

    var lastRememberedEmail: String? = null
        private set
    var clearRememberedSessionCallCount: Int = 0
        private set

    override suspend fun login(email: String, password: String, rememberSession: Boolean): Result<User> = loginResult
    override suspend fun register(name: String, email: String, password: String): Result<Boolean> = registerResult
    override suspend fun forgotPassword(email: String): Result<Unit> = forgotPasswordResult
    override suspend fun hasActiveSession(): Boolean = hasActiveSessionResult
    override fun persistRememberedSession(email: String) {
        lastRememberedEmail = email
    }
    override fun clearRememberedSession() {
        clearRememberedSessionCallCount++
    }
    override fun observeProfile(): Flow<User?> = profileFlow
    override fun logout() {
        profileFlow.value = null
    }
    override fun observeProfileFetchedAt(): Flow<Long?> = MutableStateFlow(null)
    override suspend fun updateProfile(user: User): Result<Unit> {
        updateProfileCallCount++
        lastUpdatedProfile = user
        return updateProfileResult
    }
}
