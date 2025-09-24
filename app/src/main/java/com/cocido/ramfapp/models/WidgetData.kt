package com.cocido.ramfapp.models

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

data class WidgetData(
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("temperature") val temperature: Double,
    @SerializedName("maxTemperature") val maxTemperature: Double,  // ← Corregido
    @SerializedName("minTemperature") val minTemperature: Double,  // ← Corregido
    @SerializedName("relativeHumidity") val relativeHumidity: Double,  // ← Corregido
    @SerializedName("dewPoint") val dewPoint: Double,  // ← Corregido
    @SerializedName("airPressure") val airPressure: Double,  // ← Corregido
    @SerializedName("solarRadiation") val solarRadiation: Double,  // ← Corregido
    @SerializedName("windSpeed") val windSpeed: Double,  // ← Corregido
    @SerializedName("windDirection") val windDirection: String,  // ← Corregido
    @SerializedName("rainLastHour") val rainLastHour: Double,  // ← Corregido
    @SerializedName("rainDay") val rainDay: Double,  // ← Corregido
    @SerializedName("rain24h") val rain24h: Double,  // ← Corregido
    @SerializedName("rain48h") val rain48h: Double,  // ← Corregido
    @SerializedName("rain7d") val rain7d: Double,  // ← Corregido
    @SerializedName("stationName") val stationName: String? = null,  // ← Corregido
    @SerializedName("dataQuality") val dataQuality: String? = null,  // ← Corregido
    @SerializedName("lastUpdate") val lastUpdate: String? = null  // ← Corregido
) {
    // Helper para verificar si un valor es válido (no nulo, no NaN)
    private fun isValidValue(value: Double): Boolean {
        return !value.isNaN() && value.isFinite()
    }
    
    // Helper para verificar si un valor es válido y no cero (para parámetros donde 0 no tiene sentido)
    private fun isValidNonZeroValue(value: Double): Boolean {
        return !value.isNaN() && value != 0.0 && value.isFinite()
    }

    // Helper para formatear la temperatura
    fun getFormattedTemperature(): String {
        return if (isValidValue(temperature)) "${String.format("%.1f", temperature)}°C" else "N/A"
    }

    // Helper para formatear la temperatura máxima
    fun getFormattedMaxTemperature(): String {
        return if (isValidValue(maxTemperature)) "${String.format("%.1f", maxTemperature)}°C" else "N/A"
    }

    // Helper para formatear la temperatura mínima
    fun getFormattedMinTemperature(): String {
        return if (isValidValue(minTemperature)) "${String.format("%.1f", minTemperature)}°C" else "N/A"
    }

    // Helper para formatear la humedad relativa
    fun getFormattedHumidity(): String {
        return if (isValidValue(relativeHumidity)) "${String.format("%.1f", relativeHumidity)}%" else "N/A"
    }

    // Helper para formatear la velocidad del viento
    fun getFormattedWindSpeed(): String {
        return if (isValidNonZeroValue(windSpeed)) "${String.format("%.1f", windSpeed)} km/h" else "N/A"
    }

    // Helper para formatear la presión atmosférica (redondeada a 2 decimales)
    fun getFormattedPressure(): String {
        return if (isValidValue(airPressure)) "${String.format("%.2f", airPressure)} hPa" else "N/A"
    }

    // Helper para formatear el punto de rocío
    fun getFormattedDewPoint(): String {
        return if (isValidValue(dewPoint)) "${String.format("%.1f", dewPoint)}°C" else "N/A"
    }

    // Helper para formatear la radiación solar
    fun getFormattedSolarRadiation(): String {
        return if (isValidNonZeroValue(solarRadiation)) "${String.format("%.1f", solarRadiation)} W/m²" else "N/A"
    }
    
    // Helper para obtener la dirección del viento en formato legible
    fun getWindDirectionText(): String {
        // Verificar si windDirection es null o vacío
        if (windDirection.isNullOrBlank()) {
            return "N/A"
        }
        
        // Si windDirection es un número (grados), convertirlo a dirección cardinal
        val degrees = windDirection.toDoubleOrNull()
        if (degrees != null && degrees >= 0 && degrees <= 360) {
            return when {
                degrees >= 348.75 || degrees < 11.25 -> "N"
                degrees >= 11.25 && degrees < 33.75 -> "NNE"
                degrees >= 33.75 && degrees < 56.25 -> "NE"
                degrees >= 56.25 && degrees < 78.75 -> "ENE"
                degrees >= 78.75 && degrees < 101.25 -> "E"
                degrees >= 101.25 && degrees < 123.75 -> "ESE"
                degrees >= 123.75 && degrees < 146.25 -> "SE"
                degrees >= 146.25 && degrees < 168.75 -> "SSE"
                degrees >= 168.75 && degrees < 191.25 -> "S"
                degrees >= 191.25 && degrees < 213.75 -> "SSO"
                degrees >= 213.75 && degrees < 236.25 -> "SO"
                degrees >= 236.25 && degrees < 258.75 -> "OSO"
                degrees >= 258.75 && degrees < 281.25 -> "O"
                degrees >= 281.25 && degrees < 303.75 -> "ONO"
                degrees >= 303.75 && degrees < 326.25 -> "NO"
                degrees >= 326.25 && degrees < 348.75 -> "NNO"
                else -> "${degrees.toInt()}°"
            }
        }
        
        // Si no es un número, usar el mapeo original
        return when {
            windDirection.contains("N") -> "Norte"
            windDirection.contains("S") -> "Sur"
            windDirection.contains("E") -> "Este"
            windDirection.contains("W") -> "Oeste"
            windDirection.contains("NE") -> "Noreste"
            windDirection.contains("NW") -> "Noroeste"
            windDirection.contains("SE") -> "Sureste"
            windDirection.contains("SW") -> "Suroeste"
            else -> windDirection
        }
    }
    
    // Helper para verificar si los datos están actualizados (menos de 1 hora)
    fun isDataRecent(): Boolean {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            val dataTime = dateFormat.parse(timestamp)
            val currentTime = Date()
            val diffInMinutes = (currentTime.time - (dataTime?.time ?: 0)) / (1000 * 60)
            diffInMinutes < 60
        } catch (e: Exception) {
            false
        }
    }
}