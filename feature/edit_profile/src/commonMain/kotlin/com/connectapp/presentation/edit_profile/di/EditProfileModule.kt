package com.connectapp.presentation.edit_profile.di

import com.connectapp.presentation.edit_profile.EditProfileViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val editProfileModule = module {
    viewModelOf(::EditProfileViewModel)
}
