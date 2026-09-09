plugins {
    id("cleanarch.kmp.base")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // api (not implementation): AuthRepository exposes Flow<...> directly to callers
            // (see specs/010-offline-cache-layer/contracts/), so it must be visible on their
            // compile classpath — same criterion already used by core:network/core:storage.
            api(libs.kotlinx.coroutines.core)
            // specs/003-decentralize-domain-data-di: domain/auth/di/AuthDomainModule.kt declares
            // its own Koin module instead of composeApp constructing its use cases directly.
            implementation(libs.koin.core)
            // LogoutUseCase orchestrates clearing OrdersRepository's cache on logout — see
            // specs/010-offline-cache-layer/contracts/auth-repository-profile-logout-contract.md,
            // research.md D7 ("the only place in domain that knows logout touches more than one
            // repository's cache").
            implementation(projects.domain.orders)
        }
    }
}
