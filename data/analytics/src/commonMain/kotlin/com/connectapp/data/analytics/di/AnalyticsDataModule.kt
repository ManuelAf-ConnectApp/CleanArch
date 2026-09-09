package com.connectapp.data.analytics.di

import com.connectapp.core.storage.createKVault
import com.connectapp.data.analytics.datasource.AnalyticsConsentLocalDataSource
import com.connectapp.data.analytics.datasource.AnalyticsConsentLocalDataSourceImpl
import com.connectapp.data.analytics.repository.SentryAnalyticsReporterImpl
import com.connectapp.domain.analytics.AnalyticsReporter
import org.koin.dsl.module

/** Store name passed to `:core:storage`'s createKVault() for analytics consent. */
private const val ANALYTICS_CONSENT_STORE_NAME = "connectapp_analytics_consent"

/**
 * specs/003-decentralize-domain-data-di: :data:analytics owns its own Koin module. [sentryDsn]
 * is passed in from composeApp's `AppConfig` (research.md D2) — this module never reads
 * `AppConfig` itself, since `:data:analytics` cannot depend on `:composeApp`.
 *
 * `createdAtStart = true` (unlike every other `single` here): [AnalyticsReporter] must exist
 * as soon as Koin starts, not lazily on first injection — a returning user who already
 * granted consent needs crash capture active from app launch, not only once they happen to
 * open Login/Orders/Settings (specs/011-crash-analytics-reporting/plan.md, T022).
 */
fun analyticsModule(sentryDsn: String) = module {
    single<AnalyticsConsentLocalDataSource> {
        AnalyticsConsentLocalDataSourceImpl(createKVault(name = ANALYTICS_CONSENT_STORE_NAME))
    }
    single<AnalyticsReporter>(createdAtStart = true) {
        SentryAnalyticsReporterImpl(dsn = sentryDsn, consentLocalDataSource = get())
    }
}
