package com.cocido.ramfapp.network

import com.cocido.ramfapp.models.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio para gestión de contactos y solicitudes
 */
interface ContactService {
    
    /**
     * Crear una nueva solicitud de contacto
     */
    @POST("contacts-requests")
    suspend fun createContactRequest(
        @Body request: CreateContactRequest
    ): Response<ApiResponse<ContactRequest>>
    
    /**
     * Obtener mis solicitudes de contacto
     */
    @GET("contacts-requests/my-requests")
    suspend fun getMyContactRequests(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PaginatedContactRequests>
    
    /**
     * Obtener todas las solicitudes (admin)
     */
    @GET("contacts-requests")
    suspend fun getAllContactRequests(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PaginatedContactRequests>
    
    /**
     * Obtener una solicitud por ID
     */
    @GET("contacts-requests/{id}")
    suspend fun getContactRequestById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ContactRequest>
    
    /**
     * Actualizar una solicitud de contacto
     */
    @PATCH("contacts-requests/{id}")
    suspend fun updateContactRequest(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: UpdateContactRequest
    ): Response<ContactRequest>
    
    /**
     * Eliminar una solicitud de contacto
     */
    @DELETE("contacts-requests/{id}")
    suspend fun deleteContactRequest(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Void>
    
    /**
     * Obtener estadísticas de solicitudes
     */
    @GET("contacts-requests/counts")
    suspend fun getContactRequestCounts(
        @Header("Authorization") token: String
    ): Response<ContactRequestCounts>
}






