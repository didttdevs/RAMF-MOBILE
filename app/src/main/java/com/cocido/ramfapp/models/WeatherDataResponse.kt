package com.cocido.ramfapp.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo para respuestas de datos meteorológicos que vienen envueltos en un objeto
 */
data class WeatherDataResponse(
    @SerializedName("data") val data: List<WeatherData>,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: String? = null
)




