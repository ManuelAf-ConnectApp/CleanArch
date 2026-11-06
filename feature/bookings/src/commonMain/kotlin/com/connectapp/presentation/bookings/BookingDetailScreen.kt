package com.connectapp.presentation.bookings

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.connectapp.commonresources.order_detail_date_label
import com.connectapp.commonresources.order_detail_id_label
import com.connectapp.commonresources.order_detail_status_label
import com.connectapp.commonresources.booking_cancel_action
import com.connectapp.commonresources.booking_cancel_message
import com.connectapp.commonresources.booking_cancel_title
import com.connectapp.commonresources.booking_demo_amount_label
import com.connectapp.commonresources.booking_demo_payment_label
import com.connectapp.commonresources.booking_guests_label
import com.connectapp.commonresources.booking_keep_action
import org.jetbrains.compose.resources.stringResource

/**
 * Pure Composable, no ViewModel (specs/009-order-detail-and-refresh/research.md §4): all 4
 * fields are already loaded by [BookingsScreen]'s list — showing them here never triggers a
 * network call (FR-006/SC-004). [status] is the raw `Booking.status.name` (research.md §2 —
 * the route contract carries plain strings, not the `:domain` enum), mapped back to a label
 * the same way [com.connectapp.presentation.bookings.component.BookingCard] already does.
 *
 * No local back button: the app shell's `CustomTopBar` already renders one for every route with
 * `showBackButton` (`NavigationRoute.kt`), `BookingDetailRoute` included — a second one here would
 * just duplicate it. Same reasoning for status bar insets: no `statusBarsPadding()` here — the
 * app shell's `Scaffold` (`MainScreen.kt`) already reserves that space once for every screen.
 */
@Composable
fun BookingDetailScreen(
    modifier: Modifier = Modifier,
    bookingId: String,
    title: String,
    status: String,
    date: String,
    guests: Int,
    totalCents: Long,
    paymentMethod: String,
    cancellationWindowHours: Int,
    onCancel: (() -> Unit)? = null,
) {
    var showCancellationDialog by remember { mutableStateOf(false) }
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
                DetailRow(label = stringResource(order_detail_id_label), value = bookingId)
                Spacer(modifier = Modifier.height(12.dp))
                DetailRow(label = stringResource(order_detail_status_label), value = status.toStatusLabel())
                Spacer(modifier = Modifier.height(12.dp))
                DetailRow(label = stringResource(order_detail_date_label), value = date)
                Spacer(modifier = Modifier.height(12.dp))
                DetailRow(label = stringResource(booking_guests_label), value = guests.toString())
                Spacer(modifier = Modifier.height(12.dp))
                DetailRow(label = stringResource(booking_demo_amount_label), value = "${totalCents / 100} €")
                Spacer(modifier = Modifier.height(12.dp))
                DetailRow(label = stringResource(booking_demo_payment_label), value = paymentMethod)
            }
        }

        if (onCancel != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Esta reserva es de demostración y puede cancelarse sin coste hasta $cancellationWindowHours horas antes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = { showCancellationDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(booking_cancel_action))
            }
        }
    }

    if (showCancellationDialog && onCancel != null) {
        AlertDialog(
            onDismissRequest = { showCancellationDialog = false },
            title = { Text(stringResource(booking_cancel_title)) },
            text = { Text(stringResource(booking_cancel_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showCancellationDialog = false
                    onCancel()
                }) { Text(stringResource(booking_cancel_action)) }
            },
            dismissButton = {
                TextButton(onClick = { showCancellationDialog = false }) { Text(stringResource(booking_keep_action)) }
            },
        )
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
    "CONFIRMED" -> "Confirmada"
    "CANCELLED" -> "Cancelada"
    else -> this
}
