package com.cocido.ramfapp.network

import android.content.Context
import com.cocido.ramfapp.BuildConfig
import com.cocido.ramfapp.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Módulo de configuración de red con Retrofit
 */
object NetworkModule {
    
    private const val BASE_URL = "https://api.ramf.com.ar/api/" // Cambiar por la URL real
    private const val TIMEOUT_SECONDS = 30L
    
    /**
     * Interceptor para agregar token de autorización automáticamente
     */
    private fun createAuthInterceptor(context: Context): Interceptor {
        return Interceptor { chain ->
            val tokenManager = TokenManager(context)
            val token = tokenManager.getAccessToken()
            
            val request = if (token != null) {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                chain.request()
            }
            
            chain.proceed(request)
        }
    }
    
    /**
     * Interceptor para logging de requests (solo en debug)
     */
    private fun createLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }
    
    /**
     * Cliente HTTP configurado
     */
    fun createOkHttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(createAuthInterceptor(context))
            .addInterceptor(createLoggingInterceptor())
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * Instancia de Retrofit configurada
     */
    fun createRetrofit(context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * Servicios de API
     */
    fun createAuthService(context: Context): AuthService {
        return createRetrofit(context).create(AuthService::class.java)
    }
    
    fun createWeatherStationService(context: Context): WeatherStationService {
        return createRetrofit(context).create(WeatherStationService::class.java)
    }
    
    fun createReportsService(context: Context): ReportsService {
        return createRetrofit(context).create(ReportsService::class.java)
    }
    
    fun createForecastService(context: Context): ForecastService {
        return createRetrofit(context).create(ForecastService::class.java)
    }
    
    fun createContactService(context: Context): ContactService {
        return createRetrofit(context).create(ContactService::class.java)
    }
    
    fun createProfileService(context: Context): ProfileService {
        return createRetrofit(context).create(ProfileService::class.java)
    }
}






