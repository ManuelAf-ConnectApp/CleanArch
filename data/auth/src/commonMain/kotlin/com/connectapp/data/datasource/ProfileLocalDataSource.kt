package com.connectapp.data.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.connectapp.data.database.AuthDatabase
import com.connectapp.data.database.CachedProfileEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

/**
 * Local (SQLDelight-backed) cache of the profile seeded at the last successful `login()` — see
 * specs/010-offline-cache-layer/data-model.md and research.md D6 (no network "refresh profile"
 * call exists yet, so [upsert] is only ever called from `AuthRepositoryImpl.login()`).
 */
interface ProfileLocalDataSource {
    fun observe(): Flow<CachedProfileEntity?>
    fun upsert(firstName: String, lastName: String, email: String, phone: String, fetchedAt: Long)
    fun clear()
}

class ProfileLocalDataSourceImpl(
    database: AuthDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ProfileLocalDataSource {

    private val queries = database.cachedProfileQueries

    override fun observe(): Flow<CachedProfileEntity?> =
        queries.select().asFlow().mapToOneOrNull(dispatcher)

    override fun upsert(firstName: String, lastName: String, email: String, phone: String, fetchedAt: Long) {
        queries.upsert(
            firstName = firstName,
            lastName = lastName,
            email = email,
            phone = phone,
            fetchedAt = fetchedAt,
        )
    }

    override fun clear() {
        queries.clear()
    }
}
