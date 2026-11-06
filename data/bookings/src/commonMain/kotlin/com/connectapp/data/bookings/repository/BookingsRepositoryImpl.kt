package com.connectapp.data.bookings.repository

import com.connectapp.core.database.currentEpochMillis
import com.connectapp.data.bookings.datasource.BookingsLocalDataSource
import com.connectapp.data.bookings.datasource.BookingsRemoteDataSource
import com.connectapp.data.bookings.mapper.toCacheEntity
import com.connectapp.data.bookings.mapper.toDomain
import com.connectapp.domain.model.Booking
import com.connectapp.domain.model.BookingStatus
import com.connectapp.domain.model.DemoScenario
import com.connectapp.domain.model.DemoScenarioConfig
import com.connectapp.domain.model.DemoScenarioException
import com.connectapp.domain.model.canBeCancelled
import com.connectapp.domain.repository.BookingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.delay

/**
 * See specs/010-offline-cache-layer/contracts/bookings-repository-contract.md. [bookingsLocalDataSource]
 * is the single source of truth for [observeBookings] — [refreshBookings] only ever writes to it after
 * a successful network call (FR-005); a failed refresh leaves it untouched (FR-012).
 */
class BookingsRepositoryImpl(
    private val bookingsRemoteDataSource: BookingsRemoteDataSource,
    private val bookingsLocalDataSource: BookingsLocalDataSource,
) : BookingsRepository {

    override fun observeBookings(): Flow<List<Booking>> =
        bookingsLocalDataSource.observeAll().map { cached -> cached.map { it.toDomain() } }

    override suspend fun refreshBookings(): Result<Unit> {
        return bookingsRemoteDataSource.getBookings().map { dtos ->
            val fetchedAt = currentEpochMillis()
            val cacheEntities = dtos.map { it.toDomain().toCacheEntity(fetchedAt) }
            bookingsLocalDataSource.replaceAll(cacheEntities)
        }
    }

    override suspend fun createDemoBooking(
        booking: Booking,
        scenarioConfig: DemoScenarioConfig,
    ): Result<Unit> = runCatching {
        delay(scenarioConfig.latencyMillis)
        if (scenarioConfig.scenario != DemoScenario.SUCCESS) {
            throw DemoScenarioException(scenarioConfig.scenario)
        }
        bookingsLocalDataSource.upsert(booking.toCacheEntity(currentEpochMillis()))
    }

    override suspend fun cancelDemoBooking(id: String): Result<Unit> = runCatching {
        val booking = bookingsLocalDataSource.observeAll()
            .first()
            .firstOrNull { it.id == id }
            ?.toDomain()
            ?: error("Booking not found: $id")

        check(booking.canBeCancelled) { "Booking cannot be cancelled: $id" }

        bookingsLocalDataSource.upsert(
            booking.copy(status = BookingStatus.CANCELLED).toCacheEntity(currentEpochMillis()),
        )
    }

    override suspend fun clearCache() {
        bookingsLocalDataSource.clear()
    }

    override fun observeLastFetchedAt(): Flow<Long?> =
        bookingsLocalDataSource.observeAll().map { cached -> cached.maxOfOrNull { it.fetchedAt } }
}
