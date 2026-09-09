package com.connectapp.domain.usecase

import com.connectapp.domain.fake.FakeAuthRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class SetSessionRememberedUseCaseTest {

    @Test
    fun `invoke with remembered true persists a session marker for the given email`() {
        val repository = FakeAuthRepository()
        val useCase = SetSessionRememberedUseCase(repository)

        useCase(remembered = true, email = "jane@example.com")

        assertEquals("jane@example.com", repository.lastRememberedEmail)
        assertEquals(0, repository.clearRememberedSessionCallCount)
    }

    @Test
    fun `invoke with remembered false clears the session marker`() {
        val repository = FakeAuthRepository()
        val useCase = SetSessionRememberedUseCase(repository)

        useCase(remembered = false, email = "jane@example.com")

        assertEquals(1, repository.clearRememberedSessionCallCount)
        assertEquals(null, repository.lastRememberedEmail)
    }
}
