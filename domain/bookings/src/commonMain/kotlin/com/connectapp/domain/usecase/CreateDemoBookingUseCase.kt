package com.connectapp.domain.usecase

import com.connectapp.domain.model.Booking
import com.connectapp.domain.model.DemoScenarioConfig
import com.connectapp.domain.repository.BookingsRepository

/** Persists a locally simulated restaurant reservation for the MVP. */
class CreateDemoBookingUseCase(
    private val repository: BookingsRepository,
) {
    suspend operator fun invoke(
        booking: Booking,
        scenarioConfig: DemoScenarioConfig = DemoScenarioConfig(),
    ): Result<Unit> = repository.createDemoBooking(booking, scenarioConfig)
}
