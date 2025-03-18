package com.example.rafapp.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import com.example.rafapp.models.WeatherStation

interface WeatherStationService {
    @GET("station")
    fun getWeatherStations(@Header("Authorization") token: String): Call<List<WeatherStation>>

    @GET("station/{id}") // Si necesitas obtener datos de una estación específica
    fun getWeatherStationById(@Header("Authorization") token: String, @retrofit2.http.Path("id") stationId: String): Call<WeatherStation>
}
