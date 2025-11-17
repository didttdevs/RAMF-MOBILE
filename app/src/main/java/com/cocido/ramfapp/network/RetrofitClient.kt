package com.cocido.ramfapp.network

import android.content.Context
import android.util.Log
import com.cocido.ramfapp.BuildConfig
import com.cocido.ramfapp.models.ApiError
import com.cocido.ramfapp.models.ApiResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Cliente Retrofit con mejores prácticas de seguridad y configuración
 */
object RetrofitClient {
    private const val TAG = "RetrofitClient"
    private const val TIMEOUT_SECONDS = 30L
    private const val MAX_RETRY_ATTEMPTS = 3
    
    private val BASE_URL = BuildConfig.API_BASE_URL
    
    private var authToken: String? = null
    private var refreshToken: String? = null
    private var tokenExpiryTime: Long = 0
    
    /**
     * Configurar tokens de autenticación
     */
    fun setAuthTokens(accessToken: String?, refreshToken: String?, expiresIn: Long = 0) {
        this.authToken = accessToken
        this.refreshToken = refreshToken
        this.tokenExpiryTime = System.currentTimeMillis() + expiresIn
        Log.d(TAG, "Auth tokens updated. Expires in: ${expiresIn}ms")
    }
    
    /**
     * Limpiar tokens de autenticación
     */
    fun clearAuthTokens() {
        this.authToken = null
        this.refreshToken = null
        this.tokenExpiryTime = 0
        Log.d(TAG, "Auth tokens cleared")
    }
    
    /**
     * Verificar si el token necesita ser refrescado
     */
    private fun isTokenExpired(): Boolean {
        return System.currentTimeMillis() >= tokenExpiryTime
    }
    
    /**
     * Interceptor de autenticación mejorado
     */
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        // Lista de endpoints públicos (no requieren autenticación)
        val publicEndpoints = listOf(
            "/auth/login",
            "/auth/register",
            "/auth/request-password-reset",
            "/auth/reset-password",
            "/auth/login/google",
            "/stations",
            "/stations/geo",
            "/stations/",
            "/sensors",
            "/stations-measurement/widget"
            // Nota: /stations-measurement/public/data y /stations-measurement/public-charts 
            // no existen en el servidor (404 Not Found)
        )

        // Verificar si el endpoint requiere autenticación
        val url = originalRequest.url.toString()
        val requiresAuth = !publicEndpoints.any { pattern ->
            when {
                pattern.contains("*") -> {
                    val regex = pattern.replace("*", ".*").replace("/", "\\/")
                    url.matches(Regex(regex))
                }
                else -> {
                    // Usar match exacto o que termine con el patrón para evitar falsos positivos
                    url == pattern || url.endsWith(pattern) || url.contains("$pattern/")
                }
            }
        }

            if (requiresAuth) {
                authToken?.let { token ->
                    val fullToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
                    requestBuilder.addHeader("Authorization", fullToken)
                    Log.d(TAG, "Added accessToken to request: ${url}")
                    Log.d(TAG, "Token being sent: ${fullToken.take(20)}...")
                    Log.d(TAG, "Full token length: ${fullToken.length}")
                    Log.d(TAG, "Token starts with Bearer: ${fullToken.startsWith("Bearer ")}")
                } ?: run {
                    Log.w(TAG, "Auth required but no accessToken available for: ${url}")
                }
            } else {
                Log.d(TAG, "Public endpoint, no auth required: ${url}")
            }

        chain.proceed(requestBuilder.build())
    }
    
    /**
     * Interceptor para manejar errores 401 (Unauthorized) automáticamente
     */
    private val unauthorizedInterceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())
        
        // Si recibimos un 401, limpiar tokens y forzar logout
        if (response.code == 401) {
            Log.w(TAG, "Received 401 Unauthorized, clearing tokens")
            clearAuthTokens()
            
            // Notificar a las Activities que la sesión ha expirado
            // Esto se manejará en BaseActivity
        }
        
        response
    }
    
    /**
     * Interceptor para manejo de errores y reintentos
     */
    private val errorInterceptor = Interceptor { chain ->
        val request = chain.request()
        var response: Response? = null
        var lastException: IOException? = null

        // Intentar la petición hasta MAX_RETRY_ATTEMPTS veces
        repeat(MAX_RETRY_ATTEMPTS) { attempt ->
            try {
                // Cerrar respuesta anterior si existe
                response?.close()
                
                response = chain.proceed(request)

                // Si es exitoso o no es un error de red, salir del loop
                if (response!!.isSuccessful || response!!.code in 400..499) {
                    return@Interceptor response!!
                }

                // Si es error 5xx, intentar de nuevo
                if (response!!.code >= 500) {
                    Log.w(TAG, "Server error ${response!!.code} on attempt ${attempt + 1}")
                    if (attempt < MAX_RETRY_ATTEMPTS - 1) {
                        Thread.sleep((1000 * (attempt + 1)).toLong()) // Backoff exponencial
                    }
                }
            } catch (e: IOException) {
                // Cerrar respuesta en caso de error
                response?.close()
                lastException = e
                Log.w(TAG, "Network error on attempt ${attempt + 1}: ${e.message}")
                if (attempt < MAX_RETRY_ATTEMPTS - 1) {
                    Thread.sleep((1000 * (attempt + 1)).toLong())
                }
            }
        }

        response ?: throw lastException ?: IOException("Network error after $MAX_RETRY_ATTEMPTS attempts")
    }
    
    /**
     * Interceptor para logging seguro
     */
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        // Filtrar información sensible en los logs
        val filteredMessage = message
            .replace(Regex("""("password"\s*:\s*")[^"]*(")"""), "$1***$2")
            .replace(Regex("""("token"\s*:\s*")[^"]*(")"""), "$1***$2")
            .replace(Regex("""("access_token"\s*:\s*")[^"]*(")"""), "$1***$2")
            .replace(Regex("""(Authorization:\s*Bearer\s)[^\s]+"""), "$1***")
        
        Log.d(TAG, filteredMessage)
    }.apply {
        level = if (BuildConfig.DEBUG_MODE) {
            HttpLoggingInterceptor.Level.HEADERS
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
    
    /**
     * Configuración Gson personalizada
     */
    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .setLenient()
        .create()
    
    /**
     * Configuración OkHttpClient con mejores prácticas de seguridad
     */
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(unauthorizedInterceptor)
        .addInterceptor(errorInterceptor)
        .apply {
            if (BuildConfig.DEBUG_MODE) {
                addInterceptor(loggingInterceptor)
            }
        }
        .retryOnConnectionFailure(true)
        .build()

    /**
     * Configuración Retrofit
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * Servicios de la API
     */
    val weatherStationService: WeatherStationService by lazy {
        retrofit.create(WeatherStationService::class.java)
    }

    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }
    
    val profileService: ProfileService by lazy {
        retrofit.create(ProfileService::class.java)
    }
    
    val userService: UserService by lazy {
        retrofit.create(UserService::class.java)
    }
    
    /**
     * Helper para manejar respuestas de la API de forma consistente
     */
    fun <T> handleApiResponse(response: retrofit2.Response<ApiResponse<T>>): Result<T> {
        return try {
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    val errorMessage = apiResponse?.message ?: apiResponse?.error?.message ?: "Error desconocido"
                    Result.failure(Exception(errorMessage))
                }
            } else {
                // Log del cuerpo de la respuesta para debug
                val responseBody = response.errorBody()?.string()
                Log.e(TAG, "HTTP Error ${response.code()}: ${response.message()}")
                Log.e(TAG, "Response body: $responseBody")
                
                val errorMessage = when (response.code()) {
                    400 -> "Error en la solicitud: ${responseBody ?: response.message()}"
                    401 -> "No autorizado. Por favor, inicia sesión nuevamente."
                    403 -> "Acceso denegado. No tienes permisos para realizar esta acción."
                    404 -> "Recurso no encontrado."
                    500 -> "Error interno del servidor. Intenta más tarde."
                    else -> "Error de red: ${response.code()} - ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
