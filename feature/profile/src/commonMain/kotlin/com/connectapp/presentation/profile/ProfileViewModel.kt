package com.connectapp.presentation.profile

import androidx.lifecycle.viewModelScope
import com.connectapp.core.mvi.MviViewModel
import com.connectapp.domain.usecase.LogoutUseCase
import com.connectapp.domain.usecase.ObserveProfileFetchedAtUseCase
import com.connectapp.domain.usecase.ObserveProfileUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * See specs/010-offline-cache-layer/contracts/auth-repository-profile-logout-contract.md.
 * [state].user is driven entirely by [observeProfileUseCase] (the cache seeded at the last
 * successful login) — there is no network "refresh profile" call to make (research.md D6), so
 * this ViewModel never fabricates or simulates a User.
 */
class ProfileViewModel(
    private val observeProfileUseCase: ObserveProfileUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val observeProfileFetchedAtUseCase: ObserveProfileFetchedAtUseCase,
) : MviViewModel<ProfileState, ProfileIntent, ProfileEffect>(ProfileState()) {

    init {
        updateState { it.copy(isLoading = true) }
        observeProfileUseCase()
            .onEach { user -> updateState { it.copy(isLoading = false, user = user) } }
            .launchIn(viewModelScope)
        observeProfileFetchedAtUseCase()
            .onEach { fetchedAt -> updateState { it.copy(lastFetchedAt = fetchedAt) } }
            .launchIn(viewModelScope)
    }

    override fun reduce(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.BackClicked -> {
                emitEffect(ProfileEffect.NavigateBack)
            }

            ProfileIntent.LogoutClicked -> {
                logout()
            }
        }
    }

    private fun logout() {
        updateState { it.copy(isLoading = true) }
        viewModelScope.launch {
            logoutUseCase()
            updateState { it.copy(isLoading = false) }
            emitEffect(ProfileEffect.NavigateToLogin)
        }
    }
}
