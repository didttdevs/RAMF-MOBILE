package com.cocido.ramfapp.network

import com.cocido.ramfapp.models.User
import com.cocido.ramfapp.models.ChangePasswordRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio para operaciones de usuario
 * Basado en los endpoints del backend
 */
interface UserService {
    
    /**
     * Obtener usuario actual
     * GET /users/me
     */
    @GET("users/me")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): Response<User>
    
    /**
     * Cambiar contraseña del usuario
     * PATCH /users/change-password
     */
    @PATCH("users/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body changePasswordRequest: ChangePasswordRequest
    ): Response<Unit>
    
    /**
     * Cambiar avatar del usuario
     * PATCH /users/avatar
     */
    @Multipart
    @PATCH("users/avatar")
    suspend fun changeAvatar(
        @Header("Authorization") token: String,
        @Part avatar: MultipartBody.Part
    ): Response<Map<String, String>>
    
    /**
     * Eliminar cuenta del usuario
     * DELETE /users/{id}
     */
    @DELETE("users/{id}")
    suspend fun deleteAccount(
        @Header("Authorization") token: String,
        @Path("id") userId: String
    ): Response<Unit>
    
    /**
     * Actualizar información del usuario
     * PATCH /users/{id}
     */
    @PATCH("users/{id}")
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Path("id") userId: String,
        @Body updateData: Map<String, String>
    ): Response<User>
}


