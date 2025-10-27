package com.cocido.ramfapp.repository

import android.content.Context
import com.cocido.ramfapp.models.*
import com.cocido.ramfapp.network.NetworkModule
import com.cocido.ramfapp.network.ReportsService
import com.cocido.ramfapp.utils.TokenManager

/**
 * Repositorio para operaciones de reportes
 */
class ReportsRepository(private val context: Context) {
    
    private val reportsService: ReportsService = NetworkModule.createReportsService(context)
    private val tokenManager = TokenManager(context)
    
    /**
     * Obtener reportes por estación
     */
    suspend fun getReportsByStation(
        stationName: String,
        page: Int = 1,
        limit: Int = 20
    ): Result<PaginatedReports> {
        return try {
            val token = tokenManager.getAccessToken()
            if (token == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }
            
            val response = reportsService.getReportsByStation(token, stationName, page, limit)
            if (response.isSuccessful) {
                response.body()?.let { reports ->
                    Result.success(reports)
                } ?: Result.failure(Exception("No se encontraron reportes"))
            } else {
                Result.failure(Exception("Error al obtener reportes: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtener todos los reportes
     */
    suspend fun getAllReports(
        page: Int = 1,
        limit: Int = 20
    ): Result<PaginatedReports> {
        return try {
            val token = tokenManager.getAccessToken()
            if (token == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }
            
            val response = reportsService.getAllReports(token, page, limit)
            if (response.isSuccessful) {
                response.body()?.let { reports ->
                    Result.success(reports)
                } ?: Result.failure(Exception("No se encontraron reportes"))
            } else {
                Result.failure(Exception("Error al obtener reportes: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtener reporte por ID
     */
    suspend fun getReportById(reportId: String): Result<Report> {
        return try {
            val token = tokenManager.getAccessToken()
            if (token == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }
            
            val response = reportsService.getReportById(token, reportId)
            if (response.isSuccessful) {
                response.body()?.let { report ->
                    Result.success(report)
                } ?: Result.failure(Exception("No se encontró el reporte"))
            } else {
                Result.failure(Exception("Error al obtener reporte: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Crear nuevo reporte
     */
    suspend fun createReport(report: CreateReportRequest): Result<Report> {
        return try {
            val token = tokenManager.getAccessToken()
            if (token == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }
            
            val response = reportsService.createReport(token, report)
            if (response.isSuccessful) {
                response.body()?.let { newReport ->
                    Result.success(newReport)
                } ?: Result.failure(Exception("No se pudo crear el reporte"))
            } else {
                Result.failure(Exception("Error al crear reporte: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Actualizar reporte
     */
    suspend fun updateReport(reportId: String, report: UpdateReportRequest): Result<Report> {
        return try {
            val token = tokenManager.getAccessToken()
            if (token == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }
            
            val response = reportsService.updateReport(token, reportId, report)
            if (response.isSuccessful) {
                response.body()?.let { updatedReport ->
                    Result.success(updatedReport)
                } ?: Result.failure(Exception("No se pudo actualizar el reporte"))
            } else {
                Result.failure(Exception("Error al actualizar reporte: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Eliminar reporte
     */
    suspend fun deleteReport(reportId: String): Result<Unit> {
        return try {
            val token = tokenManager.getAccessToken()
            if (token == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }
            
            val response = reportsService.deleteReport(token, reportId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al eliminar reporte: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}






