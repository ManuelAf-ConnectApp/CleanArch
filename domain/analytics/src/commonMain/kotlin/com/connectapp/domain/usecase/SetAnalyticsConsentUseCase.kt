package com.connectapp.domain.usecase

import com.connectapp.domain.analytics.AnalyticsReporter

class SetAnalyticsConsentUseCase(
    private val analyticsReporter: AnalyticsReporter,
) {
    operator fun invoke(granted: Boolean) {
        analyticsReporter.setConsent(granted)
    }
}
