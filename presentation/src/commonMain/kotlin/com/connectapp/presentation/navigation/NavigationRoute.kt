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

    data object BookingsRoute : NavigationRoute

    @Serializable
    data class RestaurantDetailRoute(val restaurantId: String) : NavigationRoute

    @Serializable
    data class BookingConfirmationRoute(
        val restaurantId: String,
        val restaurantName: String,
        val date: String,
        val time: String,
        val guests: Int,
        val paymentMethod: String,
        val totalCents: Int,
    ) : NavigationRoute

    @Serializable
    data class BookingDetailRoute(
        val bookingId: String,
        val title: String,
        val status: String,
        val date: String,
        val guests: Int,
        val totalCents: Long,
        val paymentMethod: String,
        val cancellationWindowHours: Int,
    ) : NavigationRoute

    @Serializable
    data object EditProfileRoute : NavigationRoute

    @Serializable
    data object PrivacyPolicyRoute : NavigationRoute

    val showBackButton: Boolean get() = this !is HomeRoute && this !is SplashRoute && this !is LoginRoute
    val showTopBar: Boolean get() = this !is SplashRoute && this !is LoginRoute
    val showBottomBar: Boolean
        get() = this !is SplashRoute && this !is LoginRoute && this !is RegisterRoute &&
            this !is ForgotPasswordRoute && this !is BookingDetailRoute && this !is RestaurantDetailRoute &&
            this !is BookingConfirmationRoute &&
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
            BookingsRoute -> orders_title
            is RestaurantDetailRoute -> main_title
            is BookingConfirmationRoute -> main_title
            is BookingDetailRoute -> order_detail_title
            EditProfileRoute -> edit_profile_title
            PrivacyPolicyRoute -> privacy_policy_title
        }
}
