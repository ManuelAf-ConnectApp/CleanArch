package com.connectapp.data.bookings.datasource

import com.connectapp.data.bookings.model.OrderDto
import com.connectapp.data.bookings.service.BookingsService

interface BookingsRemoteDataSource {
    suspend fun getBookings(): Result<List<OrderDto>>
}

class BookingsRemoteDataSourceImpl(
    private val bookingsService: BookingsService
) : BookingsRemoteDataSource {
    override suspend fun getBookings(): Result<List<OrderDto>> {
        return try {
            Result.success(bookingsService.getBookings())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
