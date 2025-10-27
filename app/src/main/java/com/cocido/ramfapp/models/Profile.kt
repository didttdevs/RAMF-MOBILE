package com.cocido.ramfapp.models

/**
 * Modelo para el perfil del usuario
 * Basado en la API del backend
 */
data class Profile(
    val id: String,
    val dni: String,
    val phone: String,
    val enterpriseName: String,
    val jobPosition: String,
    val userId: String,
    val createdAt: String,
    val updatedAt: String
)