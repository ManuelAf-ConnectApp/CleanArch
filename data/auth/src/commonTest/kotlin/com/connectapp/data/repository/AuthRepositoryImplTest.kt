package com.connectapp.data.repository

import com.connectapp.data.database.CachedProfileEntity
import com.connectapp.data.datasource.AuthLocalDataSource
import com.connectapp.data.datasource.AuthRemoteDataSource
import com.connectapp.data.datasource.ProfileLocalDataSource
import com.connectapp.data.model.UserDto
import com.connectapp.domain.model.AuthError
import com.connectapp.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Fake [AuthLocalDataSource] whose token can be preset, so tests can simulate "a stale/valid
 * local session already exists" without touching real secure storage (see
 * AuthLocalDataSourceTest.kt for that layer's own tests). `saveToken`/`clearSession` are exercised
 * by [AuthRepositoryImpl.login]'s `rememberSession` handling — see the "keep session alive" tests
 * below.
 */
private class FakeAuthLocalDataSource(
    private var token: String?
) : AuthLocalDataSource {
    override fun saveToken(token: String) {
        this.token = token
    }

    override fun getToken(): String? = token

    override fun clearSession() {
        token = null
    }
}

/**
 * Fake [AuthRemoteDataSource] whose `login` result is fixed by the test, so
 * [AuthRepositoryImpl.login] can be verified in isolation from any real HTTP/MockEngine
 * plumbing (that layer is already covered by AuthRemoteDataSourceLoginTest.kt / T011).
 */
private class FakeAuthRemoteDataSource(
    private val loginResult: Result<UserDto>
) : AuthRemoteDataSource {
    override suspend fun login(email: String, password: String): Result<UserDto> = loginResult

    override suspend fun register(name: String, email: String, password: String): Result<Boolean> {
        error("Not used by AuthRepositoryImplTest")
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        error("Not used by AuthRepositoryImplTest")
    }
}

/** In-memory stand-in for the SQLDelight-backed profile cache — see specs/010-offline-cache-layer. */
private class FakeProfileLocalDataSource : ProfileLocalDataSource {
    private val flow = MutableStateFlow<CachedProfileEntity?>(null)

    override fun observe(): Flow<CachedProfileEntity?> = flow

    override fun upsert(firstName: String, lastName: String, email: String, phone: String, fetchedAt: Long) {
        flow.value = CachedProfileEntity(0, firstName, lastName, email, phone, fetchedAt)
    }

    override fun clear() {
        flow.value = null
    }
}

private val REAL_USER_DTO = UserDto(
    id = "usr-8842",
    firstName = "Grace",
    lastName = "Hopper",
    email = "grace.hopper@example.com",
    phone = "+1-555-0199",
    createdAt = "2024-01-15T10:30:00Z",
    updatedAt = "2024-06-20T08:00:00Z"
)

private val REAL_USER = User(
    firstName = "Grace",
    lastName = "Hopper",
    email = "grace.hopper@example.com",
    phone = "+1-555-0199"
)

class AuthRepositoryImplTest {

    // Regression guard for the original bug (spec.md audit finding, fixed in T014): with a
    // non-null local token present, AuthRepositoryImpl.login() used to short-circuit and return a
    // hardcoded User("Cached", "User", ...) instead of hitting AuthRemoteDataSource at all. This
    // test proves that no longer happens: even with a stale local token, the result is exactly
    // what the remote data source returned, and specifically NOT a "Cached"/hardcoded value.
    @Test
    fun `login with a stale local token still returns the exact User from the remote data source and not a hardcoded one`() =
        runTest {
            val sut = AuthRepositoryImpl(
                authLocalDataSource = FakeAuthLocalDataSource(token = "stale-token-123"),
                authRemoteDataSource = FakeAuthRemoteDataSource(Result.success(REAL_USER_DTO)),
                profileLocalDataSource = FakeProfileLocalDataSource(),
            )

            val result = sut.login(email = "grace.hopper@example.com", password = "S3cr3t!2024")

            assertTrue(result.isSuccess)
            assertEquals(REAL_USER, result.getOrNull())
            assertNotEquals("Cached", result.getOrNull()?.firstName)
            assertNotEquals("User", result.getOrNull()?.lastName)
        }

    // Same setup as above but with no local session at all: the result must be identical to the
    // stale-token scenario, proving local token state (present or absent) has zero influence on
    // AuthRepositoryImpl.login()'s outcome — the outcome depends solely on the remote call.
    @Test
    fun `login with no local token returns the same result as with a stale local token`() = runTest {
        val sut = AuthRepositoryImpl(
            authLocalDataSource = FakeAuthLocalDataSource(token = null),
            authRemoteDataSource = FakeAuthRemoteDataSource(Result.success(REAL_USER_DTO)),
            profileLocalDataSource = FakeProfileLocalDataSource(),
        )

        val result = sut.login(email = "grace.hopper@example.com", password = "S3cr3t!2024")

        assertTrue(result.isSuccess)
        assertEquals(REAL_USER, result.getOrNull())
    }

    // The repository must not swallow or alter a remote failure, regardless of local token state.
    @Test
    fun `login propagates an AuthError failure from the remote data source unchanged`() = runTest {
        val sut = AuthRepositoryImpl(
            authLocalDataSource = FakeAuthLocalDataSource(token = "stale-token-123"),
            authRemoteDataSource = FakeAuthRemoteDataSource(Result.failure(AuthError.InvalidCredentials)),
            profileLocalDataSource = FakeProfileLocalDataSource(),
        )

        val result = sut.login(email = "grace.hopper@example.com", password = "wrong-password")

        assertTrue(result.isFailure)
        assertEquals(AuthError.InvalidCredentials, result.exceptionOrNull())
    }

    // specs/010-offline-cache-layer FR-003/D6: a successful login seeds the profile cache, and it
    // becomes observable without any further network call.
    @Test
    fun `a successful login seeds the profile cache observable via observeProfile`() = runTest {
        val sut = AuthRepositoryImpl(
            authLocalDataSource = FakeAuthLocalDataSource(token = null),
            authRemoteDataSource = FakeAuthRemoteDataSource(Result.success(REAL_USER_DTO)),
            profileLocalDataSource = FakeProfileLocalDataSource(),
        )

        sut.login(email = "grace.hopper@example.com", password = "S3cr3t!2024")

        assertEquals(REAL_USER, sut.observeProfile().first())
    }

    // A failed login must not seed the profile cache.
    @Test
    fun `a failed login does not seed the profile cache`() = runTest {
        val sut = AuthRepositoryImpl(
            authLocalDataSource = FakeAuthLocalDataSource(token = null),
            authRemoteDataSource = FakeAuthRemoteDataSource(Result.failure(AuthError.InvalidCredentials)),
            profileLocalDataSource = FakeProfileLocalDataSource(),
        )

        sut.login(email = "grace.hopper@example.com", password = "wrong-password")

        assertNull(sut.observeProfile().first())
    }

    // FR-009/SC-004: logout must clear both the session token and the cached profile.
    @Test
    fun `logout clears the session token and the cached profile`() = runTest {
        val authLocalDataSource = FakeAuthLocalDataSource(token = "some-token")
        val sut = AuthRepositoryImpl(
            authLocalDataSource = authLocalDataSource,
            authRemoteDataSource = FakeAuthRemoteDataSource(Result.success(REAL_USER_DTO)),
            profileLocalDataSource = FakeProfileLocalDataSource(),
        )
        sut.login(email = "grace.hopper@example.com", password = "S3cr3t!2024")

        sut.logout()

        assertEquals(null, authLocalDataSource.getToken())
        assertNull(sut.observeProfile().first())
    }

    // "Keep session alive" checkbox (rememberSession = true): a successful login must persist a
    // local session marker so hasActiveSession()/Splash can skip Login on a later app launch.
    @Test
    fun `login with rememberSession true persists a session marker for the logged-in user's email`() = runTest {
        val authLocalDataSource = FakeAuthLocalDataSource(token = null)
        val sut = AuthRepositoryImpl(
            authLocalDataSource = authLocalDataSource,
            authRemoteDataSource = FakeAuthRemoteDataSource(Result.success(REAL_USER_DTO)),
            profileLocalDataSource = FakeProfileLocalDataSource(),
        )

        sut.login(email = "grace.hopper@example.com", password = "S3cr3t!2024", rememberSession = true)

        assertEquals(REAL_USER.email, authLocalDataSource.getToken())
        assertTrue(sut.hasActiveSession())
    }

    // rememberSession = false (the default) must never leave a session marker behind, even if one
    // already existed from a previous remembered login — otherwise unchecking the box would be a
    // no-op and the app would still silently skip Login next launch.
    @Test
    fun `login with rememberSession false clears any pre-existing session marker`() = runTest {
        val authLocalDataSource = FakeAuthLocalDataSource(token = "stale-remembered-session")
        val sut = AuthRepositoryImpl(
            authLocalDataSource = authLocalDataSource,
            authRemoteDataSource = FakeAuthRemoteDataSource(Result.success(REAL_USER_DTO)),
            profileLocalDataSource = FakeProfileLocalDataSource(),
        )

        sut.login(email = "grace.hopper@example.com", password = "S3cr3t!2024", rememberSession = false)

        assertNull(authLocalDataSource.getToken())
        assertTrue(sut.hasActiveSession().not())
    }

    // A failed login must never persist a session marker, regardless of rememberSession.
    @Test
    fun `a failed login does not persist a session marker even with rememberSession true`() = runTest {
        val authLocalDataSource = FakeAuthLocalDataSource(token = null)
        val sut = AuthRepositoryImpl(
            authLocalDataSource = authLocalDataSource,
            authRemoteDataSource = FakeAuthRemoteDataSource(Result.failure(AuthError.InvalidCredentials)),
            profileLocalDataSource = FakeProfileLocalDataSource(),
        )

        sut.login(email = "grace.hopper@example.com", password = "wrong-password", rememberSession = true)

        assertNull(authLocalDataSource.getToken())
    }

    @Test
    fun `observeProfileFetchedAt is null until a login succeeds`() = runTest {
        val sut = AuthRepositoryImpl(
            authLocalDataSource = FakeAuthLocalDataSource(token = null),
            authRemoteDataSource = FakeAuthRemoteDataSource(Result.success(REAL_USER_DTO)),
            profileLocalDataSource = FakeProfileLocalDataSource(),
        )

        assertNull(sut.observeProfileFetchedAt().first())

        sut.login(email = "grace.hopper@example.com", password = "S3cr3t!2024")

        assertNotNull(sut.observeProfileFetchedAt().first())
    }
}
