package com.cocido.ramfapp.utils

import android.graphics.Color
import com.cocido.ramfapp.models.*

/**
 * Factory para crear configuraciones de gráficos predefinidas
 * Inspirado en la web RAMF y FieldClimate METOS
 */
object ChartConfigFactory {

    // Colores profesionales para gráficos
    private val COLOR_TEMPERATURE = Color.rgb(255, 99, 71)       // Rojo-naranja
    private val COLOR_HUMIDITY = Color.rgb(30, 144, 255)         // Azul
    private val COLOR_RADIATION = Color.rgb(255, 193, 7)         // Amarillo/Dorado
    private val COLOR_VPD = Color.rgb(156, 39, 176)              // Púrpura
    private val COLOR_PRECIPITATION = Color.rgb(33, 150, 243)    // Azul claro
    private val COLOR_DELTA_T = Color.rgb(255, 152, 0)           // Naranja
    private val COLOR_WIND_SPEED = Color.rgb(76, 175, 80)        // Verde
    private val COLOR_WIND_GUST = Color.rgb(0, 150, 136)         // Teal
    private val COLOR_WIND_DIRECTION = Color.rgb(158, 158, 158)  // Gris
    private val COLOR_PRESSURE = Color.rgb(96, 125, 139)         // Azul grisáceo
    private val COLOR_DEW_POINT = Color.rgb(0, 188, 212)         // Cyan

    /**
     * Obtiene todas las configuraciones de gráficos disponibles
     */
    fun getAllCharts(): List<ChartConfig> {
        return listOf(
            createCombinedEnvironmentChart(),      // Temperatura + Humedad
            createPrecipitationDeltaTChart(),      // Precipitación + Delta T + Humedad
            createWindChart(),                     // Viento: Velocidad + Dirección + Ráfagas
            createRadiationOnlyChart(),            // Radiación Solar (separada)
            createPressureChart(),                 // Presión Atmosférica
            createTemperatureOnlyChart(),          // Temperatura individual
            createHumidityOnlyChart(),             // Humedad individual
            createDewPointChart()                  // Punto de Rocío
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
     * Gráfico Combinado: Temperatura + Humedad (como en la página web)
     * Solo 2 parámetros para evitar problemas de escalado
     */
    private fun createCombinedEnvironmentChart(): ChartConfig {
        return ChartConfig(
            id = "combined_environment",
            title = "Temperatura y Humedad",
            category = ChartCategory.COMBINED,
            description = "Temperatura del aire y humedad relativa",
            parameters = listOf(
                // Temperatura en eje izquierdo
                ChartParameter(
                    key = "temperatura",
                    label = "Temp. aire",
                    unit = "°C",
                    color = COLOR_TEMPERATURE,
                    axisPosition = AxisPosition.LEFT
                ) { data ->
                    data.sensors.hcAirTemperature?.avg
                },
                // Humedad en eje derecho
                ChartParameter(
                    key = "humedad",
                    label = "Humedad relativa",
                    unit = "%",
                    color = COLOR_HUMIDITY,
                    axisPosition = AxisPosition.RIGHT
                ) { data ->
                    data.sensors.hcRelativeHumidity?.avg
                }
            )
        )
    }

    /**
     * Gráfico: Precipitación + Delta T + Humedad (como en la página web)
     */
    private fun createPrecipitationDeltaTChart(): ChartConfig {
        return ChartConfig(
            id = "precipitation_deltat",
            title = "Precipitación",
            category = ChartCategory.PRECIPITATION,
            description = "Precipitación, Delta T y Humedad relativa",
            parameters = listOf(
                // Precipitación en eje izquierdo
                ChartParameter(
                    key = "precipitacion",
                    label = "Precipitación",
                    unit = "mm",
                    color = COLOR_PRECIPITATION,
                    axisPosition = AxisPosition.LEFT
                ) { data ->
                    data.sensors.precipitation?.sum
                },
                // Delta T en eje derecho
                ChartParameter(
                    key = "deltaT",
                    label = "Delta T",
                    unit = "°C",
                    color = COLOR_DELTA_T,
                    axisPosition = AxisPosition.RIGHT
                ) { data ->
                    // Delta T = Temperatura - Punto de Rocío
                    val temp = data.sensors.hcAirTemperature?.avg
                    val dewPoint = data.sensors.dewPoint?.avg
                    if (temp != null && dewPoint != null) {
                        temp - dewPoint
                    } else null
                },
                // Humedad en eje derecho (tercer parámetro)
                ChartParameter(
                    key = "humedad",
                    label = "Humedad relativa",
                    unit = "%",
                    color = COLOR_HUMIDITY,
                    axisPosition = AxisPosition.RIGHT
                ) { data ->
                    data.sensors.hcRelativeHumidity?.avg
                }
            )
        )
    }

    /**
     * Gráfico: Viento - Velocidad, Dirección y Ráfagas (como en la página web)
     */
    private fun createWindChart(): ChartConfig {
        return ChartConfig(
            id = "wind",
            title = "Viento",
            category = ChartCategory.WIND,
            description = "Velocidad del viento, dirección y ráfagas",
            parameters = listOf(
                // Velocidad del viento en eje izquierdo
                ChartParameter(
                    key = "vientoVel",
                    label = "Vel. viento",
                    unit = "m/s",
                    color = COLOR_WIND_SPEED,
                    axisPosition = AxisPosition.LEFT
                ) { data ->
                    data.sensors.usonicWindSpeed?.avg
                },
                // Ráfagas en eje izquierdo (línea punteada)
                ChartParameter(
                    key = "windGust",
                    label = "Ráfaga",
                    unit = "m/s",
                    color = COLOR_WIND_GUST,
                    axisPosition = AxisPosition.LEFT
                ) { data ->
                    data.sensors.windGust?.max
                },
                // Dirección del viento en eje derecho
                ChartParameter(
                    key = "vientoDireccion",
                    label = "Dir. viento",
                    unit = "°",
                    color = COLOR_WIND_DIRECTION,
                    axisPosition = AxisPosition.RIGHT
                ) { data ->
                    data.sensors.usonicWindDir?.last
                }
            )
        )
    }

    /**
     * Gráfico: Temperatura del Aire (solo)
     */
    private fun createTemperatureOnlyChart(): ChartConfig {
        return ChartConfig(
            id = "temperature",
            title = "Temperatura del Aire",
            category = ChartCategory.TEMPERATURE,
            parameters = listOf(
                ChartParameter(
                    key = "temperatura",
                    label = "Temperatura",
                    unit = "°C",
                    color = COLOR_TEMPERATURE,
                    axisPosition = AxisPosition.LEFT
                ) { data ->
                    data.sensors.hcAirTemperature?.avg
                }
            )
        )
    }

    /**
     * Gráfico: Humedad Relativa (solo)
     */
    private fun createHumidityOnlyChart(): ChartConfig {
        return ChartConfig(
            id = "humidity",
            title = "Humedad Relativa",
            category = ChartCategory.HUMIDITY,
            parameters = listOf(
                ChartParameter(
                    key = "humedad",
                    label = "Humedad",
                    unit = "%",
                    color = COLOR_HUMIDITY,
                    axisPosition = AxisPosition.LEFT
                ) { data ->
                    data.sensors.hcRelativeHumidity?.avg
                }
            )
        )
    }

    /**
     * Gráfico: Presión Atmosférica
     */
    private fun createPressureChart(): ChartConfig {
        return ChartConfig(
            id = "pressure",
            title = "Presión Atmosférica",
            category = ChartCategory.PRESSURE,
            parameters = listOf(
                ChartParameter(
                    key = "presion",
                    label = "Presión",
                    unit = "kPa",
                    color = COLOR_PRESSURE,
                    axisPosition = AxisPosition.LEFT
                ) { data ->
                    data.sensors.airPressure?.avg
                }
            )
        )
    }

    /**
     * Gráfico: Radiación Solar (solo)
     */
    private fun createRadiationOnlyChart(): ChartConfig {
        return ChartConfig(
            id = "radiation",
            title = "Radiación Solar",
            category = ChartCategory.RADIATION,
            parameters = listOf(
                ChartParameter(
                    key = "radiacion",
                    label = "Radiación",
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
     * Gráfico: Punto de Rocío
     */
    private fun createDewPointChart(): ChartConfig {
        return ChartConfig(
            id = "dewpoint",
            title = "Punto de Rocío",
            category = ChartCategory.TEMPERATURE,
            parameters = listOf(
                ChartParameter(
                    key = "dewPoint",
                    label = "Punto de Rocío",
                    unit = "°C",
                    color = COLOR_DEW_POINT,
                    axisPosition = AxisPosition.LEFT
                ) { data ->
                    data.sensors.dewPoint?.avg
                }
            )
        )
    }

    /**
     * Mapea un ChartType a una lista de ChartConfig
     */
    fun getChartsByType(type: String): List<ChartConfig> {
        return when (type) {
            "Todos los gráficos" -> getAllCharts()
            "Gráficos combinados" -> getAllCharts().filter { it.parameters.size > 1 }
            "Gráficos individuales" -> getAllCharts().filter { it.parameters.size == 1 }
            "Temperatura" -> getChartsByCategory(ChartCategory.TEMPERATURE)
            "Humedad" -> getChartsByCategory(ChartCategory.HUMIDITY)
            "Viento" -> getChartsByCategory(ChartCategory.WIND)
            "Precipitación" -> getChartsByCategory(ChartCategory.PRECIPITATION)
            "Presión" -> getChartsByCategory(ChartCategory.PRESSURE)
            "Radiación" -> getChartsByCategory(ChartCategory.RADIATION)
            else -> getAllCharts()
        }
    }
}

