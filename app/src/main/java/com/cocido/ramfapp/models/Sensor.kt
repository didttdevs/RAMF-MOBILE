package com.cocido.ramfapp.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo para sensores de las estaciones meteorológicas
 */
data class Sensor(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String,
    @SerializedName("unit") val unit: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("min_value") val minValue: Double? = null,
    @SerializedName("max_value") val maxValue: Double? = null,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("calibration_date") val calibrationDate: String? = null,
    @SerializedName("last_maintenance") val lastMaintenance: String? = null
) {
    // Helper para obtener el nombre completo del sensor
    fun getDisplayName(): String = "$name ($unit)"
    
    // Helper para verificar si el sensor necesita mantenimiento
    fun needsMaintenance(): Boolean {
        return lastMaintenance?.let { maintenance ->
            try {
                val maintenanceDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(maintenance)
                val currentDate = java.util.Date()
                val diffInDays = (currentDate.time - (maintenanceDate?.time ?: 0)) / (1000 * 60 * 60 * 24)
                diffInDays > 90 // Mantenimiento cada 3 meses
            } catch (e: Exception) {
                false
            }
        } ?: true
    }
}

/**
 * Modelo para datos de sensores
 */
data class SensorData(
    @SerializedName("sensor_id") val sensorId: String,
    @SerializedName("sensor_name") val sensorName: String,
    @SerializedName("value") val value: Double,
    @SerializedName("unit") val unit: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("quality") val quality: String? = null,
    @SerializedName("station_id") val stationId: String? = null
) {
    // Helper para formatear el valor del sensor
    fun getFormattedValue(): String = "${String.format("%.2f", value)} $unit"
    
    // Helper para verificar la calidad de los datos
    fun isDataQualityGood(): Boolean = quality?.lowercase() == "good" || quality == null
}
