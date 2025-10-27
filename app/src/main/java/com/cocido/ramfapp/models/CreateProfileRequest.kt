package com.cocido.ramfapp.models

/**
 * Modelo para crear un perfil de usuario
 * Basado en la API del backend
 */
data class CreateProfileRequest(
    val dni: String,
    val phone: String,
    val enterpriseName: String,
    val jobPosition: String
)


