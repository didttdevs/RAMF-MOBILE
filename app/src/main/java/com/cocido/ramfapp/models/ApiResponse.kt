package com.cocido.ramfapp.models

import com.google.gson.annotations.SerializedName

/**
 * Respuesta genérica de la API con manejo de errores mejorado
 */
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: T? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("error") val error: ApiError? = null,
    @SerializedName("timestamp") val timestamp: String? = null
)

/**
 * Modelo para errores de la API
 */
data class ApiError(
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("details") val details: Map<String, Any>? = null
)

/**
 * Respuesta de paginación para listas grandes
 */
data class PaginatedResponse<T>(
    @SerializedName("data") val data: List<T>,
    @SerializedName("pagination") val pagination: PaginationInfo
)

data class PaginationInfo(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("last_page") val lastPage: Int,
    @SerializedName("has_next") val hasNext: Boolean,
    @SerializedName("has_prev") val hasPrev: Boolean
)
