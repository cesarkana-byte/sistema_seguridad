package com.example.seguridad.data.model

data class DashboardStats(
    val total: Int = 0,
    val pendientes: Int = 0,
    val enAtencion: Int = 0,
    val atendidas: Int = 0,
    val descartadas: Int = 0,
    val altoRiesgo: Int = 0
)