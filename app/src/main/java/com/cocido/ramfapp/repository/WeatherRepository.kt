package com.cocido.ramfapp.repository

import android.util.Log
import com.cocido.ramfapp.common.Constants
import com.cocido.ramfapp.common.Resource
import com.cocido.ramfapp.models.*
import com.cocido.ramfapp.network.RetrofitClient
import com.cocido.ramfapp.utils.AuthHelper
import com.cocido.ramfapp.utils.AuthManager
import com.cocido.ramfapp.utils.SecurityLogger
import com.cocido.ramfapp.utils.ApiErrorHandler
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.minutes

/**
 * Professional Weather Repository implementing Clean Architecture principles
 * with intelligent caching, request deduplication, and robust error handling
 */
class WeatherRepository {

    private val TAG = "WeatherRepository"
    private val weatherStationService = RetrofitClient.weatherStationService
    private val securityLogger = SecurityLogger()

    // Thread-safe cache with TTL
    private val cacheStore = ConcurrentHashMap<String, CacheEntry<*>>()
    private val ongoingRequests = ConcurrentHashMap<String, Deferred<*>>()
    private val cacheMutex = Mutex()

    data class CacheEntry<T>(
        val data: T,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun isExpired(ttlMillis: Long = Constants.Network.CACHE_MAX_AGE * 1000L): Boolean {
            return System.currentTimeMillis() - timestamp > ttlMillis
        }
    }
    
    /**
     * Get weather stations with intelligent caching and request deduplication
     */
    fun getWeatherStations(): Flow<Resource<List<WeatherStation>>> = flow {
        val cacheKey = "weather_stations"

        // Check cache first
        getCachedData<List<WeatherStation>>(cacheKey)?.let { cached ->
            Log.d(TAG, "Returning cached weather stations")
            emit(Resource.Success(cached))
            return@flow
        }

        emit(Resource.Loading)

        try {
            val result = executeWithDeduplication(cacheKey) {
                weatherStationService.getWeatherStations()
            }

            result.fold(
                onSuccess = { response ->
                    if (response.isSuccessful) {
                        response.body()?.let { stationsResponse ->
                            val stations = stationsResponse.data
                            cacheData(cacheKey, stations)
                            securityLogger.logDataAccess("weather_stations", stations.size)
                            emit(Resource.Success(stations))
                        } ?: emit(Resource.Error(Exception("Empty response"), "Respuesta vacía del servidor"))
                    } else {
                        val error = ApiErrorHandler.getErrorMessage(response.code(), "stations")
                        securityLogger.logNetworkSecurityEvent("stations", response.code())
                        emit(Resource.Error(Exception("HTTP ${response.code()}"), error))
                    }
                },
                onFailure = { exception ->
                    Log.e(TAG, "Error getting weather stations", exception)
                    securityLogger.logNetworkSecurityEvent("stations", 0)
                    emit(Resource.Error(exception, "Error de conexión: ${exception.message}"))
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error getting weather stations", e)
            emit(Resource.Error(e, "Error inesperado: ${e.message}"))
        }
    }
    
    /**
     * Get charts data with authentication and validation
     * Uses the correct backend endpoint for pre-processed chart data
     */
    fun getChartsData(
        stationName: String,
        from: String,
        to: String
    ): Flow<Resource<ChartsPayload>> = flow {
        // Security validation
        if (!isValidStationName(stationName)) {
            emit(Resource.Error(Exception("Invalid station"), "Nombre de estación inválido"))
            return@flow
        }

        if (!isValidDateRange(from, to)) {
            emit(Resource.Error(Exception("Invalid date range"), "Rango de fechas inválido"))
            return@flow
        }

        if (!AuthHelper.isAuthenticated()) {
            securityLogger.logAuthenticationEvent("unauthorized_access", false)
            emit(Resource.Error(Exception("Unauthorized"), "Autenticación requerida"))
            return@flow
        }

        val cacheKey = "charts_${stationName}_${from}_${to}"

        // Check cache for charts data (shorter TTL for real-time data)
        getCachedData<ChartsPayload>(cacheKey, ttlMinutes = 5)?.let { cached ->
            Log.d(TAG, "Returning cached charts data for $stationName")
            emit(Resource.Success(cached))
            return@flow
        }

        emit(Resource.Loading)

        try {
            val result = executeWithDeduplication(cacheKey) {
                weatherStationService.getWeatherDataForCharts(
                    stationName = stationName,
                    from = from,
                    to = to
                )
            }

            result.fold(
                onSuccess = { response ->
                    when (response.code()) {
                        200 -> {
                            response.body()?.let { chartsPayload ->
                                cacheData(cacheKey, chartsPayload, ttlMinutes = 5)
                                securityLogger.logDataAccess("charts_data", 1, stationName)
                                Log.d(TAG, "✅ Charts data loaded successfully: ${chartsPayload.charts}")
                                emit(Resource.Success(chartsPayload))
                            } ?: emit(Resource.Error(Exception("Empty response"), "Respuesta vacía del servidor"))
                        }
                        401 -> {
                            securityLogger.logAuthenticationEvent("token_expired", false)
                            // Hacer logout automático y limpiar sesión
                            AuthManager.logout()
                            emit(Resource.Error(Exception("Token expired"), "Sesión expirada. Inicia sesión nuevamente."))
                        }
                        403 -> {
                            securityLogger.logAuthenticationEvent("access_denied", false)
                            emit(Resource.Error(Exception("Access denied"), "No tienes permisos para estos datos."))
                        }
                        else -> {
                            val error = mapHttpError(response.code(), response.message())
                            securityLogger.logNetworkSecurityEvent("charts_data_$stationName", response.code())
                            emit(Resource.Error(Exception("HTTP ${response.code()}"), error))
                        }
                    }
                },
                onFailure = { exception ->
                    Log.e(TAG, "Error getting charts data", exception)
                    securityLogger.logNetworkSecurityEvent("charts_data_$stationName", 0)
                    emit(Resource.Error(exception, "Error de conexión: ${exception.message}"))
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error getting charts data", e)
            emit(Resource.Error(e, "Error inesperado: ${e.message}"))
        }
    }

    /**
     * Get historical weather data with authentication and validation
     */
    fun getWeatherDataTimeRange(
        stationName: String,
        from: String,
        to: String
    ): Flow<Resource<List<WeatherData>>> = flow {
        // Security validation
        if (!isValidStationName(stationName)) {
            emit(Resource.Error(Exception("Invalid station"), "Nombre de estación inválido"))
            return@flow
        }

        if (!isValidDateRange(from, to)) {
            emit(Resource.Error(Exception("Invalid date range"), "Rango de fechas inválido"))
            return@flow
        }

        if (!AuthHelper.isAuthenticated()) {
            securityLogger.logAuthenticationEvent("unauthorized_access", false)
            emit(Resource.Error(Exception("Unauthorized"), "Autenticación requerida"))
            return@flow
        }

        val cacheKey = "historical_${stationName}_${from}_${to}"

        // Check cache for historical data (longer TTL acceptable)
        getCachedData<List<WeatherData>>(cacheKey, ttlMinutes = 10)?.let { cached ->
            Log.d(TAG, "Returning cached historical data for $stationName")
            emit(Resource.Success(cached))
            return@flow
        }

        emit(Resource.Loading)

        try {
            val result = executeWithDeduplication(cacheKey) {
                weatherStationService.getWeatherDataTimeRange(
                    stationName = stationName,
                    from = from,
                    to = to
                )
            }

            result.fold(
                onSuccess = { response ->
                    when (response.code()) {
                        200 -> {
                            response.body()?.let { weatherDataResponse ->
                                val weatherData = weatherDataResponse.data
                                cacheData(cacheKey, weatherData, ttlMinutes = 10)
                                securityLogger.logDataAccess("historical_data", weatherData.size, stationName)
                                emit(Resource.Success(weatherData))
                            } ?: emit(Resource.Error(Exception("Empty response"), "Respuesta vacía del servidor"))
                        }
                        401 -> {
                            securityLogger.logAuthenticationEvent("token_expired", false)
                            // Hacer logout automático y limpiar sesión
                            AuthManager.logout()
                            emit(Resource.Error(Exception("Token expired"), "Sesión expirada. Inicia sesión nuevamente."))
                        }
                        403 -> {
                            securityLogger.logAuthenticationEvent("access_denied", false)
                            emit(Resource.Error(Exception("Access denied"), "No tienes permisos para estos datos."))
                        }
                        else -> {
                            val error = mapHttpError(response.code(), response.message())
                            securityLogger.logNetworkSecurityEvent("historical_data_$stationName", response.code())
                            emit(Resource.Error(Exception("HTTP ${response.code()}"), error))
                        }
                    }
                },
                onFailure = { exception ->
                    Log.e(TAG, "Error getting historical data for $stationName", exception)
                    securityLogger.logNetworkSecurityEvent("historical_data_$stationName", 0)
                    emit(Resource.Error(exception, "Error de conexión: ${exception.message}"))
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error getting historical data", e)
            emit(Resource.Error(e, "Error inesperado: ${e.message}"))
        }
    }
    
    /**
     * Get widget data with validation and caching
     */
    fun getWidgetData(stationName: String): Flow<Resource<WidgetData>> = flow {
        if (!isValidStationName(stationName)) {
            emit(Resource.Error(Exception("Invalid station"), "Nombre de estación inválido"))
            return@flow
        }

        val cacheKey = "widget_$stationName"

        // Check cache with shorter TTL for widget data (real-time importance)
        getCachedData<WidgetData>(cacheKey, ttlMinutes = 2)?.let { cached ->
            Log.d(TAG, "Returning cached widget data for $stationName")
            emit(Resource.Success(cached))
            return@flow
        }

        emit(Resource.Loading)

        try {
            val result = executeWithDeduplication(cacheKey) {
                weatherStationService.getWidgetData(stationName)
            }

            result.fold(
                onSuccess = { response ->
                    if (response.isSuccessful) {
                        response.body()?.let { widgetData ->
                            if (isValidWidgetData(widgetData)) {
                                cacheData(cacheKey, widgetData, ttlMinutes = 2)
                                securityLogger.logDataAccess("widget_data", 1, stationName)
                                emit(Resource.Success(widgetData))
                            } else {
                                Log.w(TAG, "Invalid widget data received for $stationName")
                                emit(Resource.Error(Exception("Invalid data"), "La estación $stationName no tiene datos válidos disponibles"))
                            }
                        } ?: emit(Resource.Error(Exception("Empty response"), "Respuesta vacía del servidor"))
                    } else {
                        val error = mapHttpError(response.code(), response.message())
                        securityLogger.logNetworkSecurityEvent("widget_$stationName", response.code())
                        emit(Resource.Error(Exception("HTTP ${response.code()}"), error))
                    }
                },
                onFailure = { exception ->
                    Log.e(TAG, "Error getting widget data for $stationName", exception)
                    securityLogger.logNetworkSecurityEvent("widget_$stationName", 0)
                    emit(Resource.Error(exception, "Error de conexión: ${exception.message}"))
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error getting widget data", e)
            emit(Resource.Error(e, "Error inesperado: ${e.message}"))
        }
    }
    

    
    // MARK: - Private Helper Methods

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T> getCachedData(key: String, ttlMinutes: Long = Constants.Network.CACHE_MAX_AGE.toLong() / 60): T? {
        return cacheMutex.withLock {
            val entry = cacheStore[key] as? CacheEntry<T>
            if (entry != null && !entry.isExpired(ttlMinutes * 60 * 1000L)) {
                entry.data
            } else {
                if (entry != null) {
                    cacheStore.remove(key) // Remove expired entry
                }
                null
            }
        }
    }

    private suspend fun <T> cacheData(key: String, data: T, ttlMinutes: Long = Constants.Network.CACHE_MAX_AGE.toLong() / 60) {
        cacheMutex.withLock {
            cacheStore[key] = CacheEntry(data)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T> executeWithDeduplication(
        key: String,
        operation: suspend () -> T
    ): Result<T> {
        return cacheMutex.withLock {
            // Check if there's an ongoing request
            val ongoingRequest = ongoingRequests[key] as? Deferred<Result<T>>
            if (ongoingRequest != null && ongoingRequest.isActive) {
                Log.d(TAG, "Deduplicating request for key: $key")
                return@withLock ongoingRequest.await()
            }

            // Create new request
            val deferred = CoroutineScope(Dispatchers.IO).async {
                try {
                    Result.success(operation())
                } catch (e: Exception) {
                    Result.failure(e)
                } finally {
                    ongoingRequests.remove(key)
                }
            }

            ongoingRequests[key] = deferred
            deferred.await()
        }
    }

    private fun isValidStationName(stationName: String): Boolean {
        return stationName.isNotBlank() &&
               stationName.length <= 50 &&
               stationName.matches(Regex("^[a-zA-Z0-9_-]+$"))
    }

    private fun isValidWidgetData(data: WidgetData): Boolean {
        val temperatureValid = data.temperature >= Constants.Validation.MIN_TEMPERATURE &&
                data.temperature <= Constants.Validation.MAX_TEMPERATURE &&
                data.temperature != 0.0 // 0.0°C indica datos faltantes/inválidos en el contexto de Formosa

        val humidityValid = data.relativeHumidity >= Constants.Validation.MIN_HUMIDITY &&
                data.relativeHumidity <= Constants.Validation.MAX_HUMIDITY
                // Removida validación de != 0.0 para humedad ya que puede ser válida

        val isValidData = temperatureValid && humidityValid

        if (!isValidData) {
            Log.w(TAG, "Invalid widget data detected for station ${data.stationName}: " +
                "temp=${data.temperature}°C, humidity=${data.relativeHumidity}%")
        }

        return isValidData
    }

    private fun isValidDateRange(from: String, to: String): Boolean {
        return try {
            // Basic validation - could be enhanced with proper date parsing
            from.isNotBlank() && to.isNotBlank() && from.length >= 10 && to.length >= 10
        } catch (e: Exception) {
            false
        }
    }

    private fun mapHttpError(code: Int, message: String): String {
        return when (code) {
            400 -> "Solicitud inválida"
            401 -> "Sesión expirada. Inicia sesión nuevamente."
            403 -> "No tienes permisos para acceder a estos datos"
            404 -> "Recurso no encontrado"
            429 -> "Demasiadas solicitudes. Intenta más tarde."
            500, 502, 503 -> "Error del servidor. Intenta más tarde."
            else -> "Error de red: $code - $message"
        }
    }

    /**
     * Clear cache for specific keys or all cache
     */
    suspend fun clearCache(key: String? = null) {
        cacheMutex.withLock {
            if (key != null) {
                cacheStore.remove(key)
                Log.d(TAG, "Cache cleared for key: $key")
            } else {
                cacheStore.clear()
                Log.d(TAG, "All cache cleared")
            }
        }
    }
}