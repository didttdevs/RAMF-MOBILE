package com.cocido.ramfapp.models

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String? = null,
    val user: User
) {
    // Helper para obtener el token completo con el tipo
    fun getFullToken(): String = "Bearer $accessToken"
    
    // Helper para verificar si el token está próximo a expirar (asumimos 20 minutos por defecto)
    fun isTokenExpiringSoon(): Boolean {
        // Como no tenemos expiresIn del backend, asumimos que expira en 20 minutos
        return false // Por ahora siempre retornamos false, se puede mejorar con JWT parsing
    }
    
    // Helper para obtener el tiempo de expiración (24 horas por defecto)
    fun getExpiresIn(): Long = 86400000 // 24 horas en milisegundos
}
