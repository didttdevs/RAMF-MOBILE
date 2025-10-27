package com.cocido.ramfapp.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo para solicitudes de contacto
 */
data class ContactRequest(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("lastName")
    val lastName: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("subject")
    val subject: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("status")
    val status: ContactRequestStatus,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String,
    
    @SerializedName("user")
    val user: User? = null
)

/**
 * Estados de solicitud de contacto
 */
enum class ContactRequestStatus {
    @SerializedName("pending")
    PENDING,
    
    @SerializedName("in_progress")
    IN_PROGRESS,
    
    @SerializedName("resolved")
    RESOLVED,
    
    @SerializedName("rejected")
    REJECTED
}

/**
 * Solicitud para crear contacto
 */
data class CreateContactRequest(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("lastName")
    val lastName: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("subject")
    val subject: String,
    
    @SerializedName("message")
    val message: String
)

/**
 * Solicitud para actualizar contacto
 */
data class UpdateContactRequest(
    @SerializedName("status")
    val status: ContactRequestStatus,
    
    @SerializedName("response")
    val response: String? = null
)

/**
 * Respuesta paginada de solicitudes de contacto
 */
data class PaginatedContactRequests(
    @SerializedName("data")
    val data: List<ContactRequest>,
    
    @SerializedName("meta")
    val meta: PaginationMeta,
    
    @SerializedName("links")
    val links: PaginationLinks
)

/**
 * Estadísticas de solicitudes de contacto
 */
data class ContactRequestCounts(
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("pending")
    val pending: Int,
    
    @SerializedName("inProgress")
    val inProgress: Int,
    
    @SerializedName("resolved")
    val resolved: Int,
    
    @SerializedName("rejected")
    val rejected: Int
)

