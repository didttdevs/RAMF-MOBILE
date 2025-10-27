package com.cocido.ramfapp.network

import com.cocido.ramfapp.models.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio para gestionar reportes de estaciones
 */
interface ReportsService {
    
    /**
     * Obtener reportes por estación
     */
    @GET("reports/station/{stationName}")
    suspend fun getReportsByStation(
        @Header("Authorization") token: String,
        @Path("stationName") stationName: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PaginatedReports>
    
    /**
     * Obtener todos los reportes
     */
    @GET("reports")
    suspend fun getAllReports(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PaginatedReports>
    
    /**
     * Obtener un reporte por ID
     */
    @GET("reports/{id}")
    suspend fun getReportById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Report>
    
    /**
     * Crear un nuevo reporte
     */
    @POST("reports")
    suspend fun createReport(
        @Header("Authorization") token: String,
        @Body report: CreateReportRequest
    ): Response<Report>
    
    /**
     * Actualizar un reporte
     */
    @PATCH("reports/{id}")
    suspend fun updateReport(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body report: UpdateReportRequest
    ): Response<Report>
    
    /**
     * Eliminar un reporte
     */
    @DELETE("reports/{id}")
    suspend fun deleteReport(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Void>
}









