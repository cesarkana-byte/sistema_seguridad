package com.example.seguridad.data.model

data class Incident(
    val id: String = "",
    val descripcion: String = "",
    val ubicacion: String = "",
    val latitud: Double = -16.398866,
    val longitud: Double = -71.536961,
    val estado: String = IncidentStatus.PENDIENTE.name,

    val tipoIA: String = "",
    val riesgoIA: String = "",
    val prioridadIA: String = "",
    val recomendacionIA: String = "",

    val usuarioId: String = "",
    val usuarioNombre: String = "",

    val policiaAsignadoId: String = "",
    val policiaAsignadoNombre: String = "",

    val fechaRegistro: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis()
)