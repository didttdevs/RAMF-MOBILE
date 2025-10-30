package com.cocido.ramfapp.repository

import android.content.Context
import com.cocido.ramfapp.models.*
import com.cocido.ramfapp.network.AuthService
import com.cocido.ramfapp.network.NetworkModule
import com.cocido.ramfapp.utils.TokenManager

/**
 * Repositorio para operaciones de autenticación
 */
class AuthRepository(private val context: Context) {
    
    private val authService: AuthService = NetworkModule.createAuthService(context)
    private val tokenManager = TokenManager(context)
    
    /**
     * Iniciar sesión
     */
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val request = LoginRequest(email, password)
            val response = authService.login(request)
            
            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    // Guardar tokens
                    tokenManager.saveTokens(loginResponse)
                    Result.success(loginResponse)
                } ?: Result.failure(Exception("Respuesta vacía del servidor"))
            } else {
                Result.failure(Exception("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Registrar usuario
     */
    suspend fun register(
        name: String,
        lastName: String,
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            val request = RegisterRequest(name, lastName, email, password)
            val response = authService.register(request)
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al registrar usuario: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cerrar sesión
     */
    suspend fun logout(): Result<Unit> {
        return try {
            val token = tokenManager.getAccessToken()
            if (token != null) {
                val response = authService.logout(token)
                if (response.isSuccessful) {
                    tokenManager.clearTokens()
                    Result.success(Unit)
                } else {
                    // Limpiar tokens localmente aunque falle el servidor
                    tokenManager.clearTokens()
                    Result.success(Unit)
                }
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            // Limpiar tokens localmente aunque falle
            tokenManager.clearTokens()
            Result.success(Unit)
        }
    }
    
    /**
     * Obtener usuario actual
     */
    suspend fun getCurrentUser(): Result<User> {
        return try {
            // El token se agrega automáticamente por el interceptor de RetrofitClient
            val response = authService.getCurrentUser()
            if (response.isSuccessful) {
                response.body()?.let { user ->
                    Result.success(user)
                } ?: Result.failure(Exception("Datos de usuario vacíos"))
            } else {
                Result.failure(Exception("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Solicitar reset de contraseña
     */
    suspend fun requestPasswordReset(email: String): Result<Unit> {
        return try {
            val request = ForgotPasswordRequest(email)
            val response = authService.requestPasswordReset(request)
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al solicitar reset de contraseña: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cambiar contraseña
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val token = tokenManager.getAccessToken()
            if (token == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }
            
            val request = ChangePasswordRequest(currentPassword, newPassword)
            // El token se agrega automáticamente por el interceptor de RetrofitClient
            val response = authService.changePassword(request)
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al cambiar contraseña: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Verificar si el usuario está autenticado
     */
    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }
    
    /**
     * Obtener ID del usuario actual
     */
    fun getCurrentUserId(): String? {
        return tokenManager.getCurrentUserId()
    }
}
