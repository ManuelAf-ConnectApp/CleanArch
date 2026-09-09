package com.connectapp.domain.usecase

import com.connectapp.domain.fake.FakeAuthRepository
import com.connectapp.domain.model.AuthError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegisterUseCaseTest {

    @Test
    fun `invoke returns success from repository`() = runTest {
        val useCase = RegisterUseCase(FakeAuthRepository(registerResult = Result.success(true)))

        val result = useCase(name = "Jane Doe", email = "jane@example.com", password = "secret123")

        assertEquals(Result.success(true), result)
    }

    @Test
    fun `invoke propagates failure from repository`() = runTest {
        val useCase = RegisterUseCase(
            FakeAuthRepository(registerResult = Result.failure(AuthError.EmailAlreadyRegistered))
        )

        val result = useCase(name = "Jane Doe", email = "jane@example.com", password = "secret123")

        assertTrue(result.isFailure)
        assertEquals(AuthError.EmailAlreadyRegistered, result.exceptionOrNull())
    }
}
