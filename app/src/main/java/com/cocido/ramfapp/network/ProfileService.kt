package com.cocido.ramfapp.network

import com.cocido.ramfapp.models.Profile
import com.cocido.ramfapp.models.User
import com.cocido.ramfapp.models.CreateProfileRequest
import com.cocido.ramfapp.models.UpdateProfileRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio para operaciones de perfil
 * Basado en los endpoints del backend
 */
interface ProfileService {
    
    /**
     * Crear perfil del usuario
     * POST /profiles
     */
    @POST("profiles")
    suspend fun createProfile(
        @Header("Authorization") token: String,
        @Body createProfileRequest: CreateProfileRequest
    ): Response<Profile>
    
    /**
     * Verificar si el usuario tiene perfil
     * GET /profiles/check
     */
    @GET("profiles/check")
    suspend fun checkProfile(
        @Header("Authorization") token: String
    ): Response<Map<String, Boolean>>
    
    /**
     * Obtener usuario actual
     * GET /auth/me (del usuario autenticado)
     * Devuelve directamente User con profile incluido
     */
    @GET("auth/me")
    suspend fun getCurrentUser(): Response<User>
    
    /**
     * Endpoint de prueba para verificar el token
     * GET /auth/test-token
     */
    @GET("auth/test-token")
    suspend fun testToken(): Response<String>
    
    /**
     * Actualizar perfil del usuario
     * PATCH /profiles
     */
    @PATCH("profiles")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body updateProfileRequest: UpdateProfileRequest
    ): Response<Unit>
    
    /**
     * Eliminar perfil del usuario
     * DELETE /profiles
     */
    @DELETE("profiles")
    suspend fun deleteProfile(
        @Header("Authorization") token: String
    ): Response<Unit>
    
    /**
     * Actualizar avatar del usuario
     * PATCH /users/avatar
     */
    @Multipart
    @PATCH("users/avatar")
    suspend fun updateAvatar(
        @Header("Authorization") token: String,
        @Part avatar: MultipartBody.Part
    ): Response<String>
    
    /**
     * Eliminar avatar del usuario
     * DELETE /users/avatar
     */
    @DELETE("users/avatar")
    suspend fun removeAvatar(
        @Header("Authorization") token: String
    ): Response<Unit>
}