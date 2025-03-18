package com.example.rafapp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://ramf.formosa.gob.ar/api/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Instancia del servicio de estaciones meteorológicas
    val weatherStationService: WeatherStationService by lazy {
        retrofit.create(WeatherStationService::class.java)
    }

    // Instancia del servicio de autenticación
    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }
}
