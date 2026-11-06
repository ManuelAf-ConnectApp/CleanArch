package com.connectapp.domain.di

import com.connectapp.domain.usecase.ObserveBookingsFetchedAtUseCase
import com.connectapp.domain.usecase.ObserveBookingsUseCase
import com.connectapp.domain.usecase.RefreshBookingsUseCase
import com.connectapp.domain.usecase.CreateDemoBookingUseCase
import com.connectapp.domain.usecase.CancelDemoBookingUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** specs/003-decentralize-domain-data-di: :domain:orders owns its own Koin module. */
val bookingsDomainModule = module {
    factoryOf(::ObserveBookingsUseCase)
    factoryOf(::RefreshBookingsUseCase)
    factoryOf(::CreateDemoBookingUseCase)
    factoryOf(::CancelDemoBookingUseCase)
    factoryOf(::ObserveBookingsFetchedAtUseCase)
}
