package com.connectapp.core.analytics

/**
 * Generic wrapper around the platform crash/analytics SDK. Deliberately unaware of any
 * product concept (no "KeyFlow", no domain type) — same principle as
 * core:database/core:storage/core:network: a `core:*` module is a reusable platform
 * primitive, replaceable by another provider without touching `domain` or `data:analytics`
 * (specs/011-crash-analytics-reporting/research.md D2).
 *
 * Every function is expected to swallow its own failures (research.md D6) — a broken
 * analytics pipeline must never crash the app it's trying to observe.
 */
expect fun initSentry(dsn: String)

expect fun trackEvent(name: String, data: Map<String, String>)

expect fun setTag(key: String, value: String)
