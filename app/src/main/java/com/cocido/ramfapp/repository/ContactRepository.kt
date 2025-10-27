package com.cocido.ramfapp.repository

import android.content.Context
import com.cocido.ramfapp.models.*
import com.cocido.ramfapp.network.ContactService
import com.cocido.ramfapp.network.NetworkModule
import com.cocido.ramfapp.utils.TokenManager

/**
 * Repositorio para operaciones de contacto
 */
class ContactRepository(private val context: Context) {
    
    private val contactService: ContactService = NetworkModule.createContactService(context)
    private val tokenManager = TokenManager(context)
    
    /**
     * Crear solicitud de contacto
     */
    suspend fun createContactRequest(request: CreateContactRequest): Result<ContactRequest> {
        return try {
            val response = contactService.createContactRequest(request)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success) {
                        apiResponse.data?.let { contactRequest ->
                            Result.success(contactRequest)
                        } ?: Result.failure(Exception("Datos de contacto vacíos"))
                    } else {
                        Result.failure(Exception(apiResponse.message ?: "Error al crear solicitud"))
                    }
                } ?: Result.failure(Exception("Respuesta vacía del servidor"))
            } else {
                Result.failure(Exception("Error al crear solicitud: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtener mis solicitudes de contacto
     */
    suspend fun getMyContactRequests(
        page: Int = 1,
        limit: Int = 20
    ): Result<PaginatedContactRequests> {
        return try {
            val token = tokenManager.getAccessToken()
            if (token == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }
            
            val response = contactService.getMyContactRequests(token, page, limit)
            if (response.isSuccessful) {
                response.body()?.let { requests ->
                    Result.success(requests)
                } ?: Result.failure(Exception("No se encontraron solicitudes"))
            } else {
                Result.failure(Exception("Error al obtener solicitudes: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtener todas las solicitudes (admin)
     */
    suspend fun getAllContactRequests(
        page: Int = 1,
        limit: Int = 20
    ): Result<PaginatedContactRequests> {
        return try {
            val token = tokenManager.getAccessToken()
            if (token == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }
            
            val response = contactService.getAllContactRequests(token, page, limit)
            if (response.isSuccessful) {
                response.body()?.let { requests ->
                    Result.success(requests)
                } ?: Result.failure(Exception("No se encontraron solicitudes"))
            } else {
                Result.failure(Exception("Error al obtener solicitudes: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtener solicitud por ID
     */
    suspend fun getContactRequestById(requestId: String): Result<ContactRequest> {
        return try {
            val token = tokenManager.getAccessToken()
            if (token == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }
            
            val response = contactService.getContactRequestById(token, requestId)
            if (response.isSuccessful) {
                response.body()?.let { request ->
                    Result.success(request)
                } ?: Result.failure(Exception("No se encontró la solicitud"))
            } else {
                Result.failure(Exception("Error al obtener solicitud: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Actualizar solicitud de contacto
     */
    suspend fun updateContactRequest(
        requestId: String,
        request: UpdateContactRequest
    ): Result<ContactRequest> {
        return try {
            val token = tokenManager.getAccessToken()
            if (token == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }
            
            val response = contactService.updateContactRequest(token, requestId, request)
            if (response.isSuccessful) {
                response.body()?.let { updatedRequest ->
                    Result.success(updatedRequest)
                } ?: Result.failure(Exception("No se pudo actualizar la solicitud"))
            } else {
                Result.failure(Exception("Error al actualizar solicitud: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Eliminar solicitud de contacto
     */
    suspend fun deleteContactRequest(requestId: String): Result<Unit> {
        return try {
            val token = tokenManager.getAccessToken()
            if (token == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }
            
            val response = contactService.deleteContactRequest(token, requestId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al eliminar solicitud: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtener estadísticas de solicitudes
     */
    suspend fun getContactRequestCounts(): Result<ContactRequestCounts> {
        return try {
            val token = tokenManager.getAccessToken()
            if (token == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }
            
            val response = contactService.getContactRequestCounts(token)
            if (response.isSuccessful) {
                response.body()?.let { counts ->
                    Result.success(counts)
                } ?: Result.failure(Exception("No se encontraron estadísticas"))
            } else {
                Result.failure(Exception("Error al obtener estadísticas: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}






