package com.connectapp.data.di

import com.connectapp.core.database.createSqlDriver
import com.connectapp.core.network.NetworkInterceptor
import com.connectapp.core.network.createHttpClient
import com.connectapp.core.storage.createKVault
import com.connectapp.data.database.AuthDatabase
import com.connectapp.data.datasource.AuthLocalDataSource
import com.connectapp.data.datasource.AuthLocalDataSourceImpl
import com.connectapp.data.datasource.AuthRemoteDataSource
import com.connectapp.data.datasource.AuthRemoteDataSourceImpl
import com.connectapp.data.datasource.ProfileLocalDataSource
import com.connectapp.data.datasource.ProfileLocalDataSourceImpl
import com.connectapp.data.interceptor.AuthInterceptor
import com.connectapp.data.repository.AuthRepositoryImpl
import com.connectapp.data.service.AuthService
import com.connectapp.data.service.AuthServiceImpl
import com.connectapp.domain.repository.AuthRepository
import org.koin.dsl.module

/** Store name passed to `:core:storage`'s createKVault() for the auth session. */
private const val AUTH_SESSION_STORE_NAME = "connectapp_auth_session"

/** SQLite file name for `:core:database`'s `createSqlDriver()` — see specs/010-offline-cache-layer/data-model.md. */
private const val AUTH_DATABASE_NAME = "auth.db"

/**
 * specs/003-decentralize-domain-data-di: :data:auth owns its own Koin module. [authApiBaseUrl]
 * is passed in from composeApp's `AppConfig` (research.md D2) — this module never reads
 * `AppConfig` itself, since `:data:auth` cannot depend on `:composeApp`.
 */
fun dataModule(authApiBaseUrl: String) = module {
    single { createHttpClient(baseUrl = authApiBaseUrl) }
    single<NetworkInterceptor> { AuthInterceptor(get()) }
    single<AuthService> { AuthServiceImpl(get()) }
    single<AuthRemoteDataSource> { AuthRemoteDataSourceImpl(get(), get()) }
    single<AuthLocalDataSource> { AuthLocalDataSourceImpl(createKVault(name = AUTH_SESSION_STORE_NAME)) }
    single { AuthDatabase(createSqlDriver(AuthDatabase.Schema, AUTH_DATABASE_NAME)) }
    single<ProfileLocalDataSource> { ProfileLocalDataSourceImpl(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get()) }
}
