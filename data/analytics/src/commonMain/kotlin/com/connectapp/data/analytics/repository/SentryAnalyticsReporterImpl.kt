package com.connectapp.data.analytics.repository

import com.connectapp.core.analytics.initSentry
import com.connectapp.core.analytics.setTag
import com.connectapp.core.analytics.trackEvent
import com.connectapp.data.analytics.datasource.AnalyticsConsentLocalDataSource
import com.connectapp.domain.analytics.AnalyticsReporter
import com.connectapp.domain.analytics.FailureCategory
import com.connectapp.domain.analytics.FlowOutcome
import com.connectapp.domain.analytics.KeyFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * See specs/011-crash-analytics-reporting/contracts/analytics-reporter-contract.md.
 *
 * Consent gates everything at this layer, not inside the SDK (research.md D5): while
 * [consentGranted] is `false`, [core.analytics.initSentry] is never called, so the underlying
 * SDK never starts and no network call can possibly escape.
 */
class SentryAnalyticsReporterImpl(
    private val dsn: String,
    private val consentLocalDataSource: AnalyticsConsentLocalDataSource,
) : AnalyticsReporter {

    private val consentGranted: MutableStateFlow<Boolean> =
        MutableStateFlow(consentLocalDataSource.isConsentGranted())

    init {
        if (consentGranted.value) {
            initSentry(dsn)
        }
    }

    override fun setConsent(granted: Boolean) {
        consentLocalDataSource.setConsentGranted(granted)
        consentGranted.value = granted
        if (granted) {
            initSentry(dsn)
        }
    }

    override fun observeConsent(): StateFlow<Boolean> = consentGranted.asStateFlow()

    override fun setCurrentScreen(screen: String) {
        if (!consentGranted.value) return
        setTag("screen", screen)
    }

    override fun trackFlowEvent(flow: KeyFlow, outcome: FlowOutcome, failureCategory: FailureCategory?) {
        if (!consentGranted.value) return
        val data = buildMap {
            put("flow", flow.name)
            put("outcome", outcome.name)
            failureCategory?.let { put("failure_category", it.name) }
        }
        trackEvent(name = "${flow.name}_${outcome.name}", data = data)
    }
}
