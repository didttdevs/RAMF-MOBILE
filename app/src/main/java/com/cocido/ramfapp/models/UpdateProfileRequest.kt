package com.cocido.ramfapp.models

/**
 * Modelo para actualizar el perfil del usuario
 * Basado en la API del backend
 */
data class UpdateProfileRequest(
    val name: String,
    val lastName: String,
    val email: String,
    val phone: String? = null,
    val dni: String? = null,
    val jobPosition: String? = null,
    val company: String? = null
)

