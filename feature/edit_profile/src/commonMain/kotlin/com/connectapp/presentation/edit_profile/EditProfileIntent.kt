package com.connectapp.presentation.edit_profile

sealed interface EditProfileIntent {
    data class FirstNameChanged(val value: String) : EditProfileIntent
    data class LastNameChanged(val value: String) : EditProfileIntent
    data class EmailChanged(val value: String) : EditProfileIntent
    data class PhoneChanged(val value: String) : EditProfileIntent
    data object SaveClicked : EditProfileIntent
    data object CancelClicked : EditProfileIntent
}
