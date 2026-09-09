package com.connectapp.domain.usecase

import com.connectapp.domain.fake.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GetSessionStatusUseCaseTest {

    @Test
    fun `invoke returns true when the repository has an active session`() = runTest {
        val useCase = GetSessionStatusUseCase(FakeAuthRepository(hasActiveSessionResult = true))

        assertTrue(useCase())
    }

    @Test
    fun `invoke returns false when the repository has no active session`() = runTest {
        val useCase = GetSessionStatusUseCase(FakeAuthRepository(hasActiveSessionResult = false))

        assertFalse(useCase())
    }
}
