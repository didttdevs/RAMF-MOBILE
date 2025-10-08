package com.cocido.ramfapp.models

/**
 * Modelo para cambiar contraseña
 */
data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)
