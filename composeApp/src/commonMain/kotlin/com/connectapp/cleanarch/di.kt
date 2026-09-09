package com.connectapp.cleanarch

import com.connectapp.cleanarch.config.AppConfig
import com.connectapp.cleanarch.config.AppVersionProviderImpl
import com.connectapp.cleanarch.config.NotificationPermissionProviderImpl
import com.connectapp.data.analytics.di.analyticsModule
import com.connectapp.data.di.dataModule
import com.connectapp.data.orders.di.ordersDataModule
import com.connectapp.data.settings.di.settingsDataModule
import com.connectapp.domain.di.analyticsDomainModule
import com.connectapp.domain.di.authDomainModule
import com.connectapp.domain.di.ordersDomainModule
import com.connectapp.domain.provider.AppVersionProvider
import com.connectapp.domain.provider.NotificationPermissionProvider
import com.connectapp.presentation.di.coreModule
import com.connectapp.presentation.edit_profile.di.editProfileModule
import com.connectapp.presentation.forgot_password.di.forgotPasswordModule
import com.connectapp.presentation.home.di.homeModule
import com.connectapp.presentation.login.di.loginModule
import com.connectapp.presentation.orders.di.ordersModule
import com.connectapp.presentation.profile.di.profileModule
import com.connectapp.presentation.register.di.registerModule
import com.connectapp.presentation.settings.di.settingsModule
import com.connectapp.presentation.splash.di.splashModule
import org.koin.dsl.module

/**
 * Todos los módulos Koin de la app: uno por módulo de dominio/datos (`003-decentralize-domain-data-di`)
 * más el compartido (`coreModule`, de `:presentation`) y el módulo Koin propio de cada feature
 * (`002-feature-modularization`, FR-005) — cada uno declarado dentro de su propio módulo Gradle,
 * nunca construido aquí. `dataModule`/`analyticsModule` reciben como parámetro el único dato que
 * solo `composeApp` conoce (`AppConfig`), sin que ese valor viaje como un tipo de otra capa.
 * `appConfigModule` registra [AppVersionProvider] directamente aquí en vez de en un módulo Gradle
 * propio — es la única implementación de ese contrato, y solo `composeApp` conoce `AppConfig`.
 * [NotificationPermissionProvider] sigue el mismo criterio (specs/015-notification-permissions,
 * research.md §2): su única implementación reenvía a `:core:notifications` sin lógica propia que
 * justifique un módulo `:data:notifications` nuevo.
 */
private val appConfigModule = module {
    single<AppVersionProvider> { AppVersionProviderImpl() }
    single<NotificationPermissionProvider> { NotificationPermissionProviderImpl() }
}

val appModules = listOf(
    authDomainModule,
    ordersDomainModule,
    analyticsDomainModule,
    dataModule(AppConfig.AUTH_API_BASE_URL),
    ordersDataModule,
    settingsDataModule,
    analyticsModule(AppConfig.SENTRY_DSN),
    appConfigModule,
    coreModule,
    splashModule,
    loginModule,
    registerModule,
    forgotPasswordModule,
    homeModule,
    settingsModule,
    profileModule,
    ordersModule,
    editProfileModule,
    // feature:privacy_policy has no ViewModel/Koin module — pure static content
    // (specs/008-complete-edit-profile-privacy/research.md D2's companion decision).
)
