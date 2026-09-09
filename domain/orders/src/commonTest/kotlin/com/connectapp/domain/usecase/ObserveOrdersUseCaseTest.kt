package com.connectapp.domain.usecase

import com.connectapp.domain.fake.FakeOrdersRepository
import com.connectapp.domain.model.Order
import com.connectapp.domain.model.OrderStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveOrdersUseCaseTest {

    @Test
    fun `invoke emits the repository's current orders without triggering a refresh`() = runTest {
        val orders = listOf(Order(id = "1", title = "Order 1", status = OrderStatus.SHIPPED, date = "2026-08-01"))
        val repository = FakeOrdersRepository(ordersFlow = MutableStateFlow(orders))
        val useCase = ObserveOrdersUseCase(repository)

        assertEquals(orders, useCase().first())
        assertEquals(0, repository.refreshCallCount)
    }

    @Test
    fun `invoke emits an empty list when nothing is cached yet`() = runTest {
        val repository = FakeOrdersRepository()
        val useCase = ObserveOrdersUseCase(repository)

        assertEquals(emptyList(), useCase().first())
    }
}
