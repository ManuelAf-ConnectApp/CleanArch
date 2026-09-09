package com.connectapp.presentation.home

import com.connectapp.core.mvi.MviViewModel

class HomeViewModel : MviViewModel<HomeState, HomeIntent, HomeEffect>(HomeState()) {

    override fun reduce(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.ItemClicked -> {
                emitEffect(HomeEffect.ShowToast(intent.item))
            }
        }
    }
}
