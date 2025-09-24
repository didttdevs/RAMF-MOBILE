package com.cocido.ramfapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.cocido.ramfapp.models.LoginResponse
import com.cocido.ramfapp.models.User
import com.cocido.ramfapp.network.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Gestor de autenticación con mejores prácticas de seguridad
 */
object AuthManager {
    private const val TAG = "AuthManager"
    private const val PREFS_NAME = "auth_prefs_encrypted"
    private const val ACCESS_TOKEN_KEY = "access_token"
    private const val REFRESH_TOKEN_KEY = "refresh_token"
    private const val USER_KEY = "user_data"
    private const val LOGIN_TIMESTAMP_KEY = "login_timestamp"
    private const val TOKEN_EXPIRY_KEY = "token_expiry"
    private const val TOKEN_EXPIRY_BUFFER_MINUTES = 5L // Refrescar 5 minutos antes
    
    private var encryptedSharedPref: SharedPreferences? = null
    private var masterKeyAlias: String? = null
    
    /**
     * Inicializar el AuthManager con SharedPreferences encriptadas
     */
    fun initialize(context: Context) {
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            
            encryptedSharedPref = EncryptedSharedPreferences.create(
                PREFS_NAME,
                masterKeyAlias!!,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            
            Log.d(TAG, "AuthManager initialized with encrypted storage")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize encrypted storage, falling back to regular SharedPreferences", e)
            // Fallback a SharedPreferences normal si falla la encriptación
            encryptedSharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
        
        // Restaurar tokens en RetrofitClient
        restoreTokensToRetrofit()
    }
    
    /**
     * Guardar sesión del usuario con tokens
     */
    fun saveUserSession(user: User, loginResponse: LoginResponse) {
        try {
            val userJson = Gson().toJson(user)
            
            encryptedSharedPref?.edit()?.apply {
                putString(ACCESS_TOKEN_KEY, loginResponse.accessToken)
                putString(REFRESH_TOKEN_KEY, loginResponse.refreshToken)
                putString(USER_KEY, userJson)
                putLong(LOGIN_TIMESTAMP_KEY, System.currentTimeMillis())
                putLong(TOKEN_EXPIRY_KEY, System.currentTimeMillis() + loginResponse.getExpiresIn())
                apply()
            }
            
            // Configurar tokens en RetrofitClient
            RetrofitClient.setAuthTokens(
                loginResponse.accessToken,
                loginResponse.refreshToken,
                loginResponse.getExpiresIn()
            )
            
            Log.d(TAG, "User session saved for: ${user.email}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user session", e)
        }
    }
    
    /**
     * Obtener token de acceso
     */
    fun getAccessToken(): String? {
        return encryptedSharedPref?.getString(ACCESS_TOKEN_KEY, null)
    }
    
    /**
     * Obtener token de refresh
     */
    fun getRefreshToken(): String? {
        return encryptedSharedPref?.getString(REFRESH_TOKEN_KEY, null)
    }
    
    /**
     * Obtener usuario actual
     */
    fun getCurrentUser(): User? {
        val userJson = encryptedSharedPref?.getString(USER_KEY, null)
        return if (userJson != null) {
            try {
                Gson().fromJson(userJson, User::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing user data", e)
                null
            }
        } else null
    }
    
        /**
         * Verificar si el usuario está logueado y el token es válido
         */
        fun isUserLoggedIn(): Boolean {
            val user = getCurrentUser()
            val accessToken = getAccessToken()

            Log.d(TAG, "isUserLoggedIn check:")
            Log.d(TAG, "  User: ${user?.email ?: "null"}")
            Log.d(TAG, "  AccessToken: ${if (accessToken.isNullOrEmpty()) "null/empty" else "present (${accessToken.take(20)}...)"}")

            // Si no hay usuario o token, no está logueado
            if (user == null || accessToken.isNullOrEmpty()) {
                Log.d(TAG, "User not logged in: user=${user != null}, token=${!accessToken.isNullOrEmpty()}")
                return false
            }

            // Verificar si el token ha expirado (con buffer de seguridad)
            val tokenExpiry = encryptedSharedPref?.getLong(TOKEN_EXPIRY_KEY, 0L) ?: 0L
            val currentTime = System.currentTimeMillis()
            val bufferTime = TOKEN_EXPIRY_BUFFER_MINUTES * 60 * 1000 // 5 minutos en milisegundos

            val isValid = currentTime < (tokenExpiry - bufferTime)
            Log.d(TAG, "Token expiry check: currentTime=$currentTime, tokenExpiry=$tokenExpiry, bufferTime=$bufferTime, isValid=$isValid")

            return isValid
        }
    
    /**
     * Verificar si el token necesita ser refrescado
     */
    fun isTokenExpiringSoon(): Boolean {
        val tokenExpiry = encryptedSharedPref?.getLong(TOKEN_EXPIRY_KEY, 0L) ?: 0L
        val currentTime = System.currentTimeMillis()
        val bufferTime = TOKEN_EXPIRY_BUFFER_MINUTES * 60 * 1000
        
        return currentTime >= (tokenExpiry - bufferTime)
    }
    
    /**
     * Refrescar token si es necesario
     */
    fun refreshTokenIfNeeded(): Boolean {
        if (!isTokenExpiringSoon()) {
            return true
        }
        
        val refreshToken = getRefreshToken()
        if (refreshToken.isNullOrEmpty()) {
            Log.w(TAG, "No refresh token available")
            return false
        }
        
        // Intentar refrescar el token en un hilo separado
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.authService.refreshToken("Bearer $refreshToken")
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        val loginResponse = apiResponse.data
                        val user = getCurrentUser()
                        
                        if (user != null) {
                            saveUserSession(user, loginResponse)
                            Log.d(TAG, "Token refreshed successfully")
                        }
                    }
                } else {
                    Log.w(TAG, "Failed to refresh token: ${response.code()}")
                    logout()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing token", e)
                logout()
            }
        }
        
        return false // Asumir que necesita esperar
    }
    
    /**
     * Verificar si se permite acceso como invitado
     */
    fun allowGuestAccess(): Boolean {
        // Permitir acceso como invitado para funcionalidades básicas
        return true
    }
    
    /**
     * Cerrar sesión y limpiar datos
     */
    fun logout() {
        try {
            // Intentar cerrar sesión en el servidor
            val accessToken = getAccessToken()
            if (!accessToken.isNullOrEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        RetrofitClient.authService.logout("Bearer $accessToken")
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to logout on server", e)
                    }
                }
            }
            
            // Limpiar datos locales
            encryptedSharedPref?.edit()?.apply {
                remove(ACCESS_TOKEN_KEY)
                remove(REFRESH_TOKEN_KEY)
                remove(USER_KEY)
                remove(LOGIN_TIMESTAMP_KEY)
                remove(TOKEN_EXPIRY_KEY)
                apply()
            }
            
            // Limpiar tokens de RetrofitClient
            RetrofitClient.clearAuthTokens()
            
            Log.d(TAG, "User logged out successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout", e)
        }
    }
    
    /**
     * Restaurar tokens en RetrofitClient al inicializar
     */
    private fun restoreTokensToRetrofit() {
        val accessToken = getAccessToken()
        val refreshToken = getRefreshToken()
        val tokenExpiry = encryptedSharedPref?.getLong(TOKEN_EXPIRY_KEY, 0L) ?: 0L
        
        if (!accessToken.isNullOrEmpty()) {
            val expiresIn = if (tokenExpiry > 0) {
                tokenExpiry - System.currentTimeMillis()
            } else {
                0L
            }
            
            RetrofitClient.setAuthTokens(accessToken, refreshToken, expiresIn)
            Log.d(TAG, "Tokens restored to RetrofitClient")
        }
    }
    
    /**
     * Verificar si el usuario tiene un permiso específico
     */
    fun hasPermission(permission: String): Boolean {
        val user = getCurrentUser()
        return user?.hasPermission(permission) ?: false
    }
    
    /**
     * Verificar si el usuario es administrador
     */
    fun isAdmin(): Boolean {
        val user = getCurrentUser()
        return user?.isAdmin() ?: false
    }
}