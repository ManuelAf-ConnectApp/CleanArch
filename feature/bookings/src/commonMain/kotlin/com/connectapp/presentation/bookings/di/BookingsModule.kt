package com.connectapp.presentation.bookings.di

import com.connectapp.presentation.bookings.BookingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val bookingsModule = module {
    viewModelOf(::BookingsViewModel)
}
