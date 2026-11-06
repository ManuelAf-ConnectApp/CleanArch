package com.connectapp.domain.model

/** Outcomes selectable in debug builds to exercise the MVP without a backend. */
enum class DemoScenario {
    SUCCESS,
    TIMEOUT,
    UNAUTHORIZED,
    CONFLICT,
    SERVER_ERROR,
    PAYMENT_DECLINED,
}

data class DemoScenarioConfig(
    val scenario: DemoScenario = DemoScenario.SUCCESS,
    val latencyMillis: Long = 0,
) {
    init {
        require(latencyMillis >= 0)
    }
}

class DemoScenarioException(
    val scenario: DemoScenario,
) : IllegalStateException("Demo booking scenario failed: $scenario")
