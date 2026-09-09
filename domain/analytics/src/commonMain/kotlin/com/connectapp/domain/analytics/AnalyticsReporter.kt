package com.connectapp.domain.analytics

import kotlinx.coroutines.flow.Flow

/**
 * Port for crash/analytics reporting — see
 * specs/011-crash-analytics-reporting/contracts/analytics-reporter-contract.md for the full
 * guarantees. No implementation detail of the underlying provider leaks here: [KeyFlow],
 * [FlowOutcome] and [FailureCategory] are this project's own types, not the provider's.
 *
 * Uncaught-crash capture (FR-001) is intentionally not a method here — it's handled
 * automatically by the underlying SDK's own global exception hook once [setConsent] is
 * granted, not by any `domain`/`feature` code calling into this interface.
 */
interface AnalyticsReporter {

    /** Opt-in only (FR-012) — [observeConsent] starts at `false` until this is called with `true`. */
    fun setConsent(granted: Boolean)

    fun observeConsent(): Flow<Boolean>

    /** Breadcrumb for "what screen/flow was the user in" (FR-002) when a crash report is captured. */
    fun setCurrentScreen(screen: String)

    fun trackFlowEvent(flow: KeyFlow, outcome: FlowOutcome, failureCategory: FailureCategory? = null)
}
