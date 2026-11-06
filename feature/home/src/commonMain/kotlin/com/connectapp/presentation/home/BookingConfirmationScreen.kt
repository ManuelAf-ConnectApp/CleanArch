package com.connectapp.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BookingConfirmationScreen(
    booking: BookingDraft,
    modifier: Modifier = Modifier,
    onDone: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Reserva de demostración confirmada", style = MaterialTheme.typography.headlineSmall)
        Text("No se ha realizado ningún cargo ni se ha enviado una reserva a un restaurante.")
        Text(booking.restaurantName, style = MaterialTheme.typography.titleLarge)
        Text("${booking.date} a las ${booking.time} · ${booking.guests} comensales")
        Text("Método seleccionado: ${booking.paymentMethod}")
        Text(
            "Total simulado: ${booking.totalCents / 100} €",
            style = MaterialTheme.typography.titleMedium,
        )
        Text("Referencia demo: DEMO-${booking.restaurantId.uppercase()}-${booking.time.replace(":", "")}")
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Volver al inicio") }
    }
}
