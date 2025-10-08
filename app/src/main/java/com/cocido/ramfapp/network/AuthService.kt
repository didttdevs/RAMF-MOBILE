package com.cocido.ramfapp.network

import com.cocido.ramfapp.models.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio de autenticación con mejores prácticas de seguridad
 */
interface AuthService {
    
    /**
     * Iniciar sesión con email y contraseña
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>
    
    /**
     * Registrar nuevo usuario
     */
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<LoginResponse>>
    
    /**
     * Cerrar sesión
     */
    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<ApiResponse<Void>>
    
    /**
     * Refrescar token de acceso
     */
    @POST("auth/refresh")
    suspend fun refreshToken(@Header("Authorization") token: String): Response<ApiResponse<LoginResponse>>
    
    /**
     * Iniciar sesión con Google
     */
    @POST("auth/login/google")
    suspend fun googleLogin(@Body googleToken: Map<String, String>): Response<LoginResponse>
    
    /**
     * Verificar token válido
     */
    @GET("auth/verify")
    suspend fun verifyToken(@Header("Authorization") token: String): Response<ApiResponse<User>>
    
    /**
     * Solicitar reset de contraseña
     */
    @POST("auth/forgot-password")
    suspend fun requestPasswordReset(@Body request: ForgotPasswordRequest): Response<ApiResponse<Void>>
    
    /**
     * Cambiar contraseña
     */
    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<Void>>
    
    /**
     * Resetear contraseña con token
     */
    @POST("auth/reset-password")
    suspend fun resetPassword(@Body resetData: Map<String, String>): Response<ApiResponse<Void>>
    
    /**
     * Cambiar contraseña (requiere autenticación)
     */
    @PUT("auth/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body passwordData: Map<String, String>
    ): Response<ApiResponse<Void>>
    
    /**
     * Obtener información del usuario autenticado actual
     */
    @GET("auth/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): Response<ApiResponse<User>>

    /**
     * Obtener perfil del usuario actual (alternativo)
     */
    @GET("auth/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<ApiResponse<User>>

    /**
     * Actualizar perfil del usuario
     */
    @PUT("auth/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body userData: Map<String, Any>
    ): Response<ApiResponse<User>>
}
