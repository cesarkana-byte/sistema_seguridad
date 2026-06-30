package com.example.seguridad.data.model

data class SecurityReport(
    val id: String = "",
    val fechaGeneracion: Long = System.currentTimeMillis(),
    val generadoPorId: String = "",
    val generadoPorNombre: String = "",
    val totalIncidencias: Int = 0,
    val pendientes: Int = 0,
    val enAtencion: Int = 0,
    val atendidas: Int = 0,
    val descartadas: Int = 0,
    val altoRiesgo: Int = 0,
    val detalle: String = ""
)