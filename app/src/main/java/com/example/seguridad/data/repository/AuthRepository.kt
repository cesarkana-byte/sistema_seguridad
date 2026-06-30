package com.example.seguridad.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    fun currentUser(): FirebaseUser? = auth.currentUser

    suspend fun login(correo: String, password: String): FirebaseUser {
        val result = auth.signInWithEmailAndPassword(correo, password).await()
        return result.user ?: error("No se pudo obtener el usuario autenticado.")
    }

    suspend fun register(correo: String, password: String): FirebaseUser {
        val result = auth.createUserWithEmailAndPassword(correo, password).await()
        return result.user ?: error("No se pudo crear el usuario.")
    }

    fun logout() {
        auth.signOut()
    }
}