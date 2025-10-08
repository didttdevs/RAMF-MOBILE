package com.cocido.ramfapp.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: String,

    // La API puede enviar "name" o "first_name" dependiendo del endpoint
    @SerializedName("name") val name: String? = null,
    @SerializedName("firstName") val firstNameAlt: String? = null,
    @SerializedName("first_name") val firstName: String? = null,

    // La API puede enviar "lastName" o "last_name"
    @SerializedName("lastName") val lastNameAlt: String? = null,
    @SerializedName("last_name") val lastName: String? = null,

    @SerializedName("email") val email: String,
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("permissions") val permissions: List<String>? = null
) {
    // Helper para obtener el nombre completo desde cualquier formato de la API
    fun getFullName(): String {
        // Prioridad 1: Si existe "name", usarlo directamente
        if (!name.isNullOrBlank()) {
            return name.trim()
        }

        // Prioridad 2: Intentar combinar first_name + last_name
        val first = firstName ?: firstNameAlt
        val last = lastName ?: lastNameAlt

        if (!first.isNullOrBlank() || !last.isNullOrBlank()) {
            return "${first ?: ""} ${last ?: ""}".trim()
        }

        // Si no hay datos del nombre, devolver email como identificador
        return email
    }

    // Helper para verificar si el usuario tiene un permiso específico
    fun hasPermission(permission: String): Boolean {
        return permissions?.contains(permission) ?: false
    }

    // Helper para verificar si es admin
    fun isAdmin(): Boolean = role?.lowercase() == "admin" || hasPermission("admin")
}

