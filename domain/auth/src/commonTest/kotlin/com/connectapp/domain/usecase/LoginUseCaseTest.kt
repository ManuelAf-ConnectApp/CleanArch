package com.connectapp.domain.usecase

import com.connectapp.domain.fake.FakeAuthRepository
import com.connectapp.domain.model.AuthError
import com.connectapp.domain.model.User
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoginUseCaseTest {

    private val someUser = User(firstName = "Jane", lastName = "Doe", email = "jane@example.com", phone = "+1")

    @Test
    fun `invoke returns success from repository`() = runTest {
        val useCase = LoginUseCase(FakeAuthRepository(loginResult = Result.success(someUser)))

        val result = useCase(email = "jane@example.com", password = "secret")

        assertEquals(Result.success(someUser), result)
    }

    @Test
    fun `invoke propagates failure from repository`() = runTest {
        val useCase = LoginUseCase(FakeAuthRepository(loginResult = Result.failure(AuthError.InvalidCredentials)))

        val result = useCase(email = "jane@example.com", password = "wrong")

        assertTrue(result.isFailure)
        assertEquals(AuthError.InvalidCredentials, result.exceptionOrNull())
    }
}
