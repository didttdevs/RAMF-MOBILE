package com.cocido.ramfapp.utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cocido.ramfapp.models.LoginResponse
import com.cocido.ramfapp.models.User
import com.cocido.ramfapp.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Helper para manejar autenticación y tokens de forma inteligente
 */
object AuthHelper {
    private const val TAG = "AuthHelper"
    
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState
    
    enum class AuthState {
        LOGGED_OUT,
        LOGGED_IN,
        TOKEN_EXPIRED,
        REFRESHING_TOKEN
    }
    
    /**
     * Verificar si el usuario está autenticado y el token es válido
     */
    fun isAuthenticated(): Boolean {
        val isLoggedIn = AuthManager.isUserLoggedIn()
        val state = if (isLoggedIn) AuthState.LOGGED_IN else AuthState.LOGGED_OUT
        _authState.postValue(state)
        
        if (isLoggedIn) {
            Log.d(TAG, "User is authenticated with valid accessToken")
        } else {
            Log.d(TAG, "User is not authenticated or token expired")
        }
        
        return isLoggedIn
    }
    
    /**
     * Intentar refrescar el token automáticamente
     */
    fun refreshTokenIfNeeded(): Boolean {
        if (AuthManager.isTokenExpiringSoon()) {
            _authState.postValue(AuthState.REFRESHING_TOKEN)
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val refreshToken = AuthManager.getRefreshToken()
                    if (refreshToken != null) {
                        val response = RetrofitClient.authService.refreshToken(mapOf("refreshToken" to refreshToken))
                        
                        // El backend retorna 202 Accepted en vez de 200 OK
                        if (response.code() == 202) {
                            val apiResponse = response.body()
                            if (apiResponse?.success == true && apiResponse.data != null) {
                                val loginResponse = apiResponse.data
                                val user = AuthManager.getCurrentUser()
                                
                                if (user != null) {
                                    AuthManager.saveUserSession(user, loginResponse)
                                    _authState.postValue(AuthState.LOGGED_IN)
                                    Log.d(TAG, "Token refreshed successfully")
                                }
                            }
                        } else {
                            Log.w(TAG, "Failed to refresh token: ${response.code()}")
                            _authState.postValue(AuthState.TOKEN_EXPIRED)
                        }
                    } else {
                        Log.w(TAG, "No refresh token available")
                        _authState.postValue(AuthState.TOKEN_EXPIRED)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error refreshing token", e)
                    _authState.postValue(AuthState.TOKEN_EXPIRED)
                }
            }
            
            return false // Asumir que necesita esperar
        }
        
        return true
    }
    
    /**
     * Obtener mensaje de error apropiado según el estado de autenticación
     */
    fun getAuthErrorMessage(): String {
        return when (_authState.value) {
            AuthState.LOGGED_OUT -> "Esta funcionalidad requiere autenticación. Por favor, inicia sesión."
            AuthState.TOKEN_EXPIRED -> "Tu sesión ha expirado. Por favor, inicia sesión nuevamente."
            AuthState.REFRESHING_TOKEN -> "Refrescando sesión, por favor espera..."
            else -> "Error de autenticación. Por favor, inicia sesión."
        }
    }
    
    /**
     * Obtener mensaje de éxito para funcionalidades autenticadas
     */
    fun getAuthSuccessMessage(): String {
        return "Datos cargados correctamente. Acceso completo habilitado."
    }
}
