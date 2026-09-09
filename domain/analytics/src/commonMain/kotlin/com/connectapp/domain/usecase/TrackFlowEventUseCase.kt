package com.connectapp.domain.usecase

import com.connectapp.domain.analytics.AnalyticsReporter
import com.connectapp.domain.analytics.FailureCategory
import com.connectapp.domain.analytics.FlowOutcome
import com.connectapp.domain.analytics.KeyFlow

class TrackFlowEventUseCase(
    private val analyticsReporter: AnalyticsReporter,
) {
    operator fun invoke(flow: KeyFlow, outcome: FlowOutcome, failureCategory: FailureCategory? = null) {
        analyticsReporter.trackFlowEvent(flow, outcome, failureCategory)
    }
}
