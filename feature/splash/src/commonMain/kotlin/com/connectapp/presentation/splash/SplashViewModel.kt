package com.connectapp.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.usecase.GetSessionStatusUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SplashViewModel(
    private val getSessionStatusUseCase: GetSessionStatusUseCase,
) : ViewModel() {

    private val _effect = MutableSharedFlow<SplashEffect>()
    val effect: SharedFlow<SplashEffect> = _effect.asSharedFlow()

    init {
        startTimer()
    }

    private fun startTimer() {
        viewModelScope.launch {
            delay(3000)
            val hasActiveSession = getSessionStatusUseCase()
            _effect.emit(
                if (hasActiveSession) SplashEffect.NavigateToHome else SplashEffect.NavigateToLogin
            )
        }
    }
}
