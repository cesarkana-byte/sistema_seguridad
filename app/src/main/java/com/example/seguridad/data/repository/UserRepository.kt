package com.example.seguridad.data.repository

import com.example.seguridad.data.model.AppUser
import com.example.seguridad.data.model.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val collection = db.collection("usuarios")

    suspend fun saveUser(user: AppUser) {
        collection.document(user.uid).set(user).await()
    }

    suspend fun getUser(uid: String): AppUser? {
        val document = collection.document(uid).get().await()
        return document.toObject(AppUser::class.java)
    }

    suspend fun getAllUsers(): List<AppUser> {
        val snapshot = collection.get().await()
        return snapshot.documents.mapNotNull { it.toObject(AppUser::class.java) }
    }

    suspend fun getPoliceUsers(): List<AppUser> {
        return getAllUsers().filter { it.rol == UserRole.POLICIA.name && it.activo }
    }

    suspend fun updateUserRole(
        uid: String,
        role: UserRole
    ) {
        collection.document(uid).update(
            mapOf(
                "rol" to role.name
            )
        ).await()
    }

    suspend fun updateUserActive(
        uid: String,
        active: Boolean
    ) {
        collection.document(uid).update(
            mapOf(
                "activo" to active
            )
        ).await()
    }
}

