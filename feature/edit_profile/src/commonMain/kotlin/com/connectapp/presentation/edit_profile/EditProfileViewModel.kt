package com.connectapp.presentation.edit_profile

import androidx.lifecycle.viewModelScope
import com.connectapp.commonresources.error_required_field
import com.connectapp.commonresources.error_unexpected
import com.connectapp.core.mvi.MviViewModel
import com.connectapp.domain.model.User
import com.connectapp.domain.usecase.ObserveProfileUseCase
import com.connectapp.domain.usecase.UpdateProfileUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * See specs/008-complete-edit-profile-privacy/contracts/edit-profile-and-navigation-contract.md.
 * Seeds the form once from [observeProfileUseCase] (FR-002) — deliberately not an ongoing
 * `onEach`/`launchIn` subscription, which would overwrite in-progress edits on every
 * re-emission (e.g. right after this screen's own save succeeds).
 */
class EditProfileViewModel(
    private val observeProfileUseCase: ObserveProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
) : MviViewModel<EditProfileState, EditProfileIntent, EditProfileEffect>(EditProfileState()) {

    init {
        viewModelScope.launch {
            observeProfileUseCase().first()?.let { user ->
                updateState {
                    it.copy(
                        firstName = user.firstName,
                        lastName = user.lastName,
                        email = user.email,
                        phone = user.phone,
                    )
                }
            }
        }
    }

    override fun reduce(intent: EditProfileIntent) {
        when (intent) {
            is EditProfileIntent.FirstNameChanged -> updateState { it.copy(firstName = intent.value) }
            is EditProfileIntent.LastNameChanged -> updateState { it.copy(lastName = intent.value) }
            is EditProfileIntent.EmailChanged -> updateState { it.copy(email = intent.value) }
            is EditProfileIntent.PhoneChanged -> updateState { it.copy(phone = intent.value) }
            EditProfileIntent.SaveClicked -> save()
            EditProfileIntent.CancelClicked -> emitEffect(EditProfileEffect.NavigateBack)
        }
    }

    private fun save() {
        val current = state.value
        // FR-005: same "not blank" validation ForgotPasswordViewModel already uses — no email
        // format check, since no form in this project has one (spec.md Edge Cases).
        if (current.firstName.isBlank() || current.lastName.isBlank() || current.email.isBlank()) {
            updateState { it.copy(errorMessage = error_required_field) }
            return
        }

        updateState { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            updateProfileUseCase(
                User(
                    firstName = current.firstName,
                    lastName = current.lastName,
                    email = current.email,
                    phone = current.phone,
                )
            ).onSuccess {
                updateState { it.copy(isLoading = false) }
                emitEffect(EditProfileEffect.NavigateToProfile)
            }.onFailure {
                updateState { it.copy(isLoading = false, errorMessage = error_unexpected) }
            }
        }
    }
}
