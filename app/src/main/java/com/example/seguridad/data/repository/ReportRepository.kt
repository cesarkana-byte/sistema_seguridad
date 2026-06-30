package com.example.seguridad.data.repository

import com.example.seguridad.data.model.SecurityReport
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ReportRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val collection = db.collection("reportes")

    suspend fun getReports(): List<SecurityReport> {
        val snapshot = collection.get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(SecurityReport::class.java)?.copy(id = doc.id)
        }.sortedByDescending { it.fechaGeneracion }
    }

    suspend fun saveReport(report: SecurityReport): String {
        val document = collection.document()
        val newReport = report.copy(id = document.id)
        document.set(newReport).await()
        return document.id
    }
}