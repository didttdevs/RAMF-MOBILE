package com.cocido.ramfapp.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("email") val email: String,
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("role") val role: String,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("permissions") val permissions: List<String>? = null
) {
    // Helper para obtener el nombre completo
    fun getFullName(): String = "$firstName $lastName".trim()
    
    // Helper para verificar si el usuario tiene un permiso específico
    fun hasPermission(permission: String): Boolean {
        return permissions?.contains(permission) ?: false
    }
    
    // Helper para verificar si es admin
    fun isAdmin(): Boolean = role.lowercase() == "admin" || hasPermission("admin")
}

