package com.connectapp.presentation.edit_profile

sealed interface EditProfileEffect {
    /** Cancel/back — specs/008-complete-edit-profile-privacy FR-004. */
    data object NavigateBack : EditProfileEffect

    /** Save succeeded — spec.md US1 Acceptance Scenario 2 (FR-003): navigates to Profile, not back. */
    data object NavigateToProfile : EditProfileEffect
}
