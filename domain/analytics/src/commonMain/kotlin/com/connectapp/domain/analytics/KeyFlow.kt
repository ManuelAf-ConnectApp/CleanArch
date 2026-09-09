package com.connectapp.domain.analytics

/**
 * The key flows tracked by [AnalyticsReporter] (specs/011-crash-analytics-reporting/spec.md
 * FR-004/FR-005). Only [LOGIN] and [ORDERS_LIST] have a real call site in this iteration —
 * [ORDER_DETAIL] and [PROFILE_UPDATE] depend on specs 009/008/007, none implemented yet
 * (see plan.md § Complexity Tracking). Defined now for forward compatibility.
 */
enum class KeyFlow {
    LOGIN,
    ORDERS_LIST,
    ORDER_DETAIL,
    PROFILE_UPDATE,
}
