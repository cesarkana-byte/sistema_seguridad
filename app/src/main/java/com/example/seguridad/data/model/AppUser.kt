package com.example.seguridad.data.model

data class AppUser(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val rol: String = UserRole.CIUDADANO.name,
    val activo: Boolean = true,
    val fechaRegistro: Long = System.currentTimeMillis()
)