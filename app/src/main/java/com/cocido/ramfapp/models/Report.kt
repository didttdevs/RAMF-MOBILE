package com.cocido.ramfapp.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo para reportes de estaciones meteorológicas
 */
data class Report(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("stationName")
    val stationName: String,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String,
    
    @SerializedName("author")
    val author: ReportAuthor?,
    
    @SerializedName("status")
    val status: String?,
    
    @SerializedName("priority")
    val priority: String?,
    
    @SerializedName("attachments")
    val attachments: List<ReportAttachment>? = emptyList()
)

/**
 * Autor del reporte
 */
data class ReportAuthor(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("firstName")
    val firstName: String?,
    
    @SerializedName("lastName")
    val lastName: String?,
    
    @SerializedName("email")
    val email: String
)

/**
 * Adjunto del reporte
 */
data class ReportAttachment(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("filename")
    val filename: String,
    
    @SerializedName("url")
    val url: String,
    
    @SerializedName("type")
    val type: String,
    
    @SerializedName("size")
    val size: Long
)

/**
 * DTO para crear un nuevo reporte
 */
data class CreateReportRequest(
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("stationName")
    val stationName: String,
    
    @SerializedName("priority")
    val priority: String? = "normal"
)

/**
 * DTO para actualizar un reporte
 */
data class UpdateReportRequest(
    @SerializedName("title")
    val title: String?,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("status")
    val status: String?
)

/**
 * Respuesta paginada de reportes
 */
data class PaginatedReports(
    @SerializedName("data")
    val data: List<Report>,
    
    @SerializedName("meta")
    val meta: PaginationMeta
)

/**
 * Metadatos de paginación
 */
data class PaginationMeta(
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("page")
    val page: Int,
    
    @SerializedName("limit")
    val limit: Int,
    
    @SerializedName("totalPages")
    val totalPages: Int
)









