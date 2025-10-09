package com.cocido.ramfapp.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo para la respuesta de datos de gráficos del backend
 * Formato: { "charts": { "tempHum": [...], "viento": [...], ... } }
 */
data class ChartsPayload(
    @SerializedName("charts") val charts: ChartsByGroup
)

/**
 * Grupos de gráficos predefinidos por el backend
 */
data class ChartsByGroup(
    @SerializedName("radiacion") val radiacion: List<ChartPoint>? = null,
    @SerializedName("energia") val energia: List<ChartPoint>? = null,
    @SerializedName("lluvia") val lluvia: List<ChartPoint>? = null,
    @SerializedName("tempHum") val tempHum: List<ChartPoint>? = null,
    @SerializedName("viento") val viento: List<ChartPoint>? = null,
    @SerializedName("direccion") val direccion: List<ChartPoint>? = null,
    @SerializedName("presion") val presion: List<ChartPoint>? = null,
    @SerializedName("et0") val et0: List<ChartPoint>? = null
)

/**
 * Punto de datos para gráficos con fecha y valores dinámicos
 */
data class ChartPoint(
    @SerializedName("date") val date: String,
    // Campos dinámicos según el tipo de gráfico
    @SerializedName("Temp. aire (°C)") val temperatura: Double? = null,
    @SerializedName("Humedad relativa (%)") val humedad: Double? = null,
    @SerializedName("Punto de rocío (°C)") val puntoRocio: Double? = null,
    @SerializedName("VPD (kPa)") val vpd: Double? = null,
    @SerializedName("Delta T (°C)") val deltaT: Double? = null,
    @SerializedName("Vel. viento (m/s)") val velocidadViento: Double? = null,
    @SerializedName("Ráfaga (m/s)") val rafaga: Double? = null,
    @SerializedName("Dir. viento (°)") val direccionViento: Double? = null,
    @SerializedName("Presión (hPa)") val presion: Double? = null,
    @SerializedName("Precipitación (mm)") val precipitacion: Double? = null,
    @SerializedName("Radiación solar (W/m²)") val radiacionSolar: Double? = null,
    @SerializedName("Batería (mV)") val bateria: Double? = null,
    @SerializedName("Panel solar (mV)") val panelSolar: Double? = null,
    @SerializedName("ET0 (mm)") val et0: Double? = null,
    @SerializedName("Horas sol (h)") val horasSol: Double? = null,
    @SerializedName("Orientación (°)") val orientacion: Double? = null
)
