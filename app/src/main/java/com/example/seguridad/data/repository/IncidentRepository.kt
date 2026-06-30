package com.example.seguridad.data.repository

import com.example.seguridad.data.model.Incident
import com.example.seguridad.data.model.IncidentStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class IncidentRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val collection = db.collection("incidencias")

    suspend fun getAllIncidents(): List<Incident> {
        val snapshot = collection.get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Incident::class.java)?.copy(id = doc.id)
        }.sortedByDescending { it.fechaRegistro }
    }

    suspend fun addIncident(incident: Incident): String {
        val document = collection.document()
        val newIncident = incident.copy(id = document.id)
        document.set(newIncident).await()
        return document.id
    }

    suspend fun updateStatus(incidentId: String, status: IncidentStatus) {
        collection.document(incidentId).update(
            mapOf(
                "estado" to status.name,
                "fechaActualizacion" to System.currentTimeMillis()
            )
        ).await()
    }

    suspend fun assignPolice(
        incidentId: String,
        policeId: String,
        policeName: String
    ) {
        collection.document(incidentId).update(
            mapOf(
                "policiaAsignadoId" to policeId,
                "policiaAsignadoNombre" to policeName,
                "fechaActualizacion" to System.currentTimeMillis()
            )
        ).await()
    }
}