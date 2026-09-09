package com.connectapp.data.analytics.repository

import com.connectapp.data.analytics.datasource.AnalyticsConsentLocalDataSource
import com.connectapp.domain.analytics.FlowOutcome
import com.connectapp.domain.analytics.KeyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FakeAnalyticsConsentLocalDataSource(
    initiallyGranted: Boolean = false,
) : AnalyticsConsentLocalDataSource {
    private var granted = initiallyGranted

    override fun isConsentGranted(): Boolean = granted
    override fun setConsentGranted(granted: Boolean) {
        this.granted = granted
    }
}

class SentryAnalyticsReporterImplTest {

    @Test
    fun `observeConsent starts false when nothing was ever persisted`() = runTest {
        val reporter = SentryAnalyticsReporterImpl(dsn = "", consentLocalDataSource = FakeAnalyticsConsentLocalDataSource())

        assertFalse(reporter.observeConsent().first())
    }

    @Test
    fun `observeConsent reflects the value already persisted at construction`() = runTest {
        val reporter = SentryAnalyticsReporterImpl(
            dsn = "",
            consentLocalDataSource = FakeAnalyticsConsentLocalDataSource(initiallyGranted = true),
        )

        assertTrue(reporter.observeConsent().first())
    }

    @Test
    fun `setConsent persists and is reflected immediately`() = runTest {
        val localDataSource = FakeAnalyticsConsentLocalDataSource()
        val reporter = SentryAnalyticsReporterImpl(dsn = "", consentLocalDataSource = localDataSource)

        reporter.setConsent(true)

        assertTrue(reporter.observeConsent().first())
        assertTrue(localDataSource.isConsentGranted())
    }

    @Test
    fun `trackFlowEvent and setCurrentScreen never throw regardless of consent or a blank dsn`() = runTest {
        // FR-011: a broken/unconfigured reporting pipeline must never crash the caller.
        val reporter = SentryAnalyticsReporterImpl(dsn = "", consentLocalDataSource = FakeAnalyticsConsentLocalDataSource())

        reporter.trackFlowEvent(KeyFlow.LOGIN, FlowOutcome.FAILURE)
        reporter.setCurrentScreen("login")
        reporter.setConsent(true)
        reporter.trackFlowEvent(KeyFlow.ORDERS_LIST, FlowOutcome.SUCCESS)
        reporter.setCurrentScreen("orders")
    }
}
