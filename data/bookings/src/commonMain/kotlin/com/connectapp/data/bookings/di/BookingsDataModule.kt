package com.connectapp.data.bookings.di

import com.connectapp.core.database.createSqlDriver
import com.connectapp.data.bookings.database.OrdersDatabase
import com.connectapp.data.bookings.datasource.BookingsLocalDataSource
import com.connectapp.data.bookings.datasource.BookingsLocalDataSourceImpl
import com.connectapp.data.bookings.datasource.BookingsRemoteDataSource
import com.connectapp.data.bookings.datasource.BookingsRemoteDataSourceImpl
import com.connectapp.data.bookings.repository.BookingsRepositoryImpl
import com.connectapp.data.bookings.service.BookingsService
import com.connectapp.data.bookings.service.BookingsServiceImpl
import com.connectapp.domain.repository.BookingsRepository
import org.koin.dsl.module

/** SQLite file name for `:core:database`'s `createSqlDriver()` — see specs/010-offline-cache-layer/data-model.md. */
private const val ORDERS_DATABASE_NAME = "orders.db"

/**
 * specs/003-decentralize-domain-data-di: :data:orders owns its own Koin module. Reuses the
 * same injected [com.connectapp.core.network.createHttpClient] singleton as `:data:auth`'s
 * `dataModule` (one backend, many resources) at the shared Koin container level (research.md
 * D4) — no Gradle dependency on `:data:auth` needed for that.
 */
val bookingsDataModule = module {
    single<BookingsService> { BookingsServiceImpl(get()) }
    single<BookingsRemoteDataSource> { BookingsRemoteDataSourceImpl(get()) }
    single { OrdersDatabase(createSqlDriver(OrdersDatabase.Schema, ORDERS_DATABASE_NAME)) }
    single<BookingsLocalDataSource> { BookingsLocalDataSourceImpl(get()) }
    single<BookingsRepository> { BookingsRepositoryImpl(get(), get()) }
}
