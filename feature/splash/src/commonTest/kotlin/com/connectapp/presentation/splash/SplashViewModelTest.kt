package com.connectapp.presentation.splash

import com.connectapp.domain.usecase.GetSessionStatusUseCase
import com.connectapp.presentation.splash.fake.FakeAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `active session emits NavigateToHome`() = runTest {
        val viewModel = SplashViewModel(GetSessionStatusUseCase(FakeAuthRepository(hasActiveSessionResult = true)))
        val effects = mutableListOf<SplashEffect>()
        val job = launch { viewModel.effect.toList(effects) }

        advanceUntilIdle()

        assertTrue(effects.any { it is SplashEffect.NavigateToHome })
        job.cancel()
    }

    @Test
    fun `no active session emits NavigateToLogin`() = runTest {
        val viewModel = SplashViewModel(GetSessionStatusUseCase(FakeAuthRepository(hasActiveSessionResult = false)))
        val effects = mutableListOf<SplashEffect>()
        val job = launch { viewModel.effect.toList(effects) }

        advanceUntilIdle()

        assertTrue(effects.any { it is SplashEffect.NavigateToLogin })
        job.cancel()
    }
}
