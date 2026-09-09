package com.connectapp.domain.repository

import com.connectapp.domain.model.Order
import kotlinx.coroutines.flow.Flow

/**
 * See specs/010-offline-cache-layer/contracts/orders-repository-contract.md — reads and
 * refreshes are deliberately separate: [observeOrders] never calls the network by itself, and
 * [refreshOrders] never returns the list directly (a successful refresh is only observable
 * through [observeOrders] re-emitting).
 */
interface OrdersRepository {
    fun observeOrders(): Flow<List<Order>>
    suspend fun refreshOrders(): Result<Unit>
    suspend fun clearCache()

    /**
     * When the currently cached orders were last successfully synced, or `null` if nothing has
     * ever been cached. Additive to the contract above — see
     * specs/010-offline-cache-layer/spec.md User Story 3 (FR-006).
     */
    fun observeLastFetchedAt(): Flow<Long?>
}
