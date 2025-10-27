package com.cocido.ramfapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.cocido.ramfapp.models.LoginResponse

/**
 * Gestor de tokens JWT para autenticación
 */
class TokenManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("ramf_tokens", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRES_AT = "token_expires_at"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
    }
    
    /**
     * Guardar tokens después del login
     */
    fun saveTokens(loginResponse: LoginResponse) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, loginResponse.accessToken)
            putString(KEY_REFRESH_TOKEN, loginResponse.refreshToken)
            putLong(KEY_TOKEN_EXPIRES_AT, System.currentTimeMillis() + (24 * 60 * 60 * 1000)) // 24 horas
            putString(KEY_USER_ID, loginResponse.user.id)
            putString(KEY_USER_EMAIL, loginResponse.user.email)
            apply()
        }
    }
    
    /**
     * Obtener token de acceso
     */
    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }
    
    /**
     * Obtener token de refresh
     */
    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }
    
    /**
     * Verificar si el token está expirado
     */
    fun isTokenExpired(): Boolean {
        val expiresAt = prefs.getLong(KEY_TOKEN_EXPIRES_AT, 0)
        return System.currentTimeMillis() >= expiresAt
    }
    
    /**
     * Verificar si hay un usuario autenticado
     */
    fun isLoggedIn(): Boolean {
        val accessToken = getAccessToken()
        val refreshToken = getRefreshToken()
        return accessToken != null && refreshToken != null && !isTokenExpired()
    }
    
    /**
     * Verificar si el token necesita ser refrescado (5 minutos antes de expirar)
     */
    fun shouldRefreshToken(): Boolean {
        val expiresAt = prefs.getLong(KEY_TOKEN_EXPIRES_AT, 0)
        val fiveMinutesFromNow = System.currentTimeMillis() + (5 * 60 * 1000)
        return fiveMinutesFromNow >= expiresAt
    }
    
    /**
     * Obtener tiempo restante del token en segundos
     */
    fun getTokenTimeRemaining(): Long {
        val expiresAt = prefs.getLong(KEY_TOKEN_EXPIRES_AT, 0)
        val remaining = expiresAt - System.currentTimeMillis()
        return if (remaining > 0) remaining / 1000 else 0
    }
    
    /**
     * Obtener ID del usuario actual
     */
    fun getCurrentUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }
    
    /**
     * Obtener email del usuario actual
     */
    fun getCurrentUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }
    
    /**
     * Limpiar todos los tokens (logout)
     */
    fun clearTokens() {
        prefs.edit().clear().apply()
    }
    
    /**
     * Actualizar solo el access token
     */
    fun updateAccessToken(newAccessToken: String, newExpiresAt: Long) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, newAccessToken)
            putLong(KEY_TOKEN_EXPIRES_AT, newExpiresAt)
            apply()
        }
    }
}
