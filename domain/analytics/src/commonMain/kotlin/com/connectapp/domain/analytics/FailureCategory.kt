package com.connectapp.domain.analytics

/**
 * A closed, generic categorization of why a [KeyFlow] failed — deliberately not a free-form
 * `String` (specs/011-crash-analytics-reporting/research.md D3): it must be structurally
 * impossible for a caller to pass user-entered text (an email, a password, an error message)
 * as an analytics payload.
 */
enum class FailureCategory {
    NETWORK,
    SERVER,
    INVALID_CREDENTIALS,
    VALIDATION,
    UNKNOWN,
}
