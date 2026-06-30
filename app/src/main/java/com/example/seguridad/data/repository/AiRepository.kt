package com.example.seguridad.data.repository

import android.util.Log
import com.example.seguridad.BuildConfig
import com.example.seguridad.data.model.AiAnalysis
import com.google.ai.client.generativeai.GenerativeModel

class AiRepository {

    suspend fun analyzeIncident(description: String): AiAnalysis {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        val cleanDescription = description.trim()

        if (apiKey.isBlank() || apiKey.contains("TU_API_KEY", ignoreCase = true)) {
            return localAnalysis(cleanDescription, "LOCAL")
        }

        return try {
            val model = GenerativeModel(
                modelName = "gemini-3.5-flash",
                apiKey = apiKey
            )

            val prompt = """
                Eres una IA para una central de seguridad ciudadana de Arequipa.
                La app es usada por municipalidad, serenazgo, policias y administradores.
                No respondas como consejo al ciudadano.
                Responde como sistema de despacho policial.

                Incidencia:
                "$cleanDescription"

                Reglas obligatorias:
                - Si hay arma, amenaza directa, heridos, sangre, disparos, violencia grave o peligro inmediato: Riesgo Alto y Prioridad Urgente.
                - Si hay robo o hurto de monto alto, por ejemplo 1000 soles o mas: Riesgo Alto y Prioridad Urgente.
                - Si hay robo menor sin arma, sin violencia y sin monto alto: Riesgo Medio y Prioridad Normal.
                - Si es animal perdido, ruido, basura, alumbrado o reporte leve: Riesgo Bajo y Prioridad Baja.
                - La recomendacion debe indicar que accion debe tomar la central, no el ciudadano.
                - Debe sugerir cuantos efectivos enviar.

                Responde exactamente en este formato:
                Tipo: ...
                Riesgo: Bajo/Medio/Alto
                Prioridad: Baja/Normal/Urgente
                Recomendacion: ...
                Resumen: ...
            """.trimIndent()

            val response = model.generateContent(prompt)
            val parsed = parseResponse(response.text ?: "")

            applyOperationalRules(
                description = cleanDescription,
                base = parsed,
                source = "GEMINI"
            )
        } catch (e: Exception) {
            Log.e("GEMINI_ERROR", "Gemini fallo", e)
            localAnalysis(cleanDescription, "LOCAL")
        }
    }

    private fun parseResponse(text: String): AiAnalysis {
        fun value(label: String): String {
            return text.lines()
                .firstOrNull { it.startsWith(label, ignoreCase = true) }
                ?.substringAfter(":")
                ?.trim()
                .orEmpty()
        }

        return AiAnalysis(
            tipo = value("Tipo").ifBlank { "Incidencia ciudadana" },
            riesgo = normalizeRiesgo(value("Riesgo")).ifBlank { "Medio" },
            prioridad = normalizePrioridad(value("Prioridad")).ifBlank { "Normal" },
            recomendacion = value("Recomendacion").ifBlank { "Evaluar la incidencia desde la central de monitoreo." },
            resumen = value("Resumen").ifBlank { text.take(120) }
        )
    }

    private fun localAnalysis(description: String, source: String): AiAnalysis {
        val base = AiAnalysis(
            tipo = "Incidencia ciudadana",
            riesgo = "Medio",
            prioridad = "Normal",
            recomendacion = "Evaluar la incidencia desde la central de monitoreo.",
            resumen = "Analisis local aplicado."
        )

        return applyOperationalRules(
            description = description,
            base = base,
            source = source
        )
    }

    private fun applyOperationalRules(
        description: String,
        base: AiAnalysis,
        source: String
    ): AiAnalysis {
        val lower = normalizeText(description)
        val amount = extractAmount(lower)

        val hasCriticalDanger = listOf(
            "arma",
            "pistola",
            "cuchillo",
            "navaja",
            "disparo",
            "balazo",
            "herido",
            "herida",
            "sangre",
            "amenaza",
            "amenazando",
            "violencia",
            "agresion",
            "secuestro",
            "mano armada",
            "emergencia"
        ).any { lower.contains(it) }

        val hasTheft = listOf(
            "robo",
            "hurto",
            "asalt",
            "sustrajeron",
            "me quitaron",
            "me robaron",
            "delincuente"
        ).any { lower.contains(it) }

        val isLowReport = listOf(
            "animal perdido",
            "mascota perdida",
            "ruido",
            "basura",
            "alumbrado",
            "luz quemada",
            "mal estacionado",
            "informativo"
        ).any { lower.contains(it) }

        val isHighAmount = amount >= 1000

        return when {
            hasCriticalDanger -> {
                AiAnalysis(
                    tipo = "Emergencia de seguridad",
                    riesgo = "Alto",
                    prioridad = "Urgente",
                    recomendacion = "$source: Asignar 2 a 3 efectivos policiales o serenazgo de inmediato, enviar alerta al personal cercano y priorizar la incidencia en el mapa.",
                    resumen = "Caso con peligro inmediato para la seguridad."
                )
            }

            hasTheft && isHighAmount -> {
                AiAnalysis(
                    tipo = "Robo de monto alto",
                    riesgo = "Alto",
                    prioridad = "Urgente",
                    recomendacion = "$source: Asignar 2 efectivos policiales de inmediato, enviar aviso al policia o serenazgo disponible y registrar seguimiento del caso desde la central.",
                    resumen = "Robo o hurto con monto alto detectado."
                )
            }

            hasTheft -> {
                AiAnalysis(
                    tipo = "Robo o hurto",
                    riesgo = "Medio",
                    prioridad = "Normal",
                    recomendacion = "$source: Asignar 1 efectivo policial o serenazgo segun disponibilidad, verificar la zona y mantener seguimiento del caso.",
                    resumen = "Robo o hurto sin senales de arma, heridos o monto alto."
                )
            }

            isLowReport -> {
                AiAnalysis(
                    tipo = "Reporte ciudadano",
                    riesgo = "Bajo",
                    prioridad = "Baja",
                    recomendacion = "$source: No requiere despacho policial inmediato. Derivar al area correspondiente o revisar cuando exista disponibilidad.",
                    resumen = "Reporte de baja urgencia."
                )
            }

            else -> {
                val fixedRisk = normalizeRiesgo(base.riesgo).ifBlank { "Medio" }
                val fixedPriority = normalizePrioridad(base.prioridad).ifBlank { "Normal" }

                AiAnalysis(
                    tipo = base.tipo.ifBlank { "Incidencia ciudadana" },
                    riesgo = fixedRisk,
                    prioridad = fixedPriority,
                    recomendacion = "$source: Evaluar desde la central y asignar personal segun disponibilidad. ${base.recomendacion}",
                    resumen = base.resumen.ifBlank { "Incidencia clasificada para revision operativa." }
                )
            }
        }
    }

    private fun extractAmount(text: String): Int {
        val regex = Regex("(\\d{3,})\\s*(soles|s/|s\\.|dolares|usd)?")
        val match = regex.find(text) ?: return 0
        return match.groupValues[1].toIntOrNull() ?: 0
    }

    private fun normalizeText(text: String): String {
        return text
            .lowercase()
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .replace("ñ", "n")
    }

    private fun normalizeRiesgo(value: String): String {
        return when (value.trim().lowercase()) {
            "bajo" -> "Bajo"
            "medio" -> "Medio"
            "alto" -> "Alto"
            else -> ""
        }
    }

    private fun normalizePrioridad(value: String): String {
        return when (value.trim().lowercase()) {
            "baja" -> "Baja"
            "normal" -> "Normal"
            "urgente" -> "Urgente"
            else -> ""
        }
    }
}
