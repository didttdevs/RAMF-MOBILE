package com.cocido.ramfapp.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("email") val email: String,
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("isActive") val isActive: Boolean = true,
    @SerializedName("lastLogin") val lastLogin: String? = null,
    @SerializedName("roles") val roles: List<Role>? = null,
    @SerializedName("profile") val profile: Profile? = null
) {
    // Helper para obtener el nombre completo
    fun getFullName(): String {
        return "$name $lastName".trim()
    }

    // Helper para verificar si el usuario tiene un permiso específico
    fun hasPermission(permission: String): Boolean {
        return roles?.any { role -> 
            role.permissions?.any { perm -> perm.name == permission } ?: false 
        } ?: false
    }

    // Helper para verificar si es admin
    fun isAdmin(): Boolean = roles?.any { it.name.lowercase() == "admin" } ?: false
    
    // Helper para obtener campos del profile
    fun getPhone(): String? = profile?.phone
    fun getDni(): String? = profile?.dni
    fun getJobPosition(): String? = profile?.jobPosition
    fun getCompany(): String? = profile?.enterpriseName
    
    // Helper para verificar si el usuario tiene perfil
    fun hasProfile(): Boolean = profile != null
}

data class Role(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("isActive") val isActive: Boolean = true,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("permissions") val permissions: List<Permission>? = null
)

data class Permission(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("isActive") val isActive: Boolean = true,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

