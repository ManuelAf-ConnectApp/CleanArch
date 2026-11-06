package com.connectapp.presentation.bookings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.orders_showing_sample_data
import com.connectapp.commonresources.orders_subtitle
import com.connectapp.commonresources.orders_title
import com.connectapp.commonresources.showing_cached_data
import com.connectapp.domain.model.Booking
import com.connectapp.domain.model.BookingStatus
import com.connectapp.presentation.bookings.component.BookingCard
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

// Provisional: no backend is deployed/connected by this project (see README), so a fresh
// install would otherwise show either the "No orders yet" empty state or the full-screen error
// (every refresh fails with no backend to hit). This bundled sample fills that gap for demo
// purposes only -- it never reaches BookingsState/BookingsViewModel, so it can't be mistaken for real
// cached/backend data.
private val sampleBookings = listOf(
    Booking(id = "sample-1", title = "Reserva en La Mesa del Sol", status = BookingStatus.CONFIRMED, date = "Mañana, 20:30", guests = 2, totalCents = 2_400),
    Booking(id = "sample-2", title = "Reserva en Forno Verde", status = BookingStatus.CONFIRMED, date = "Sábado, 21:00", guests = 4, totalCents = 4_000),
    Booking(id = "sample-3", title = "Reserva en Sakura Barrio", status = BookingStatus.CANCELLED, date = "12 de noviembre, 20:00", guests = 2, totalCents = 3_000),
)

@Composable
fun BookingsScreen(
    modifier: Modifier = Modifier,
    onBookingClick: (Booking) -> Unit,
) {
    val viewModel: BookingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Pull-to-refresh (specs/009-order-detail-and-refresh) wraps every branch below, not just
    // the loaded-list one, since (FR-004) refreshing must never require a previous failure first.
    // No statusBarsPadding() here — MainScreen's Scaffold (the app shell) already reserves that
    // space once for every screen; adding it again here would double it under the TopBar.
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { viewModel.onIntent(BookingsIntent.Refresh) },
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
    ) {
        // Initial load only shows the centered spinner while there's nothing cached yet
        // (specs/009-order-detail-and-refresh) — a pull-to-refresh on an already-loaded list
        // uses state.isRefreshing (the box's own indicator) instead. A cached list stays on
        // screen through a failed refresh (FR-012, specs/010-offline-cache-layer) and the "may be
        // stale" banner inside BookingsList (state.isShowingCachedData) is the only visible sign
        // something failed.
        when {
            state.isLoading && state.bookings.isEmpty() -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            // Provisional: nothing cached, whether because nothing has loaded yet or because the
            // refresh failed (there's no backend deployed for this project, see README, so every
            // refresh fails) -- show the bundled sampleBookings instead of the empty/error state,
            // so the screen still demos what a populated list looks like.
            state.bookings.isEmpty() -> {
                BookingsList(
                    state = state.copy(bookings = sampleBookings),
                    onBookingClick = onBookingClick,
                    isSampleData = true,
                )
            }

            else -> {
                BookingsList(state = state, onBookingClick = onBookingClick)
            }
        }
    }
}

@Composable
private fun BookingsList(
    modifier: Modifier = Modifier,
    state: BookingsState,
    onBookingClick: (Booking) -> Unit,
    isSampleData: Boolean = false,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = stringResource(orders_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(orders_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (isSampleData) {
                Text(
                    text = stringResource(orders_showing_sample_data),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
                Spacer(modifier = Modifier.height(12.dp))
            } else if (state.isShowingCachedData) {
                Text(
                    text = stringResource(showing_cached_data),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        items(state.bookings) { booking ->
            BookingCard(booking = booking, onClick = { onBookingClick(booking) })
        }
    }
}
