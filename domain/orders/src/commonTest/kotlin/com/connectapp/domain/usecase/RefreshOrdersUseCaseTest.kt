package com.connectapp.domain.usecase

import com.connectapp.domain.fake.FakeOrdersRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RefreshOrdersUseCaseTest {

    @Test
    fun `invoke returns success from repository`() = runTest {
        val repository = FakeOrdersRepository(refreshResult = Result.success(Unit))
        val useCase = RefreshOrdersUseCase(repository)

        assertEquals(Result.success(Unit), useCase())
        assertEquals(1, repository.refreshCallCount)
    }

    @Test
    fun `invoke propagates failure from repository`() = runTest {
        val repository = FakeOrdersRepository(refreshResult = Result.failure(Exception("network down")))
        val useCase = RefreshOrdersUseCase(repository)

        assertTrue(useCase().isFailure)
    }
}
