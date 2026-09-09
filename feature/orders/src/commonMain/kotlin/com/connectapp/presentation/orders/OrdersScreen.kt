package com.connectapp.presentation.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.connectapp.domain.model.Order
import com.connectapp.domain.model.OrderStatus
import com.connectapp.presentation.orders.component.OrderCard
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

// Provisional: no backend is deployed/connected by this project (see README), so a fresh
// install would otherwise show either the "No orders yet" empty state or the full-screen error
// (every refresh fails with no backend to hit). This bundled sample fills that gap for demo
// purposes only -- it never reaches OrdersState/OrdersViewModel, so it can't be mistaken for real
// cached/backend data.
private val sampleOrders = listOf(
    Order(id = "sample-1", title = "Wireless Keyboard", status = OrderStatus.SHIPPED, date = "2026-08-28"),
    Order(id = "sample-2", title = "USB-C Hub", status = OrderStatus.PENDING, date = "2026-08-30"),
    Order(id = "sample-3", title = "Noise Cancelling Headphones", status = OrderStatus.DELIVERED, date = "2026-08-20"),
)

@Composable
fun OrdersScreen(
    modifier: Modifier = Modifier,
    onOrderClick: (Order) -> Unit,
) {
    val viewModel: OrdersViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Pull-to-refresh (specs/009-order-detail-and-refresh) wraps every branch below, not just
    // the loaded-list one, since (FR-004) refreshing must never require a previous failure first.
    // No statusBarsPadding() here — MainScreen's Scaffold (the app shell) already reserves that
    // space once for every screen; adding it again here would double it under the TopBar.
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { viewModel.onIntent(OrdersIntent.Refresh) },
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
    ) {
        // Initial load only shows the centered spinner while there's nothing cached yet
        // (specs/009-order-detail-and-refresh) — a pull-to-refresh on an already-loaded list
        // uses state.isRefreshing (the box's own indicator) instead. A cached list stays on
        // screen through a failed refresh (FR-012, specs/010-offline-cache-layer) and the "may be
        // stale" banner inside OrdersList (state.isShowingCachedData) is the only visible sign
        // something failed.
        when {
            state.isLoading && state.orders.isEmpty() -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            // Provisional: nothing cached, whether because nothing has loaded yet or because the
            // refresh failed (there's no backend deployed for this project, see README, so every
            // refresh fails) -- show the bundled sampleOrders instead of the empty/error state,
            // so the screen still demos what a populated list looks like.
            state.orders.isEmpty() -> {
                OrdersList(
                    state = state.copy(orders = sampleOrders),
                    onOrderClick = onOrderClick,
                    isSampleData = true,
                )
            }

            else -> {
                OrdersList(state = state, onOrderClick = onOrderClick)
            }
        }
    }
}

@Composable
private fun OrdersList(
    modifier: Modifier = Modifier,
    state: OrdersState,
    onOrderClick: (Order) -> Unit,
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

        items(state.orders) { order ->
            OrderCard(order = order, onClick = { onOrderClick(order) })
        }
    }
}
