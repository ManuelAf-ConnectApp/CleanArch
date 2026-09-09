package com.connectapp.presentation.home

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
class HomeViewModelTest {

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
    fun `ItemClicked emits ShowToast for that item`() = runTest {
        val viewModel = HomeViewModel()
        val effects = mutableListOf<HomeEffect>()
        val job = launch { viewModel.effect.toList(effects) }
        val item = dashboardItems.first()

        viewModel.onIntent(HomeIntent.ItemClicked(item))
        advanceUntilIdle()

        assertTrue(effects.any { it is HomeEffect.ShowToast && it.item == item })
        job.cancel()
    }
}
