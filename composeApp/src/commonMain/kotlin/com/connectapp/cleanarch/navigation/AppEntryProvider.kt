package com.connectapp.cleanarch.navigation

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import com.connectapp.presentation.MainViewModel
import com.connectapp.presentation.edit_profile.EditProfileScreen
import com.connectapp.presentation.forgot_password.ForgotPasswordScreen
import com.connectapp.presentation.home.HomeScreen
import com.connectapp.presentation.login.LoginScreen
import com.connectapp.presentation.navigation.NavigationRoute
import com.connectapp.presentation.orders.OrderDetailScreen
import com.connectapp.presentation.orders.OrdersScreen
import com.connectapp.presentation.privacy_policy.PrivacyPolicyScreen
import com.connectapp.presentation.profile.ProfileScreen
import com.connectapp.presentation.register.RegisterScreen
import com.connectapp.presentation.settings.SettingsScreen
import com.connectapp.presentation.splash.SplashScreen

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
                    onShowToast = { message, actionLabel ->
                        viewModel.showSnackBar(
                            message = message,
                            actionLabel = actionLabel,
                            onAction = {

                            }
                        )
                    }
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

        is NavigationRoute.OrdersRoute -> {
            NavEntry(key = key) {
                OrdersScreen(
                    modifier = modifier,
                    onOrderClick = { order ->
                        viewModel.updateNavigationState(
                            NavigationRoute.OrderDetailRoute(
                                orderId = order.id,
                                title = order.title,
                                status = order.status.name,
                                date = order.date,
                            )
                        )
                    },
                )
            }
        }

        is NavigationRoute.OrderDetailRoute -> {
            NavEntry(key = key) {
                OrderDetailScreen(
                    modifier = modifier,
                    orderId = key.orderId,
                    title = key.title,
                    status = key.status,
                    date = key.date,
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
