package com.connectapp.presentation.navigation

import com.connectapp.commonresources.*
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

sealed interface NavigationRoute {
    @Serializable
    data object SplashRoute : NavigationRoute

    @Serializable
    data object HomeRoute : NavigationRoute

    @Serializable
    data object LoginRoute : NavigationRoute

    @Serializable
    data object RegisterRoute : NavigationRoute

    @Serializable
    data object ForgotPasswordRoute : NavigationRoute

    data object SettingsRoute : NavigationRoute

    data object ProfileRoute : NavigationRoute

    data object OrdersRoute : NavigationRoute

    @Serializable
    data class OrderDetailRoute(
        val orderId: String,
        val title: String,
        val status: String,
        val date: String,
    ) : NavigationRoute

    @Serializable
    data object EditProfileRoute : NavigationRoute

    @Serializable
    data object PrivacyPolicyRoute : NavigationRoute

    val showBackButton: Boolean get() = this !is HomeRoute && this !is SplashRoute && this !is LoginRoute
    val showTopBar: Boolean get() = this !is SplashRoute && this !is LoginRoute
    val showBottomBar: Boolean
        get() = this !is SplashRoute && this !is LoginRoute && this !is RegisterRoute &&
            this !is ForgotPasswordRoute && this !is OrderDetailRoute &&
            this !is EditProfileRoute && this !is PrivacyPolicyRoute

    val titleRes: StringResource
        get() = when (this) {
            SplashRoute -> main_title
            HomeRoute -> main_title
            LoginRoute -> login_title
            RegisterRoute -> register_title
            ForgotPasswordRoute -> forgot_password_title
            SettingsRoute -> settings
            ProfileRoute -> nav_profile
            OrdersRoute -> orders_title
            is OrderDetailRoute -> order_detail_title
            EditProfileRoute -> edit_profile_title
            PrivacyPolicyRoute -> privacy_policy_title
        }
}
