package com.connectapp.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.connectapp.domain.model.DemoScenario
import kotlinx.coroutines.launch

data class BookingDraft(
    val restaurantId: String,
    val restaurantName: String,
    val date: String,
    val time: String,
    val guests: Int,
    val paymentMethod: String,
    val totalCents: Int,
    val scenario: DemoScenario,
)

@Composable
fun RestaurantDetailScreen(
    restaurant: RestaurantPreview,
    modifier: Modifier = Modifier,
    onBook: suspend (BookingDraft) -> Result<Unit>,
) {
    var guests by remember { mutableIntStateOf(2) }
    var selectedTime by remember { mutableStateOf("20:30") }
    var selectedDate by remember { mutableStateOf("Mañana") }
    var payment by remember { mutableStateOf("Tarjeta (demo)") }
    var scenario by remember { mutableStateOf(DemoScenario.SUCCESS) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val totalCents = restaurant.reservationPriceCents * guests
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(restaurant.name, style = MaterialTheme.typography.headlineMedium)
        Text("${restaurant.cuisine.label} · ${restaurant.city}", color = MaterialTheme.colorScheme.primary)
        Text(restaurant.description)
        Text("Tarifa simulada de reserva: ${restaurant.reservationPriceCents / 100} € por persona. No incluye el consumo en el restaurante.")
        Text("Cancelación gratuita hasta 24 horas antes de la hora local del establecimiento.", style = MaterialTheme.typography.bodyMedium)
        Text("Disponibilidad demo", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Mañana", "Pasado mañana", "Este sábado").forEach { date ->
                FilterChip(selected = selectedDate == date, onClick = { selectedDate = date }, label = { Text(date) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("20:00", "20:30", "21:00").forEach { time ->
                FilterChip(selected = selectedTime == time, onClick = { selectedTime = time }, label = { Text(time) })
            }
        }
        Text("Comensales: $guests")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { if (guests > 1) guests-- }) { Text("−") }
            OutlinedButton(onClick = { if (guests < 8) guests++ }) { Text("+") }
        }
        Text("Método de pago", style = MaterialTheme.typography.titleMedium)
        listOf("Tarjeta (demo)", "PayPal (demo)", "Google Pay (demo)", "Apple Pay (demo)").forEach { method ->
            Row { RadioButton(selected = payment == method, onClick = { payment = method }); Text(method, modifier = Modifier.padding(top = 12.dp)) }
        }
        Text("Respuesta simulada", style = MaterialTheme.typography.titleMedium)
        DemoScenario.entries.forEach { option ->
            Row {
                RadioButton(selected = scenario == option, onClick = { scenario = option; errorMessage = null })
                Text(option.label, modifier = Modifier.padding(top = 12.dp))
            }
        }
        Text("Total demo: ${totalCents / 100} €", style = MaterialTheme.typography.titleLarge)
        Text("Simulación: no se realizará ningún cobro.", color = MaterialTheme.colorScheme.error)
        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = {
                scope.launch {
                    isSubmitting = true
                    val result = onBook(
                        BookingDraft(
                            restaurantId = restaurant.id,
                            restaurantName = restaurant.name,
                            date = selectedDate,
                            time = selectedTime,
                            guests = guests,
                            paymentMethod = payment,
                            totalCents = totalCents,
                            scenario = scenario,
                        ),
                    )
                    errorMessage = result.exceptionOrNull()?.let { scenario.failureMessage }
                    isSubmitting = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting,
        ) { Text(if (isSubmitting) "Procesando…" else "Confirmar reserva de demostración") }
    }
}

private val DemoScenario.label: String
    get() = when (this) {
        DemoScenario.SUCCESS -> "Éxito"
        DemoScenario.TIMEOUT -> "Tiempo de espera agotado"
        DemoScenario.UNAUTHORIZED -> "Sesión no autorizada"
        DemoScenario.CONFLICT -> "Horario no disponible"
        DemoScenario.SERVER_ERROR -> "Error de servidor"
        DemoScenario.PAYMENT_DECLINED -> "Pago rechazado"
    }

private val DemoScenario.failureMessage: String
    get() = when (this) {
        DemoScenario.TIMEOUT -> "La solicitud ha tardado demasiado. Inténtalo de nuevo."
        DemoScenario.UNAUTHORIZED -> "Tu sesión no está autorizada para reservar."
        DemoScenario.CONFLICT -> "Ese horario ya no está disponible. Elige otro."
        DemoScenario.SERVER_ERROR -> "No se ha podido completar la reserva por un error del servidor."
        DemoScenario.PAYMENT_DECLINED -> "El método de pago ha sido rechazado en la simulación."
        DemoScenario.SUCCESS -> ""
    }
