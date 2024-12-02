package com.example.rafapp.network

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import com.example.rafapp.models.WeatherStation

interface WeatherStationService {
    @GET("station")
    fun getWeatherStations(): Call<List<WeatherStation>>
}

object RetrofitClient {
    private const val BASE_URL = "https://ramf.formosa.gob.ar/api/"

    val instance: WeatherStationService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(WeatherStationService::class.java)
    }
}
