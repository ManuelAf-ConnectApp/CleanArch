package com.connectapp.domain.usecase

import com.connectapp.domain.fake.FakeBookingsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RefreshBookingsUseCaseTest {

    @Test
    fun `invoke returns success from repository`() = runTest {
        val repository = FakeBookingsRepository(refreshResult = Result.success(Unit))
        val useCase = RefreshBookingsUseCase(repository)

        assertEquals(Result.success(Unit), useCase())
        assertEquals(1, repository.refreshCallCount)
    }

    @Test
    fun `invoke propagates failure from repository`() = runTest {
        val repository = FakeBookingsRepository(refreshResult = Result.failure(Exception("network down")))
        val useCase = RefreshBookingsUseCase(repository)

        assertTrue(useCase().isFailure)
    }
}
