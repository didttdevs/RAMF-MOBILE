package com.example.rafapp.models

import com.google.gson.annotations.SerializedName

data class WeatherStation(
    @SerializedName("_id") val id: String,
    @SerializedName("meta") val meta: Meta?,
    @SerializedName("name") val name: Name?,
    @SerializedName("dates") val dates: WeatherDates? //
)

data class WeatherDates(
    @SerializedName("min_date") val minDate: String?,
    @SerializedName("max_date") val maxDate: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("last_communication") val lastCommunication: String?
)

data class Meta(
    @SerializedName("airTemp") val airTemp: Double?,
    @SerializedName("rh") val rh: Double?,
    @SerializedName("solarRadiation") val solarRadiation: Double?,
    @SerializedName("rain_last") val rainLast: Double?,
    @SerializedName("rain1h") val rain1h: Double?,
    @SerializedName("windSpeed") val windSpeed: Double?,
    @SerializedName("battery") val battery: Double?,
    @SerializedName("solarPanel") val solarPanel: Double?,
    @SerializedName("time") val time: Long?,
    @SerializedName("rain7d") val rain7d: RainData?,
    @SerializedName("rain2d") val rain2d: RainData?,
    @SerializedName("rain48h") val rain48h: RainData?,
    @SerializedName("rain24h") val rain24h: RainData?,
    @SerializedName("rainCurrentDay") val rainCurrentDay: RainData?
)

data class RainData(
    @SerializedName("vals") val vals: List<Double>?,
    @SerializedName("sum") val sum: Double?
)

data class Name(
    @SerializedName("original") val original: String,
    @SerializedName("custom") val custom: String
)
