package com.connectapp.domain.usecase

import com.connectapp.domain.analytics.AnalyticsReporter
import kotlinx.coroutines.flow.Flow

class ObserveAnalyticsConsentUseCase(
    private val analyticsReporter: AnalyticsReporter,
) {
    operator fun invoke(): Flow<Boolean> = analyticsReporter.observeConsent()
}
