package com.example.seguridad.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.seguridad.data.model.AppUser
import com.example.seguridad.data.model.DashboardStats

@Composable
fun DashboardScreen(
    user: AppUser,
    stats: DashboardStats
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Bienvenido, ${user.nombre}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text("Rol: ${user.rol}")

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Total", stats.total.toString(), Modifier.weight(1f))
            StatCard("Pendientes", stats.pendientes.toString(), Modifier.weight(1f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Atendidas", stats.atendidas.toString(), Modifier.weight(1f))
            StatCard("Alto riesgo IA", stats.altoRiesgo.toString(), Modifier.weight(1f))
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Lógica del sistema",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "La app permite registrar incidencias, analizarlas con IA, guardarlas en Firestore y visualizarlas según el rol del usuario."
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title)
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}