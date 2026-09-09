package com.connectapp.domain.fake

import com.connectapp.domain.analytics.AnalyticsReporter
import com.connectapp.domain.analytics.FailureCategory
import com.connectapp.domain.analytics.FlowOutcome
import com.connectapp.domain.analytics.KeyFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeAnalyticsReporter : AnalyticsReporter {

    private val consentFlow = MutableStateFlow(false)

    val screens = mutableListOf<String>()
    val trackedEvents = mutableListOf<Triple<KeyFlow, FlowOutcome, FailureCategory?>>()

    override fun setConsent(granted: Boolean) {
        consentFlow.value = granted
    }

    override fun observeConsent(): Flow<Boolean> = consentFlow

    override fun setCurrentScreen(screen: String) {
        if (!consentFlow.value) return
        screens += screen
    }

    override fun trackFlowEvent(flow: KeyFlow, outcome: FlowOutcome, failureCategory: FailureCategory?) {
        if (!consentFlow.value) return
        trackedEvents += Triple(flow, outcome, failureCategory)
    }
}
