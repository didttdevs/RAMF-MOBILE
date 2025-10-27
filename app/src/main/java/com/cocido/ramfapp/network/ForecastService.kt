package com.cocido.ramfapp.network

import com.cocido.ramfapp.models.ApiResponse
import com.cocido.ramfapp.models.ForecastImage
import retrofit2.Response
import retrofit2.http.*

interface ForecastService {

    @GET("forecast-images/by/{stationName}")
    suspend fun getForecastImagesByStation(
        @Header("Authorization") token: String,
        @Path("stationName") stationName: String,
        @Query("type") type: String? = null, // Filtro por tipo de pronóstico
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse<List<ForecastImage>>>

    @GET("forecast-images/latest")
    suspend fun getLatestForecastImages(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<List<ForecastImage>>>

    @GET("forecast-images/{id}")
    suspend fun getForecastImageById(
        @Header("Authorization") token: String,
        @Path("id") imageId: String
    ): Response<ApiResponse<ForecastImage>>
}









