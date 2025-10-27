package com.cocido.ramfapp.models

/**
 * Modelo para cambiar la contraseña del usuario
 * Basado en la API del backend
 */
data class ChangePasswordRequest(
    val password: String,
    val newPassword: String
)