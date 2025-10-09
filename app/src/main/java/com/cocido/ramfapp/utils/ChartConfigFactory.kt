package com.cocido.ramfapp.utils

import android.graphics.Color
import com.cocido.ramfapp.models.*

/**
 * Factory para crear configuraciones de gráficos basadas en los grupos del backend
 * Cada gráfico corresponde a un grupo de datos del backend
 */
object ChartConfigFactory {

    // Colores profesionales para gráficos (basados en la web)
    private val COLOR_TEMPERATURE = Color.rgb(255, 152, 0)       // Naranja (como en la web)
    private val COLOR_HUMIDITY = Color.rgb(30, 144, 255)         // Azul
    private val COLOR_RADIATION = Color.rgb(255, 193, 7)         // Amarillo/Dorado (área sombreada)
    private val COLOR_VPD = Color.rgb(233, 30, 99)               // Rosa/Magenta (como en la web)
    private val COLOR_DEW_POINT = Color.rgb(0, 188, 212)         // Cyan
    private val COLOR_DELTA_T = Color.rgb(255, 152, 0)           // Naranja
    private val COLOR_WIND_SPEED = Color.rgb(76, 175, 80)        // Verde
    private val COLOR_WIND_GUST = Color.rgb(0, 150, 136)         // Teal
    private val COLOR_WIND_DIRECTION = Color.rgb(158, 158, 158)  // Gris
    private val COLOR_PRECIPITATION = Color.rgb(33, 150, 243)    // Azul claro
    private val COLOR_PRESSURE = Color.rgb(96, 125, 139)         // Azul grisáceo

    /**
     * Obtiene todas las configuraciones de gráficos disponibles
     * Cada gráfico corresponde a un grupo del backend
     */
    fun getAllCharts(): List<ChartConfig> {
        return listOf(
            createTempHumChart(),           // Grupo: tempHum
            createRadiationChart(),         // Grupo: radiacion
            createWindChart(),              // Grupo: viento
            createPrecipitationChart(),     // Grupo: lluvia
            createPressureChart()           // Grupo: presion
        )
    }

    /**
     * Filtra gráficos por categoría
     */
    fun getChartsByCategory(category: ChartCategory): List<ChartConfig> {
        return when (category) {
            ChartCategory.ALL -> getAllCharts()
            else -> getAllCharts().filter { it.category == category }
        }
    }

    /**
     * Filtra gráficos por tipo (selector de dropdown)
     */
    fun getChartsByType(type: String): List<ChartConfig> {
        return when (type.lowercase()) {
            "todos" -> getAllCharts()
            "temp/humedad" -> listOf(createTempHumChart())
            "radiación" -> listOf(createRadiationChart())
            "viento" -> listOf(createWindChart())
            "precipitación" -> listOf(createPrecipitationChart())
            "presión" -> listOf(createPressureChart())
            else -> getAllCharts()
        }
    }

    /**
     * Gráfico: Temperatura y Humedad (Grupo: tempHum del backend)
     * Incluye: Humedad relativa, Temp. aire, Radiación solar, VPD (como en la web)
     */
    private fun createTempHumChart(): ChartConfig {
        return ChartConfig(
            id = "temp_hum",
            title = "Temperatura y Humedad",
            category = ChartCategory.TEMPERATURE,
            description = "Humedad relativa, temperatura del aire, radiación solar y VPD",
            parameters = listOf(
                // Humedad relativa (eje izquierdo) - Azul
                ChartParameter(
                    key = "humedad",
                    label = "Humedad relativa (%)",
                    unit = "%",
                    color = COLOR_HUMIDITY,
                    axisPosition = AxisPosition.LEFT
                ) { data ->
                    data.sensors.hcRelativeHumidity?.avg
                },
                // Temperatura del aire (eje izquierdo) - Naranja
                ChartParameter(
                    key = "temperatura",
                    label = "Temp. aire (°C)",
                    unit = "°C",
                    color = COLOR_TEMPERATURE,
                    axisPosition = AxisPosition.LEFT
                ) { data ->
                    data.sensors.hcAirTemperature?.avg
                },
                // VPD (eje derecho) - Rosa/Magenta
                ChartParameter(
                    key = "vpd",
                    label = "VPD (kPa)",
                    unit = "kPa",
                    color = COLOR_VPD,
                    axisPosition = AxisPosition.RIGHT
                ) { data ->
                    data.sensors.vpd?.avg
                }
            )
        )
    }

    /**
     * Gráfico: Radiación Solar (Grupo: radiacion del backend)
     */
    private fun createRadiationChart(): ChartConfig {
        return ChartConfig(
            id = "radiation",
            title = "Radiación Solar",
            category = ChartCategory.COMBINED, // Usar categoría genérica
            description = "Radiación solar",
            parameters = listOf(
                ChartParameter(
                    key = "radiacion",
                    label = "Radiación solar (W/m²)",
                    unit = "W/m²",
                    color = COLOR_RADIATION,
                    axisPosition = AxisPosition.LEFT
                ) { data ->
                    data.sensors.solarRadiation?.avg
                }
            )
        )
    }

    /**
     * Gráfico: Viento (Grupo: viento del backend)
     * Incluye: Velocidad del viento y Ráfaga
     */
    private fun createWindChart(): ChartConfig {
        return ChartConfig(
            id = "wind",
            title = "Viento",
            category = ChartCategory.WIND,
            description = "Velocidad del viento y ráfagas",
            parameters = listOf(
                ChartParameter(
                    key = "viento",
                    label = "Vel. viento (m/s)",
                    unit = "m/s",
                    color = COLOR_WIND_SPEED,
                    axisPosition = AxisPosition.LEFT
                ) { data ->
                    data.sensors.usonicWindSpeed?.avg
                },
                ChartParameter(
                    key = "rafaga",
                    label = "Ráfaga (m/s)",
                    unit = "m/s",
                    color = COLOR_WIND_GUST,
                    axisPosition = AxisPosition.LEFT
                ) { data ->
                    data.sensors.windGust?.max
                }
            )
        )
    }

    /**
     * Gráfico: Precipitación (Grupo: lluvia del backend)
     */
    private fun createPrecipitationChart(): ChartConfig {
        return ChartConfig(
            id = "precipitation",
            title = "Precipitación",
            category = ChartCategory.PRECIPITATION,
            description = "Precipitación acumulada",
            parameters = listOf(
                ChartParameter(
                    key = "precipitacion",
                    label = "Precipitación (mm)",
                    unit = "mm",
                    color = COLOR_PRECIPITATION,
                    axisPosition = AxisPosition.LEFT
                ) { data ->
                    data.sensors.precipitation?.sum
                }
            )
        )
    }

    /**
     * Gráfico: Presión Atmosférica (Grupo: presion del backend)
     */
    private fun createPressureChart(): ChartConfig {
        return ChartConfig(
            id = "pressure",
            title = "Presión Atmosférica",
            category = ChartCategory.PRESSURE,
            description = "Presión atmosférica",
            parameters = listOf(
                ChartParameter(
                    key = "presion",
                    label = "Presión (hPa)",
                    unit = "hPa",
                    color = COLOR_PRESSURE,
                    axisPosition = AxisPosition.LEFT
                ) { data ->
                    data.sensors.airPressure?.avg
                }
            )
        )
    }
}
