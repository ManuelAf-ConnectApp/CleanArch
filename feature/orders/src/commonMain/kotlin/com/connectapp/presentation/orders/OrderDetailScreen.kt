package com.connectapp.presentation.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.connectapp.commonresources.order_detail_date_label
import com.connectapp.commonresources.order_detail_id_label
import com.connectapp.commonresources.order_detail_status_label
import com.connectapp.commonresources.order_status_delivered
import com.connectapp.commonresources.order_status_pending
import com.connectapp.commonresources.order_status_shipped
import org.jetbrains.compose.resources.stringResource

/**
 * Pure Composable, no ViewModel (specs/009-order-detail-and-refresh/research.md §4): all 4
 * fields are already loaded by [OrdersScreen]'s list — showing them here never triggers a
 * network call (FR-006/SC-004). [status] is the raw `Order.status.name` (research.md §2 —
 * the route contract carries plain strings, not the `:domain` enum), mapped back to a label
 * the same way [com.connectapp.presentation.orders.component.OrderCard] already does.
 *
 * No local back button: the app shell's `CustomTopBar` already renders one for every route with
 * `showBackButton` (`NavigationRoute.kt`), `OrderDetailRoute` included — a second one here would
 * just duplicate it. Same reasoning for status bar insets: no `statusBarsPadding()` here — the
 * app shell's `Scaffold` (`MainScreen.kt`) already reserves that space once for every screen.
 */
@Composable
fun OrderDetailScreen(
    modifier: Modifier = Modifier,
    orderId: String,
    title: String,
    status: String,
    date: String,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailRow(label = stringResource(order_detail_id_label), value = orderId)
                Spacer(modifier = Modifier.height(12.dp))
                DetailRow(label = stringResource(order_detail_status_label), value = status.toStatusLabel())
                Spacer(modifier = Modifier.height(12.dp))
                DetailRow(label = stringResource(order_detail_date_label), value = date)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun String.toStatusLabel(): String = when (this) {
    "PENDING" -> stringResource(order_status_pending)
    "SHIPPED" -> stringResource(order_status_shipped)
    "DELIVERED" -> stringResource(order_status_delivered)
    else -> this
}
