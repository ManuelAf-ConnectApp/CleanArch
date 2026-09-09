package com.connectapp.domain.di

import com.connectapp.domain.usecase.ForgotPasswordUseCase
import com.connectapp.domain.usecase.GetSessionStatusUseCase
import com.connectapp.domain.usecase.LoginUseCase
import com.connectapp.domain.usecase.LogoutUseCase
import com.connectapp.domain.usecase.ObserveProfileFetchedAtUseCase
import com.connectapp.domain.usecase.ObserveProfileUseCase
import com.connectapp.domain.usecase.RegisterUseCase
import com.connectapp.domain.usecase.SetSessionRememberedUseCase
import com.connectapp.domain.usecase.UpdateProfileUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** specs/003-decentralize-domain-data-di: :domain:auth owns its own Koin module. */
val authDomainModule = module {
    factoryOf(::LoginUseCase)
    factoryOf(::RegisterUseCase)
    factoryOf(::ForgotPasswordUseCase)
    factoryOf(::GetSessionStatusUseCase)
    factoryOf(::ObserveProfileUseCase)
    factoryOf(::UpdateProfileUseCase)
    factoryOf(::LogoutUseCase)
    factoryOf(::ObserveProfileFetchedAtUseCase)
    factoryOf(::SetSessionRememberedUseCase)
}
