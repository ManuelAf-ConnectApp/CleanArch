package com.connectapp.domain.usecase

import com.connectapp.domain.analytics.FlowOutcome
import com.connectapp.domain.analytics.KeyFlow
import com.connectapp.domain.fake.FakeAnalyticsReporter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObserveAnalyticsConsentUseCaseTest {

    @Test
    fun `defaults to false before any consent is granted`() = runTest {
        val reporter = FakeAnalyticsReporter()
        val useCase = ObserveAnalyticsConsentUseCase(reporter)

        assertFalse(useCase().first())
    }

    @Test
    fun `reflects consent granted via SetAnalyticsConsentUseCase`() = runTest {
        val reporter = FakeAnalyticsReporter()
        val observe = ObserveAnalyticsConsentUseCase(reporter)
        val setConsent = SetAnalyticsConsentUseCase(reporter)

        setConsent(granted = true)

        assertTrue(observe().first())
    }

    @Test
    fun `trackFlowEvent is a no-op without consent`() = runTest {
        val reporter = FakeAnalyticsReporter()
        val trackEvent = TrackFlowEventUseCase(reporter)

        trackEvent(KeyFlow.LOGIN, FlowOutcome.SUCCESS)

        assertTrue(reporter.trackedEvents.isEmpty())
    }
}
