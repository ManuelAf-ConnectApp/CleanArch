package com.connectapp.domain.usecase

import com.connectapp.domain.fake.FakeAuthRepository
import com.connectapp.domain.model.AuthError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ForgotPasswordUseCaseTest {

    @Test
    fun `invoke returns success from repository`() = runTest {
        val useCase = ForgotPasswordUseCase(FakeAuthRepository(forgotPasswordResult = Result.success(Unit)))

        val result = useCase(email = "jane@example.com")

        assertEquals(Result.success(Unit), result)
    }

    @Test
    fun `invoke propagates failure from repository`() = runTest {
        val useCase = ForgotPasswordUseCase(
            FakeAuthRepository(forgotPasswordResult = Result.failure(AuthError.Server(500)))
        )

        val result = useCase(email = "jane@example.com")

        assertTrue(result.isFailure)
        assertEquals(AuthError.Server(500), result.exceptionOrNull())
    }
}
