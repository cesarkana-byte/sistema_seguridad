package com.example.seguridad.data.model

data class AiAnalysis(
    val tipo: String = "Sin clasificar",
    val riesgo: String = "Medio",
    val prioridad: String = "Normal",
    val recomendacion: String = "Revisar la incidencia y asignar atención según disponibilidad.",
    val resumen: String = ""
)