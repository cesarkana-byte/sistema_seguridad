package com.example.seguridad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seguridad.data.model.AppUser
import com.example.seguridad.data.model.Incident
import com.example.seguridad.data.model.IncidentStatus
import com.example.seguridad.data.model.SecurityReport
import com.example.seguridad.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReportsUiState(
    val isLoading: Boolean = false,
    val reports: List<SecurityReport> = emptyList(),
    val error: String? = null
)

class ReportsViewModel : ViewModel() {

    private val reportRepository = ReportRepository()

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    fun loadReports() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val reports = reportRepository.getReports()
                _uiState.value = ReportsUiState(reports = reports)
            } catch (e: Exception) {
                _uiState.value = ReportsUiState(error = "Error al cargar reportes.")
            }
        }
    }

    fun generateReport(user: AppUser, incidents: List<Incident>) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val pendientes = incidents.count { it.estado == IncidentStatus.PENDIENTE.name }
                val enAtencion = incidents.count { it.estado == IncidentStatus.EN_ATENCION.name }
                val atendidas = incidents.count { it.estado == IncidentStatus.ATENDIDA.name }
                val descartadas = incidents.count { it.estado == IncidentStatus.DESCARTADA.name }
                val altoRiesgo = incidents.count { it.riesgoIA.equals("Alto", ignoreCase = true) }

                val detalle = """
                    REPORTE DE SEGURIDAD CIUDADANA AREQUIPA

                    Total de incidencias: ${incidents.size}
                    Pendientes: $pendientes
                    En atención: $enAtencion
                    Atendidas: $atendidas
                    Descartadas: $descartadas
                    Alto riesgo según IA: $altoRiesgo

                    Recomendación:
                    Priorizar incidencias pendientes y de alto riesgo para asignación policial.
                """.trimIndent()

                val report = SecurityReport(
                    generadoPorId = user.uid,
                    generadoPorNombre = user.nombre,
                    totalIncidencias = incidents.size,
                    pendientes = pendientes,
                    enAtencion = enAtencion,
                    atendidas = atendidas,
                    descartadas = descartadas,
                    altoRiesgo = altoRiesgo,
                    detalle = detalle
                )

                reportRepository.saveReport(report)
                loadReports()
            } catch (e: Exception) {
                _uiState.value = ReportsUiState(error = "Error al generar reporte.")
            }
        }
    }
}