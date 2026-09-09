package com.connectapp.domain.usecase

import com.connectapp.domain.fake.FakeAuthRepository
import com.connectapp.domain.model.User
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdateProfileUseCaseTest {

    private val editedUser = User(firstName = "Jane", lastName = "Doe", email = "jane@example.com", phone = "+1")

    @Test
    fun `invoke forwards the user to the repository and returns its result`() = runTest {
        val repository = FakeAuthRepository()
        val useCase = UpdateProfileUseCase(repository)

        val result = useCase(editedUser)

        assertTrue(result.isSuccess)
        assertEquals(1, repository.updateProfileCallCount)
        assertEquals(editedUser, repository.lastUpdatedProfile)
    }

    @Test
    fun `invoke propagates failure from the repository`() = runTest {
        val repository = FakeAuthRepository(updateProfileResult = Result.failure(Exception("disk full")))
        val useCase = UpdateProfileUseCase(repository)

        val result = useCase(editedUser)

        assertTrue(result.isFailure)
    }
}
