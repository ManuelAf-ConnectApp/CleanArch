package com.connectapp.presentation.splash

sealed interface SplashEffect {
    object NavigateToLogin : SplashEffect
    object NavigateToHome : SplashEffect
}
