package com.example.rafapp.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import com.example.rafapp.models.WeatherStation
import com.example.rafapp.models.TemperatureMaxMin
import com.example.rafapp.models.WeatherDataLastDayResponse
import retrofit2.http.Path

interface WeatherStationService {
    @GET("station")
    fun getWeatherStations(@Header("Authorization") token: String): Call<List<WeatherStation>>

    @GET("station/{id}") // Si necesitas obtener datos de una estación específica
    fun getWeatherStationById(
        @Header("Authorization") token: String,
        @Path("id") stationId: String
    ): Call<WeatherStation>

    @GET("station-data/temp-max-min/{stationId}")
    fun getTemperatureMaxMin(
        @Header("Authorization") token: String,
        @Path("stationId") stationId: String
    ): Call<TemperatureMaxMin>

    @GET("station-data/last-day/{stationId}")
    fun getWeatherDataLastDay(
        @Header("Authorization") token: String,
        @Path("stationId") stationId: String
    ): Call<List<WeatherDataLastDayResponse>>
}
