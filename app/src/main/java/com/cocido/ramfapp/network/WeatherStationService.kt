package com.cocido.ramfapp.network

import com.cocido.ramfapp.models.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio para estaciones meteorológicas con mejores prácticas de desarrollo
 */
interface WeatherStationService {

    // ============ ENDPOINTS PÚBLICOS (no requieren autenticación) ============

    /**
     * Obtener lista de todas las estaciones meteorológicas
     */
    @GET("stations")
    suspend fun getWeatherStations(): Response<StationsResponse>

    /**
     * Obtener estaciones con datos geográficos para mapas
     */
    @GET("stations/geo")
    suspend fun getGeoStations(): Response<ApiResponse<List<WeatherStation>>>

    /**
     * Obtener estación específica por nombre
     */
    @GET("stations/{stationName}")
    suspend fun getWeatherStation(
        @Path("stationName") stationName: String
    ): Response<ApiResponse<WeatherStation>>

    /**
     * Obtener sensores de una estación específica
     */
    @GET("stations/{stationName}/sensors")
    suspend fun getStationSensors(
        @Path("stationName") stationName: String
    ): Response<ApiResponse<List<Sensor>>>

    /**
     * Obtener lista de todos los sensores disponibles
     */
    @GET("sensors")
    suspend fun getAllSensors(): Response<ApiResponse<List<Sensor>>>

    /**
     * Widget con datos meteorológicos actuales (público)
     * NOTA: Este endpoint devuelve directamente WidgetData, no ApiResponse<WidgetData>
     */
    @GET("stations-measurement/widget/{stationName}")
    suspend fun getWidgetData(
        @Path("stationName") stationName: String
    ): Response<WidgetData>

        /**
         * Datos públicos de una estación (sin autenticación) - ENDPOINT NO DISPONIBLE
         * Comentado hasta confirmar que existe en el servidor
         */
        // @GET("stations-measurement/public/data/{stationName}")
        // suspend fun getPublicStationData(
        //     @Path("stationName") stationName: String
        // ): Response<ApiResponse<List<WeatherData>>>

        /**
         * Datos para gráficos públicos (sin autenticación) - ENDPOINT NO DISPONIBLE
         * Comentado hasta confirmar que existe en el servidor
         */
        // @GET("stations-measurement/public-charts/{stationName}")
        // suspend fun getPublicChartsData(
        //     @Path("stationName") stationName: String
        // ): Response<ApiResponse<List<WeatherData>>>

    /**
     * Imágenes de pronósticos para una estación
     */
    @GET("forecast-images/by/{stationName}")
    suspend fun getForecastImages(
        @Path("stationName") stationName: String
    ): Response<ApiResponse<List<ForecastImage>>>

    // ============ ENDPOINTS PROTEGIDOS (requieren autenticación) ============

    /**
     * Obtener datos de temperatura máxima y mínima (desde widget)
     */
    @GET("stations-measurement/widget/{stationName}")
    suspend fun getTemperatureMaxMin(
        @Path("stationName") stationName: String
    ): Response<WidgetData>

    /**
     * Datos en rango de tiempo específico (requiere auth)
     */
    @GET("stations-measurement/data-time-range/{stationName}")
    suspend fun getWeatherDataTimeRange(
        @Path("stationName") stationName: String,
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("timeRange") timeRange: String = "custom"
    ): Response<WeatherDataResponse>

    /**
     * Datos para gráficos en rango de tiempo (requiere auth)
     */
    @GET("stations-measurement/data-time-range-charts/{stationName}")
    suspend fun getWeatherDataForCharts(
        @Path("stationName") stationName: String,
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("timeRange") timeRange: String = "custom"
    ): Response<WeatherDataResponse>

    /**
     * Datos entre fechas específicas (requiere auth)
     */
    @GET("stations-measurement/data-from-to/{stationName}")
    suspend fun getWeatherDataFromTo(
        @Path("stationName") stationName: String,
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("timeRange") timeRange: String = "custom"
    ): Response<ApiResponse<List<WeatherData>>>

    /**
     * Todos los datos de una estación (requiere auth)
     */
    @GET("stations-measurement/allByStationName/{stationName}")
    suspend fun getAllStationMeasurements(
        @Path("stationName") stationName: String
    ): Response<ApiResponse<List<WeatherData>>>

    /**
     * Obtener datos de sensores específicos
     */
    @GET("stations/{stationId}/sensor-data")
    suspend fun getSensorData(
        @Path("stationId") stationId: String,
        @Query("sensor_ids") sensorIds: List<String>,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<ApiResponse<List<SensorData>>>

    /**
     * Obtener estadísticas de una estación
     */
    @GET("stations/{stationId}/statistics")
    suspend fun getStationStatistics(
        @Path("stationId") stationId: String,
        @Query("period") period: String = "24h" // 24h, 7d, 30d, 1y
    ): Response<ApiResponse<Map<String, Any>>>

    /**
     * Buscar estaciones por ubicación
     */
    @GET("stations/search")
    suspend fun searchStationsByLocation(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("radius") radiusKm: Double = 50.0
    ): Response<ApiResponse<List<WeatherStation>>>
}
