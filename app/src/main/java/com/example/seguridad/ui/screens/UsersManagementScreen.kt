package com.example.seguridad.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.seguridad.data.model.AppUser
import com.example.seguridad.data.model.UserRole
import com.example.seguridad.viewmodel.UsersUiState

@Composable
fun UsersManagementScreen(
    admin: AppUser,
    state: UsersUiState,
    onRefresh: () -> Unit,
    onChangeRole: (AppUser, UserRole) -> Unit,
    onToggleActive: (AppUser) -> Unit
) {
    var query by remember { mutableStateOf("") }

    val filteredUsers = state.users.filter { user ->
        val q = query.trim().lowercase()
        q.isBlank() ||
                user.nombre.lowercase().contains(q) ||
                user.correo.lowercase().contains(q) ||
                user.rol.lowercase().contains(q)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Gestion de usuarios",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            text = "Busca ciudadanos registrados y habilitalos como policia o administrador.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = query,
            onValueChange = { query = it },
            label = { Text("Buscar por nombre, correo o rol") },
            singleLine = true
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            onClick = onRefresh
        ) {
            Text("Actualizar usuarios")
        }

        state.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        state.successMessage?.let {
            Text(
                text = it,
                color = Color(0xFF2E7D32),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = filteredUsers,
                key = { user -> user.uid }
            ) { user ->
                UserAdminCard(
                    admin = admin,
                    user = user,
                    onChangeRole = onChangeRole,
                    onToggleActive = onToggleActive
                )
            }
        }
    }
}

@Composable
private fun UserAdminCard(
    admin: AppUser,
    user: AppUser,
    onChangeRole: (AppUser, UserRole) -> Unit,
    onToggleActive: (AppUser) -> Unit
) {
    val isCurrentAdmin = admin.uid == user.uid

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
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = user.nombre.ifBlank { "Usuario sin nombre" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = user.correo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RoleChip(user.rol)
                ActiveChip(user.activo)
            }

            Text(
                text = if (isCurrentAdmin) {
                    "Esta es tu cuenta actual. No puedes quitarte permisos ni desactivarte."
                } else {
                    "Asignar rol del usuario"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = !isCurrentAdmin,
                    onClick = {
                        onChangeRole(user, UserRole.CIUDADANO)
                    }
                ) {
                    Text("Ciudadano")
                }

                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = !isCurrentAdmin,
                    onClick = {
                        onChangeRole(user, UserRole.POLICIA)
                    }
                ) {
                    Text("Policia")
                }
            }

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCurrentAdmin,
                onClick = {
                    onChangeRole(user, UserRole.ADMINISTRADOR)
                }
            ) {
                Text("Hacer administrador")
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCurrentAdmin,
                onClick = {
                    onToggleActive(user)
                }
            ) {
                Text(
                    text = if (user.activo) {
                        "Desactivar cuenta"
                    } else {
                        "Activar cuenta"
                    }
                )
            }
        }
    }
}

@Composable
private fun RoleChip(role: String) {
    Surface(
        shape = CircleShape,
        color = Color(0xFFFFE8CC)
    ) {
        Text(
            text = "Rol: $role",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF9B581D)
        )
    }
}

@Composable
private fun ActiveChip(active: Boolean) {
    val text = if (active) "Activo" else "Inactivo"
    val color = if (active) Color(0xFF2E7D32) else Color(0xFFC62828)

    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
