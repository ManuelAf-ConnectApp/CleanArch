package com.connectapp.domain.di

import com.connectapp.domain.usecase.ObserveAnalyticsConsentUseCase
import com.connectapp.domain.usecase.SetAnalyticsConsentUseCase
import com.connectapp.domain.usecase.TrackFlowEventUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** specs/003-decentralize-domain-data-di: :domain:analytics owns its own Koin module. */
val analyticsDomainModule = module {
    factoryOf(::SetAnalyticsConsentUseCase)
    factoryOf(::ObserveAnalyticsConsentUseCase)
    factoryOf(::TrackFlowEventUseCase)
}
