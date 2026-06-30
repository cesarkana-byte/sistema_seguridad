package com.example.seguridad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seguridad.data.model.AppUser
import com.example.seguridad.data.model.DashboardStats
import com.example.seguridad.data.model.Incident
import com.example.seguridad.data.model.IncidentStatus
import com.example.seguridad.data.model.UserRole
import com.example.seguridad.data.repository.AiRepository
import com.example.seguridad.data.repository.IncidentRepository
import com.example.seguridad.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class IncidentsUiState(
    val isLoading: Boolean = false,
    val incidents: List<Incident> = emptyList(),
    val policeUsers: List<AppUser> = emptyList(),
    val stats: DashboardStats = DashboardStats(),
    val error: String? = null,
    val successMessage: String? = null
)

class IncidentsViewModel : ViewModel() {

    private val incidentRepository = IncidentRepository()
    private val userRepository = UserRepository()
    private val aiRepository = AiRepository()

    private val _uiState = MutableStateFlow(IncidentsUiState())
    val uiState: StateFlow<IncidentsUiState> = _uiState.asStateFlow()

    private var registrationInProgress = false

    fun loadIncidents(user: AppUser) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null
                )

                reloadVisibleIncidents(user)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar incidencias."
                )
            }
        }
    }

    fun registerIncident(
        user: AppUser,
        descripcion: String,
        ubicacion: String,
        latitudText: String,
        longitudText: String
    ) {
        if (registrationInProgress) {
            return
        }

        registrationInProgress = true

        viewModelScope.launch {
            try {
                val descripcionLimpia = descripcion.trim()
                val ubicacionLimpia = ubicacion.trim()

                if (descripcionLimpia.isBlank() || ubicacionLimpia.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Completa descripción y ubicación.",
                        successMessage = null
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null,
                    successMessage = null
                )

                val descripcionNormalizada =
                    normalizeDescriptionForDuplicate(descripcionLimpia)

                val alreadyExists = incidentRepository.getAllIncidents().any { existing ->
                    existing.usuarioId == user.uid &&
                            normalizeDescriptionForDuplicate(existing.descripcion) == descripcionNormalizada
                }

                if (alreadyExists) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Ya existe una incidencia con esa descripción. Cambia la descripción para registrar otra.",
                        successMessage = null
                    )
                    return@launch
                }

                val analysis = aiRepository.analyzeIncident(descripcionLimpia)

                val incident = Incident(
                    descripcion = descripcionLimpia,
                    ubicacion = ubicacionLimpia,
                    latitud = latitudText.toDoubleOrNull() ?: -16.398866,
                    longitud = longitudText.toDoubleOrNull() ?: -71.536961,
                    tipoIA = analysis.tipo,
                    riesgoIA = analysis.riesgo,
                    prioridadIA = analysis.prioridad,
                    recomendacionIA = analysis.recomendacion,
                    usuarioId = user.uid,
                    usuarioNombre = user.nombre,
                    policiaAsignadoId = "",
                    policiaAsignadoNombre = ""
                )

                incidentRepository.addIncident(incident)

                loadIncidents(user)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Incidencia registrada con análisis de IA.",
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al registrar incidencia.",
                    successMessage = null
                )
            } finally {
                registrationInProgress = false
            }
        }
    }
    fun updateStatus(
        user: AppUser,
        incidentId: String,
        status: IncidentStatus
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null
                )

                incidentRepository.updateStatus(incidentId, status)

                reloadVisibleIncidents(
                    user = user,
                    successMessage = "Estado actualizado correctamente."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No se pudo actualizar el estado."
                )
            }
        }
    }

    fun assignPolice(
        user: AppUser,
        incidentId: String,
        police: AppUser
    ) {
        viewModelScope.launch {
            try {
                if (user.rol != UserRole.ADMINISTRADOR.name) {
                    _uiState.value = _uiState.value.copy(
                        error = "Solo el administrador puede asignar policÃ­as."
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null
                )

                incidentRepository.assignPolice(
                    incidentId = incidentId,
                    policeId = police.uid,
                    policeName = police.nombre
                )

                reloadVisibleIncidents(
                    user = user,
                    successMessage = "Incidencia asignada a ${police.nombre}."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No se pudo asignar policÃ­a."
                )
            }
        }
    }

    fun assignFirstPolice(
        user: AppUser,
        incidentId: String
    ) {
        viewModelScope.launch {
            try {
                val police = userRepository.getPoliceUsers().firstOrNull()

                if (police == null) {
                    _uiState.value = _uiState.value.copy(
                        error = "No hay policÃ­as registrados."
                    )
                    return@launch
                }

                assignPolice(
                    user = user,
                    incidentId = incidentId,
                    police = police
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "No se pudo asignar policÃ­a."
                )
            }
        }
    }

    private suspend fun reloadVisibleIncidents(
        user: AppUser,
        successMessage: String? = null
    ) {
        val allIncidents = incidentRepository.getAllIncidents()
        val policeUsers = userRepository.getPoliceUsers()

        val visibleIncidents = when (user.rol) {
            UserRole.ADMINISTRADOR.name -> {
                allIncidents
            }

            UserRole.POLICIA.name -> {
                allIncidents.filter { incident ->
                    incident.policiaAsignadoId == user.uid
                }
            }

            else -> {
                allIncidents.filter { incident ->
                    incident.usuarioId == user.uid
                }
            }
        }

        _uiState.value = IncidentsUiState(
            isLoading = false,
            incidents = visibleIncidents,
            policeUsers = policeUsers,
            stats = calculateStats(visibleIncidents),
            error = null,
            successMessage = successMessage
        )
    }

    private fun normalizeDescriptionForDuplicate(text: String): String {
        return text
            .trim()
            .lowercase()
            .replace(Regex("\\s+"), " ")
    }
    private fun calculateStats(items: List<Incident>): DashboardStats {
        return DashboardStats(
            total = items.size,
            pendientes = items.count { it.estado == IncidentStatus.PENDIENTE.name },
            enAtencion = items.count { it.estado == IncidentStatus.EN_ATENCION.name },
            atendidas = items.count { it.estado == IncidentStatus.ATENDIDA.name },
            descartadas = items.count { it.estado == IncidentStatus.DESCARTADA.name },
            altoRiesgo = items.count { it.riesgoIA.equals("Alto", ignoreCase = true) }
        )
    }
}


