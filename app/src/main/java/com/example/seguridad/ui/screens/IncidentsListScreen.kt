package com.example.seguridad.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.seguridad.data.model.AppUser
import com.example.seguridad.data.model.Incident
import com.example.seguridad.data.model.IncidentStatus
import com.example.seguridad.data.model.UserRole
import com.example.seguridad.viewmodel.IncidentsUiState

@Composable
fun IncidentsListScreen(
    user: AppUser,
    state: IncidentsUiState,
    onRefresh: () -> Unit,
    onStatusChange: (String, IncidentStatus) -> Unit,
    onAssignPolice: (String, AppUser) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Incidencias",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Panel operativo de reportes ciudadanos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            OutlinedButton(
                onClick = onRefresh,
                enabled = !state.isLoading
            ) {
                Text("Actualizar")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        state.error?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        state.successMessage?.let { message ->
            Text(
                text = message,
                color = Color(0xFF2E7D32),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (state.incidents.isEmpty()) {
            EmptyIncidentsCard()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(
                    items = state.incidents,
                    key = { incident -> incident.id }
                ) { incident ->
                    IncidentCard(
                        user = user,
                        incident = incident,
                        policeUsers = state.policeUsers,
                        isLoading = state.isLoading,
                        onStatusChange = onStatusChange,
                        onAssignPolice = onAssignPolice
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyIncidentsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "No hay incidencias asignadas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Cuando el administrador asigne una incidencia, aparecerá en este panel.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun IncidentCard(
    user: AppUser,
    incident: Incident,
    policeUsers: List<AppUser>,
    isLoading: Boolean,
    onStatusChange: (String, IncidentStatus) -> Unit,
    onAssignPolice: (String, AppUser) -> Unit
) {
    val context = LocalContext.current
    var showDetails by remember { mutableStateOf(false) }

    val estadoColor = getEstadoColor(incident.estado)
    val riesgoColor = getRiesgoColor(incident.riesgoIA)
    val prioridadColor = getPrioridadColor(incident.prioridadIA)

    if (showDetails) {
        IncidentDetailDialog(
            incident = incident,
            onDismiss = { showDetails = false },
            onOpenLocation = {
                val uri = Uri.parse(
                    "https://www.google.com/maps/search/?api=1&query=${incident.latitud},${incident.longitud}"
                )

                val intent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(intent)
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                showDetails = true
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .height(10.dp)
                        .width(10.dp)
                        .clip(CircleShape)
                        .background(riesgoColor)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = incident.descripcion.ifBlank { "Incidencia registrada" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = incident.ubicacion.ifBlank { "Ubicación no especificada" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Toca la tarjeta para ver detalles y ubicación",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfessionalChip(
                    label = incident.estado.ifBlank { "SIN ESTADO" },
                    color = estadoColor
                )

                ProfessionalChip(
                    label = "Riesgo IA: ${incident.riesgoIA.ifBlank { "No definido" }}",
                    color = riesgoColor
                )

                ProfessionalChip(
                    label = "Prioridad IA: ${incident.prioridadIA.ifBlank { "No definida" }}",
                    color = prioridadColor
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            InfoBlock(
                title = "Recomendación IA",
                body = incident.recomendacionIA.ifBlank {
                    "No se generó una recomendación para esta incidencia."
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoLine(
                label = "Policía asignado",
                value = if (incident.policiaAsignadoNombre.isNotBlank()) {
                    incident.policiaAsignadoNombre
                } else {
                    "Sin asignar"
                }
            )

            if (user.rol == UserRole.ADMINISTRADOR.name) {
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Asignar o reasignar policía",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (policeUsers.isEmpty()) {
                    Text(
                        text = "No hay policías activos registrados.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        policeUsers.forEach { police ->
                            OutlinedButton(
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isLoading,
                                onClick = {
                                    onAssignPolice(incident.id, police)
                                }
                            ) {
                                val label = if (incident.policiaAsignadoId == police.uid) {
                                    "Asignado a ${police.nombre}"
                                } else {
                                    "Asignar a ${police.nombre}"
                                }

                                Text(label)
                            }
                        }
                    }
                }
            }

            if (user.rol == UserRole.ADMINISTRADOR.name || user.rol == UserRole.POLICIA.name) {
                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    onClick = {
                        val nextStatus = when (incident.estado) {
                            IncidentStatus.PENDIENTE.name -> IncidentStatus.EN_ATENCION
                            IncidentStatus.EN_ATENCION.name -> IncidentStatus.ATENDIDA
                            else -> IncidentStatus.ATENDIDA
                        }

                        onStatusChange(incident.id, nextStatus)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Avanzar estado")
                }
            }
        }
    }
}

@Composable
private fun IncidentDetailDialog(
    incident: Incident,
    onDismiss: () -> Unit,
    onOpenLocation: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Detalle de incidencia",
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoLine(
                    label = "Descripción",
                    value = incident.descripcion.ifBlank { "Sin descripción" }
                )

                InfoLine(
                    label = "Ubicación",
                    value = incident.ubicacion.ifBlank { "Sin ubicación" }
                )

                InfoLine(
                    label = "Estado",
                    value = incident.estado.ifBlank { "Sin estado" }
                )

                InfoLine(
                    label = "Riesgo IA",
                    value = incident.riesgoIA.ifBlank { "No definido" }
                )

                InfoLine(
                    label = "Prioridad IA",
                    value = incident.prioridadIA.ifBlank { "No definida" }
                )

                InfoLine(
                    label = "Recomendación IA",
                    value = incident.recomendacionIA.ifBlank { "Sin recomendación" }
                )

                InfoLine(
                    label = "Coordenadas",
                    value = "${incident.latitud}, ${incident.longitud}"
                )

                InfoLine(
                    label = "Policía asignado",
                    value = if (incident.policiaAsignadoNombre.isNotBlank()) {
                        incident.policiaAsignadoNombre
                    } else {
                        "Sin asignar"
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onOpenLocation
            ) {
                Text("Abrir ubicación")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun ProfessionalChip(
    label: String,
    color: Color
) {
    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.13f),
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = 0.35f)
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 6.dp
            ),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun InfoBlock(
    title: String,
    body: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun InfoLine(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun getEstadoColor(estado: String): Color {
    return when (estado.trim().uppercase()) {
        IncidentStatus.PENDIENTE.name -> Color(0xFFF9A825)
        IncidentStatus.EN_ATENCION.name -> Color(0xFF1565C0)
        IncidentStatus.ATENDIDA.name -> Color(0xFF2E7D32)
        IncidentStatus.DESCARTADA.name -> Color(0xFF757575)
        else -> Color(0xFF8A4F20)
    }
}

private fun getRiesgoColor(riesgo: String): Color {
    return when (riesgo.trim().lowercase()) {
        "alto" -> Color(0xFFC62828)
        "medio" -> Color(0xFFF9A825)
        "bajo" -> Color(0xFF2E7D32)
        else -> Color(0xFF8A4F20)
    }
}

private fun getPrioridadColor(prioridad: String): Color {
    return when (prioridad.trim().lowercase()) {
        "urgente" -> Color(0xFFC62828)
        "alta" -> Color(0xFFC62828)
        "media" -> Color(0xFFF9A825)
        "normal" -> Color(0xFF1565C0)
        "baja" -> Color(0xFF2E7D32)
        else -> Color(0xFF8A4F20)
    }
}

