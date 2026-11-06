package com.connectapp.domain.repository

import com.connectapp.domain.model.Booking
import com.connectapp.domain.model.DemoScenarioConfig
import kotlinx.coroutines.flow.Flow

/**
 * See specs/010-offline-cache-layer/contracts/bookings-repository-contract.md — reads and
 * refreshes are deliberately separate: [observeBookings] never calls the network by itself, and
 * [refreshBookings] never returns the list directly (a successful refresh is only observable
 * through [observeBookings] re-emitting).
 */
interface BookingsRepository {
    fun observeBookings(): Flow<List<Booking>>
    suspend fun refreshBookings(): Result<Unit>

    /** Stores a reservation created by the local MVP flow. */
    suspend fun createDemoBooking(
        booking: Booking,
        scenarioConfig: DemoScenarioConfig = DemoScenarioConfig(),
    ): Result<Unit> =
        Result.failure(UnsupportedOperationException("Creating demo bookings is not supported."))

    /** Cancels a locally simulated reservation. */
    suspend fun cancelDemoBooking(id: String): Result<Unit> =
        Result.failure(UnsupportedOperationException("Cancelling demo bookings is not supported."))

    suspend fun clearCache()

    /**
     * When the currently cached orders were last successfully synced, or `null` if nothing has
     * ever been cached. Additive to the contract above — see
     * specs/010-offline-cache-layer/spec.md User Story 3 (FR-006).
     */
    fun observeLastFetchedAt(): Flow<Long?>
}
