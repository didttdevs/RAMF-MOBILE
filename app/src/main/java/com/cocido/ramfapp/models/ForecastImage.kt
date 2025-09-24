package com.cocido.ramfapp.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo para imágenes de pronósticos meteorológicos
 */
data class ForecastImage(
    @SerializedName("id") val id: String,
    @SerializedName("station_name") val stationName: String,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("image_type") val imageType: String, // "radar", "satellite", "forecast", etc.
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("valid_from") val validFrom: String,
    @SerializedName("valid_to") val validTo: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("file_size") val fileSize: Long? = null,
    @SerializedName("resolution") val resolution: String? = null
) {
    // Helper para verificar si la imagen está vigente
    fun isValid(): Boolean {
        return try {
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
            val validToDate = dateFormat.parse(validTo)
            val currentDate = java.util.Date()
            currentDate.before(validToDate)
        } catch (e: Exception) {
            false
        }
    }
    
    // Helper para obtener el tipo de imagen en formato legible
    fun getImageTypeDisplay(): String {
        return when (imageType.lowercase()) {
            "radar" -> "Imagen de Radar"
            "satellite" -> "Imagen Satelital"
            "forecast" -> "Pronóstico"
            "temperature" -> "Mapa de Temperatura"
            "precipitation" -> "Mapa de Precipitación"
            "wind" -> "Mapa de Viento"
            else -> imageType
        }
    }
}
