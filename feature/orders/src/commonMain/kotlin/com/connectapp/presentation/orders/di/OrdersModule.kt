package com.connectapp.presentation.orders.di

import com.connectapp.presentation.orders.OrdersViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val ordersModule = module {
    viewModelOf(::OrdersViewModel)
}
