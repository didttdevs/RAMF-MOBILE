package com.example.rafapp.models

import com.google.gson.annotations.SerializedName

data class WeatherStation(
    @SerializedName("meta") val meta: Meta?,
    @SerializedName("name") val name: Name?
)

data class Meta(
    @SerializedName("airTemp") val airTemp: Double,  // Temperatura actual
    @SerializedName("rh") val rh: Double,  // Humedad relativa
    @SerializedName("solarRadiation") val solarRadiation: Double,  // Radiación solar
    @SerializedName("rain_last") val rainLast: Double,  // Lluvia en la última hora
    @SerializedName("rain1h") val rain1h: Double,  // Lluvia en la última 1 hora
    @SerializedName("windSpeed") val windSpeed: Double,  // Velocidad del viento
    @SerializedName("battery") val battery: Double,  // Nivel de batería
    @SerializedName("solarPanel") val solarPanel: Double,  // Energía del panel solar
    @SerializedName("time") val time: Long,  // Timestamp de la medición
    @SerializedName("rain7d") val rain7d: RainData,  // Datos de lluvia en los últimos 7 días
    @SerializedName("rain2d") val rain2d: RainData,  // Datos de lluvia en los últimos 2 días
    @SerializedName("rain48h") val rain48h: RainData,  // Datos de lluvia en las últimas 48 horas
    @SerializedName("rain24h") val rain24h: RainData,  // Datos de lluvia en las últimas 24 horas
    @SerializedName("rainCurrentDay") val rainCurrentDay: RainData  // Datos de lluvia en el día actual
)

data class RainData(
    @SerializedName("vals") val vals: List<Double>,  // Valores históricos de lluvia
    @SerializedName("sum") val sum: Double  // Total acumulado de lluvia
)

data class Name(
    @SerializedName("original") val original: String,
    @SerializedName("custom") val custom: String
)
