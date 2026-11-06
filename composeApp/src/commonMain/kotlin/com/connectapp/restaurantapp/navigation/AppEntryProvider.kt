package com.connectapp.restaurantapp.navigation

import androidx.compose.ui.Modifier
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.NavEntry
import com.connectapp.presentation.MainViewModel
import com.connectapp.presentation.edit_profile.EditProfileScreen
import com.connectapp.presentation.forgot_password.ForgotPasswordScreen
import com.connectapp.presentation.home.HomeScreen
import com.connectapp.presentation.home.BookingConfirmationScreen
import com.connectapp.presentation.home.BookingDraft
import com.connectapp.presentation.home.RestaurantDetailScreen
import com.connectapp.presentation.home.demoRestaurants
import com.connectapp.presentation.login.LoginScreen
import com.connectapp.presentation.navigation.NavigationRoute
import com.connectapp.presentation.bookings.BookingDetailScreen
import com.connectapp.presentation.bookings.BookingsScreen
import com.connectapp.presentation.privacy_policy.PrivacyPolicyScreen
import com.connectapp.presentation.profile.ProfileScreen
import com.connectapp.presentation.register.RegisterScreen
import com.connectapp.presentation.settings.SettingsScreen
import com.connectapp.presentation.splash.SplashScreen
import com.connectapp.domain.model.Booking
import com.connectapp.domain.model.BookingStatus
import com.connectapp.domain.model.DemoPaymentMethod
import com.connectapp.domain.model.DemoScenario
import com.connectapp.domain.model.DemoScenarioConfig
import com.connectapp.domain.usecase.CreateDemoBookingUseCase
import com.connectapp.domain.usecase.CancelDemoBookingUseCase
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Único punto del proyecto que importa las 7 `Screen` de feature a la vez y construye
 * el mapeo ruta -> pantalla (FR-004). Recrea, idéntico en contenido/lambdas de navegación,
 * el `when(key) { ... }` que antes vivía dentro de `NavigationScreen.kt` (`:presentation`).
 */
fun appEntryProvider(
    modifier: Modifier,
    viewModel: MainViewModel,
): (Any) -> NavEntry<out Any> = { key ->
    when (key) {
        is NavigationRoute.SplashRoute -> {
            NavEntry(key = key) {
                SplashScreen(
                    onNavigateToLogin = {
                        viewModel.updateNavigationState(NavigationRoute.LoginRoute)
                    },
                    onNavigateToHome = {
                        viewModel.updateNavigationState(NavigationRoute.HomeRoute)
                    }
                )
            }
        }

        is NavigationRoute.LoginRoute -> {
            NavEntry(key = key) {
                LoginScreen(
                    modifier = modifier,
                    onNavigateToRegister = {
                        viewModel.updateNavigationState(NavigationRoute.RegisterRoute)
                    },
                    onNavigateToForgotPassword = {
                        viewModel.updateNavigationState(NavigationRoute.ForgotPasswordRoute)
                    },
                    onNavigateToHome = {
                        viewModel.updateNavigationState(NavigationRoute.HomeRoute)
                    }
                )
            }
        }

        is NavigationRoute.RegisterRoute -> {
            NavEntry(key = key) {
                RegisterScreen(
                    modifier = modifier,
                    onNavigateBack = {
                        viewModel.updateNavigationState(NavigationRoute.LoginRoute)
                    },
                    onNavigateToHome = {
                        viewModel.updateNavigationState(NavigationRoute.HomeRoute)
                    }
                )
            }
        }

        is NavigationRoute.ForgotPasswordRoute -> {
            NavEntry(key = key) {
                ForgotPasswordScreen(
                    modifier = modifier,
                    onNavigateBack = {
                        viewModel.updateNavigationState(NavigationRoute.LoginRoute)
                    },
                )
            }
        }

        is NavigationRoute.HomeRoute -> {
            NavEntry(key = key) {
                HomeScreen(
                    modifier = modifier,
                    onRestaurantSelected = { restaurantId ->
                        viewModel.updateNavigationState(NavigationRoute.RestaurantDetailRoute(restaurantId))
                    }
                )
            }
        }

        is NavigationRoute.RestaurantDetailRoute -> {
            NavEntry(key = key) {
                val createDemoBooking: CreateDemoBookingUseCase = koinInject()
                demoRestaurants.firstOrNull { it.id == key.restaurantId }?.let { restaurant ->
                    RestaurantDetailScreen(
                        restaurant = restaurant,
                        modifier = modifier,
                        onBook = { booking ->
                            val result = createDemoBooking(
                                Booking(
                                    id = "demo-${booking.restaurantId}-${booking.time.replace(":", "")}",
                                    title = "Reserva en ${booking.restaurantName} · ${booking.guests} comensales",
                                    status = BookingStatus.CONFIRMED,
                                    date = "${booking.date}, ${booking.time}",
                                    guests = booking.guests,
                                    totalCents = booking.totalCents.toLong(),
                                    paymentMethod = booking.paymentMethod.toDemoPaymentMethod(),
                                ),
                                DemoScenarioConfig(scenario = booking.scenario),
                            )
                            if (result.isSuccess) {
                                viewModel.updateNavigationState(
                                    NavigationRoute.BookingConfirmationRoute(
                                        restaurantId = booking.restaurantId,
                                        restaurantName = booking.restaurantName,
                                        date = booking.date,
                                        time = booking.time,
                                        guests = booking.guests,
                                        paymentMethod = booking.paymentMethod,
                                        totalCents = booking.totalCents,
                                    )
                                )
                            }
                            result
                        },
                    )
                }
            }
        }

        is NavigationRoute.BookingConfirmationRoute -> {
            NavEntry(key = key) {
                BookingConfirmationScreen(
                    booking = BookingDraft(
                        restaurantId = key.restaurantId,
                        restaurantName = key.restaurantName,
                        date = key.date,
                        time = key.time,
                        guests = key.guests,
                        paymentMethod = key.paymentMethod,
                        totalCents = key.totalCents,
                        scenario = DemoScenario.SUCCESS,
                    ),
                    modifier = modifier,
                    onDone = { viewModel.updateNavigationState(NavigationRoute.HomeRoute) },
                )
            }
        }

        is NavigationRoute.SettingsRoute -> {
            NavEntry(key = key) {
                SettingsScreen(
                    modifier = modifier,
                    onLogout = {
                        viewModel.updateNavigationState(NavigationRoute.LoginRoute)
                    },
                    onNavigateToEditProfile = {
                        viewModel.updateNavigationState(NavigationRoute.EditProfileRoute)
                    },
                    onNavigateToPrivacyPolicy = {
                        viewModel.updateNavigationState(NavigationRoute.PrivacyPolicyRoute)
                    },
                    onShowToast = { message, actionLabel ->
                        viewModel.showSnackBar(
                            message = message,
                            actionLabel = actionLabel,
                        )
                    },
                )
            }
        }

        is NavigationRoute.EditProfileRoute -> {
            NavEntry(key = key) {
                EditProfileScreen(
                    modifier = modifier,
                    onNavigateBack = { viewModel.onBack() },
                    onSaved = { viewModel.updateNavigationState(NavigationRoute.ProfileRoute) },
                )
            }
        }

        is NavigationRoute.PrivacyPolicyRoute -> {
            NavEntry(key = key) {
                PrivacyPolicyScreen(
                    modifier = modifier,
                )
            }
        }

        is NavigationRoute.BookingsRoute -> {
            NavEntry(key = key) {
                BookingsScreen(
                    modifier = modifier,
                    onBookingClick = { booking ->
                        viewModel.updateNavigationState(
                            NavigationRoute.BookingDetailRoute(
                                bookingId = booking.id,
                                title = booking.title,
                                status = booking.status.name,
                                date = booking.date,
                                guests = booking.guests,
                                totalCents = booking.totalCents,
                                paymentMethod = booking.paymentMethod.name,
                                cancellationWindowHours = booking.cancellationPolicy.freeCancellationUntilHoursBefore,
                            )
                        )
                    },
                )
            }
        }

        is NavigationRoute.BookingDetailRoute -> {
            NavEntry(key = key) {
                val cancelDemoBooking: CancelDemoBookingUseCase = koinInject()
                val scope = rememberCoroutineScope()
                BookingDetailScreen(
                    modifier = modifier,
                    bookingId = key.bookingId,
                    title = key.title,
                    status = key.status,
                    date = key.date,
                    guests = key.guests,
                    totalCents = key.totalCents,
                    paymentMethod = key.paymentMethod,
                    cancellationWindowHours = key.cancellationWindowHours,
                    onCancel = if (key.status == "CONFIRMED") {
                        {
                            scope.launch {
                                cancelDemoBooking(key.bookingId)
                                viewModel.updateNavigationState(NavigationRoute.BookingsRoute)
                            }
                        }
                    } else {
                        null
                    },
                )
            }
        }

        is NavigationRoute.ProfileRoute -> {
            NavEntry(key = key) {
                ProfileScreen(
                    modifier = modifier,
                    onNavigateBack = {
                        viewModel.onBack()
                    },
                    onNavigateToLogin = {
                        viewModel.updateNavigationState(NavigationRoute.LoginRoute)
                    }
                )
            }
        }

        else -> {
            NavEntry(key = key) {

            }
        }
    }
}

private fun String.toDemoPaymentMethod(): DemoPaymentMethod = when {
    startsWith("PayPal") -> DemoPaymentMethod.PAYPAL
    startsWith("Google Pay") -> DemoPaymentMethod.GOOGLE_PAY
    startsWith("Apple Pay") -> DemoPaymentMethod.APPLE_PAY
    else -> DemoPaymentMethod.CARD
}
