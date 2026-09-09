package com.connectapp.domain.di

import com.connectapp.domain.usecase.ObserveOrdersFetchedAtUseCase
import com.connectapp.domain.usecase.ObserveOrdersUseCase
import com.connectapp.domain.usecase.RefreshOrdersUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** specs/003-decentralize-domain-data-di: :domain:orders owns its own Koin module. */
val ordersDomainModule = module {
    factoryOf(::ObserveOrdersUseCase)
    factoryOf(::RefreshOrdersUseCase)
    factoryOf(::ObserveOrdersFetchedAtUseCase)
}
