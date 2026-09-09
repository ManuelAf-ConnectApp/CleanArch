package com.connectapp.data.orders.di

import com.connectapp.core.database.createSqlDriver
import com.connectapp.data.orders.database.OrdersDatabase
import com.connectapp.data.orders.datasource.OrdersLocalDataSource
import com.connectapp.data.orders.datasource.OrdersLocalDataSourceImpl
import com.connectapp.data.orders.datasource.OrdersRemoteDataSource
import com.connectapp.data.orders.datasource.OrdersRemoteDataSourceImpl
import com.connectapp.data.orders.repository.OrdersRepositoryImpl
import com.connectapp.data.orders.service.OrdersService
import com.connectapp.data.orders.service.OrdersServiceImpl
import com.connectapp.domain.repository.OrdersRepository
import org.koin.dsl.module

/** SQLite file name for `:core:database`'s `createSqlDriver()` — see specs/010-offline-cache-layer/data-model.md. */
private const val ORDERS_DATABASE_NAME = "orders.db"

/**
 * specs/003-decentralize-domain-data-di: :data:orders owns its own Koin module. Reuses the
 * same injected [com.connectapp.core.network.createHttpClient] singleton as `:data:auth`'s
 * `dataModule` (one backend, many resources) at the shared Koin container level (research.md
 * D4) — no Gradle dependency on `:data:auth` needed for that.
 */
val ordersDataModule = module {
    single<OrdersService> { OrdersServiceImpl(get()) }
    single<OrdersRemoteDataSource> { OrdersRemoteDataSourceImpl(get()) }
    single { OrdersDatabase(createSqlDriver(OrdersDatabase.Schema, ORDERS_DATABASE_NAME)) }
    single<OrdersLocalDataSource> { OrdersLocalDataSourceImpl(get()) }
    single<OrdersRepository> { OrdersRepositoryImpl(get(), get()) }
}
