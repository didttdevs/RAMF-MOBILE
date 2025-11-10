package com.cocido.ramfapp.utils

import android.graphics.Color
import com.cocido.ramfapp.models.ChartAxisConfig
import com.cocido.ramfapp.models.ChartCategory
import com.cocido.ramfapp.models.ChartConfig
import com.cocido.ramfapp.models.ChartDataGroup
import com.cocido.ramfapp.models.ChartParameter
import com.cocido.ramfapp.models.ChartValueKey
import com.cocido.ramfapp.models.SeriesOptions
import com.cocido.ramfapp.models.SeriesStyle
import com.cocido.ramfapp.models.AxisPosition

/**
 * Factory para crear configuraciones de gráficos basadas en la estructura devuelta por el backend.
 * Las series se describen con metadatos que permiten combinar grupos diferentes como en la versión web.
 */
object ChartConfigFactory {

    // Paleta alineada con la versión web
    private val COLOR_TEMPERATURE = Color.rgb(255, 152, 0)
    private val COLOR_HUMIDITY = Color.rgb(30, 144, 255)
    private val COLOR_RADIATION = Color.rgb(255, 193, 7)
    private val COLOR_VPD = Color.rgb(233, 30, 99)
    private val COLOR_DELTA_T = Color.rgb(255, 112, 67)
    private val COLOR_WIND_SPEED = Color.rgb(76, 175, 80)
    private val COLOR_WIND_GUST = Color.rgb(0, 150, 136)
    private val COLOR_WIND_DIRECTION = Color.rgb(142, 36, 170)
    private val COLOR_PRECIPITATION = Color.rgb(255, 171, 64)
    private val COLOR_PRESSURE = Color.rgb(96, 125, 139)
    private val COLOR_ET0 = Color.rgb(55, 71, 79)

    private const val AXIS_TEMPERATURE = "axis_temperature"
    private const val AXIS_HUMIDITY = "axis_humidity"
    private const val AXIS_RADIATION = "axis_radiation"
    private const val AXIS_VPD = "axis_vpd"
    private const val AXIS_PRECIPITATION = "axis_precipitation"
    private const val AXIS_DELTA_T = "axis_delta_t"
    private const val AXIS_WIND_SPEED = "axis_wind_speed"
    private const val AXIS_WIND_DIRECTION = "axis_wind_direction"
    private const val AXIS_ET0 = "axis_et0"
    private const val AXIS_PRESSURE = "axis_pressure"

    fun getAllCharts(): List<ChartConfig> {
        return listOf(
            createTempHumChart(),
            createRadiationChart(),
            createPrecipitationChart(),
            createWindChart(),
            createEvapotranspirationChart(),
            createPressureChart()
        )
    }

    fun getChartsByCategory(category: ChartCategory): List<ChartConfig> {
        return when (category) {
            ChartCategory.ALL -> getAllCharts()
            else -> getAllCharts().filter { it.category == category }
        }
    }

    fun getChartsByType(type: String): List<ChartConfig> {
        return when (type.lowercase()) {
            "todos" -> getAllCharts()
            "temp/humedad" -> listOf(createTempHumChart())
            "radiación" -> listOf(createRadiationChart())
            "precipitación" -> listOf(createPrecipitationChart())
            "viento" -> listOf(createWindChart())
            "evapotranspiración" -> listOf(createEvapotranspirationChart())
            "presión" -> listOf(createPressureChart())
            else -> getAllCharts()
        }
    }

    private fun createTempHumChart(): ChartConfig {
        return ChartConfig(
            id = "temp_hum",
            title = "Temperatura y Humedad",
            category = ChartCategory.TEMPERATURE,
            description = "Humedad relativa, temperatura del aire, radiación y VPD",
            parameters = listOf(
                ChartParameter(
                    key = "humedad",
                    label = "Humedad relativa (%)",
                    unit = "%",
                    color = COLOR_HUMIDITY,
                    axisPosition = AxisPosition.RIGHT,
                    axisId = AXIS_HUMIDITY,
                    sourceGroup = ChartDataGroup.TEMP_HUM,
                    valueKey = ChartValueKey.HUMEDAD,
                    seriesOptions = SeriesOptions(
                        style = SeriesStyle.LINE,
                        lineWidth = 3f
                    )
                ),
                ChartParameter(
                    key = "temperatura",
                    label = "Temp. aire (°C)",
                    unit = "°C",
                    color = COLOR_TEMPERATURE,
                    axisPosition = AxisPosition.LEFT,
                    axisId = AXIS_TEMPERATURE,
                    sourceGroup = ChartDataGroup.TEMP_HUM,
                    valueKey = ChartValueKey.TEMPERATURA,
                    seriesOptions = SeriesOptions(
                        style = SeriesStyle.LINE,
                        lineWidth = 3.2f
                    )
                ),
                ChartParameter(
                    key = "radiacion",
                    label = "Radiación solar (W/m²)",
                    unit = "W/m²",
                    color = COLOR_RADIATION,
                    axisPosition = AxisPosition.RIGHT,
                    axisId = AXIS_RADIATION,
                    sourceGroup = ChartDataGroup.RADIACION,
                    valueKey = ChartValueKey.RADIACION_SOLAR,
                    seriesOptions = SeriesOptions(
                        style = SeriesStyle.AREA,
                        lineWidth = 1.5f,
                        fillAlpha = 28,
                        fillColorOverride = COLOR_RADIATION
                    ),
                    scaleFactor = 0.075
                ),
                ChartParameter(
                    key = "vpd",
                    label = "VPD (kPa)",
                    unit = "kPa",
                    color = COLOR_VPD,
                    axisPosition = AxisPosition.RIGHT,
                    axisId = AXIS_VPD,
                    sourceGroup = ChartDataGroup.TEMP_HUM,
                    valueKey = ChartValueKey.VPD,
                    seriesOptions = SeriesOptions(
                        style = SeriesStyle.DASHED_LINE,
                        lineWidth = 2.2f,
                        dashedIntervals = floatArrayOf(12f, 6f)
                    ),
                    scaleFactor = 18.0
                )
            ),
            axes = listOf(
                ChartAxisConfig(
                    id = AXIS_TEMPERATURE,
                    position = AxisPosition.LEFT,
                    label = "Temperatura",
                    unit = "°C",
                    color = COLOR_TEMPERATURE,
                    min = 0.0,
                    labelCount = 7,
                    formatPattern = "#0.0",
                    scaleFactor = 1.0,
                    forceZeroInRange = true
                ),
                ChartAxisConfig(
                    id = AXIS_HUMIDITY,
                    position = AxisPosition.RIGHT,
                    label = "Humedad relativa",
                    unit = "%",
                    color = COLOR_HUMIDITY,
                    min = 0.0,
                    max = 100.0,
                    labelCount = 5,
                    formatPattern = "#0",
                    scaleFactor = 1.0,
                    forceZeroInRange = true,
                    overlayPriority = 0
                ),
                ChartAxisConfig(
                    id = AXIS_RADIATION,
                    position = AxisPosition.RIGHT,
                    label = "Radiación solar",
                    unit = "W/m²",
                    color = COLOR_RADIATION,
                    min = 0.0,
                    max = 1400.0,
                    labelCount = 6,
                    formatPattern = "#0",
                    scaleFactor = 0.075,
                    forceZeroInRange = true,
                    overlayPriority = 1
                ),
                ChartAxisConfig(
                    id = AXIS_VPD,
                    position = AxisPosition.RIGHT,
                    label = "VPD",
                    unit = "kPa",
                    color = COLOR_VPD,
                    min = 0.0,
                    max = 2.2,
                    labelValues = listOf(0.0, 0.55, 1.1, 1.65, 2.2),
                    formatPattern = "#0.00",
                    scaleFactor = 18.0,
                    forceZeroInRange = true,
                    overlayPriority = 2
                )
            )
        )
    }

    private fun createRadiationChart(): ChartConfig {
        return ChartConfig(
            id = "radiation",
            title = "Radiación Solar",
            category = ChartCategory.COMBINED,
            description = "Irradiancia global",
            parameters = listOf(
                ChartParameter(
                    key = "radiacion",
                    label = "Radiación solar (W/m²)",
                    unit = "W/m²",
                    color = COLOR_RADIATION,
                    axisPosition = AxisPosition.LEFT,
                    axisId = AXIS_RADIATION,
                    sourceGroup = ChartDataGroup.RADIACION,
                    valueKey = ChartValueKey.RADIACION_SOLAR,
                    seriesOptions = SeriesOptions(
                        style = SeriesStyle.AREA,
                        lineWidth = 2f,
                        fillAlpha = 32,
                        fillColorOverride = COLOR_RADIATION
                    )
                )
            ),
            axes = listOf(
                ChartAxisConfig(
                    id = AXIS_RADIATION,
                    position = AxisPosition.LEFT,
                    label = "Radiación solar",
                    unit = "W/m²",
                    color = COLOR_RADIATION,
                    min = 0.0,
                    max = 1400.0,
                    labelCount = 6,
                    formatPattern = "#0",
                    scaleFactor = 1.0,
                    forceZeroInRange = true
                )
            )
        )
    }

    private fun createPrecipitationChart(): ChartConfig {
        return ChartConfig(
            id = "precipitation",
            title = "Precipitación",
            category = ChartCategory.PRECIPITATION,
            description = "Precipitación acumulada, HR y Delta T",
            parameters = listOf(
                ChartParameter(
                    key = "precipitacion",
                    label = "Precipitación (mm)",
                    unit = "mm",
                    color = COLOR_PRECIPITATION,
                    axisPosition = AxisPosition.LEFT,
                    axisId = AXIS_PRECIPITATION,
                    sourceGroup = ChartDataGroup.LLUVIA,
                    valueKey = ChartValueKey.PRECIPITACION,
                    seriesOptions = SeriesOptions(
                        style = SeriesStyle.STEP,
                        lineWidth = 2.6f
                    )
                ),
                ChartParameter(
                    key = "humedad_relativa",
                    label = "Humedad relativa (%)",
                    unit = "%",
                    color = COLOR_HUMIDITY,
                    axisPosition = AxisPosition.RIGHT,
                    axisId = AXIS_HUMIDITY,
                    sourceGroup = ChartDataGroup.TEMP_HUM,
                    valueKey = ChartValueKey.HUMEDAD,
                    seriesOptions = SeriesOptions(
                        style = SeriesStyle.DASHED_LINE,
                        lineWidth = 2.2f,
                        dashedIntervals = floatArrayOf(10f, 6f)
                    )
                ),
                ChartParameter(
                    key = "delta_t",
                    label = "Delta T (°C)",
                    unit = "°C",
                    color = COLOR_DELTA_T,
                    axisPosition = AxisPosition.RIGHT,
                    axisId = AXIS_DELTA_T,
                    sourceGroup = ChartDataGroup.TEMP_HUM,
                    valueKey = ChartValueKey.DELTA_T,
                    seriesOptions = SeriesOptions(
                        style = SeriesStyle.LINE,
                        lineWidth = 2.4f
                    ),
                    scaleFactor = 10.0
                )
            ),
            axes = listOf(
                ChartAxisConfig(
                    id = AXIS_PRECIPITATION,
                    position = AxisPosition.LEFT,
                    label = "Precipitación",
                    unit = "mm",
                    color = COLOR_PRECIPITATION,
                    min = 0.0,
                    labelCount = 6,
                    formatPattern = "#0.0",
                    scaleFactor = 1.0,
                    forceZeroInRange = true
                ),
                ChartAxisConfig(
                    id = AXIS_HUMIDITY,
                    position = AxisPosition.RIGHT,
                    label = "Humedad relativa",
                    unit = "%",
                    color = COLOR_HUMIDITY,
                    min = 0.0,
                    max = 100.0,
                    labelCount = 5,
                    formatPattern = "#0",
                    scaleFactor = 1.0,
                    forceZeroInRange = true,
                    overlayPriority = 0
                ),
                ChartAxisConfig(
                    id = AXIS_DELTA_T,
                    position = AxisPosition.RIGHT,
                    label = "Delta T",
                    unit = "°C",
                    color = COLOR_DELTA_T,
                    min = -10.0,
                    max = 15.0,
                    labelCount = 6,
                    formatPattern = "#0.0",
                    scaleFactor = 10.0,
                    forceZeroInRange = false,
                    overlayPriority = 1
                )
            )
        )
    }

    private fun createWindChart(): ChartConfig {
        return ChartConfig(
            id = "wind",
            title = "Viento",
            category = ChartCategory.WIND,
            description = "Velocidad, ráfagas y dirección del viento",
            parameters = listOf(
                ChartParameter(
                    key = "vel_viento",
                    label = "Vel. viento (m/s)",
                    unit = "m/s",
                    color = COLOR_WIND_SPEED,
                    axisPosition = AxisPosition.LEFT,
                    axisId = AXIS_WIND_SPEED,
                    sourceGroup = ChartDataGroup.VIENTO,
                    valueKey = ChartValueKey.VELOCIDAD_VIENTO,
                    seriesOptions = SeriesOptions(
                        style = SeriesStyle.LINE,
                        lineWidth = 3f
                    )
                ),
                ChartParameter(
                    key = "rafaga",
                    label = "Ráfaga (m/s)",
                    unit = "m/s",
                    color = COLOR_WIND_GUST,
                    axisPosition = AxisPosition.LEFT,
                    axisId = AXIS_WIND_SPEED,
                    sourceGroup = ChartDataGroup.VIENTO,
                    valueKey = ChartValueKey.RAFAGA_VIENTO,
                    seriesOptions = SeriesOptions(
                        style = SeriesStyle.DASHED_LINE,
                        lineWidth = 2.4f,
                        dashedIntervals = floatArrayOf(12f, 6f)
                    )
                ),
                ChartParameter(
                    key = "dir_viento",
                    label = "Dir. viento (°)",
                    unit = "°",
                    color = COLOR_WIND_DIRECTION,
                    axisPosition = AxisPosition.RIGHT,
                    axisId = AXIS_WIND_DIRECTION,
                    sourceGroup = ChartDataGroup.DIRECCION,
                    valueKey = ChartValueKey.DIRECCION_VIENTO,
                    seriesOptions = SeriesOptions(
                        style = SeriesStyle.LINE,
                        lineWidth = 2.2f
                    )
                )
            ),
            axes = listOf(
                ChartAxisConfig(
                    id = AXIS_WIND_SPEED,
                    position = AxisPosition.LEFT,
                    label = "Velocidad / Ráfaga",
                    unit = "m/s",
                    color = COLOR_WIND_SPEED,
                    min = 0.0,
                    max = 30.0,
                    labelCount = 6,
                    formatPattern = "#0.0",
                    scaleFactor = 1.0,
                    forceZeroInRange = true
                ),
                ChartAxisConfig(
                    id = AXIS_WIND_DIRECTION,
                    position = AxisPosition.RIGHT,
                    label = "Dirección del viento",
                    unit = "°",
                    color = COLOR_WIND_DIRECTION,
                    min = 0.0,
                    max = 360.0,
                    labelCount = 5,
                    formatPattern = "#0",
                    scaleFactor = 1.0,
                    forceZeroInRange = true
                )
            )
        )
    }

    private fun createEvapotranspirationChart(): ChartConfig {
        return ChartConfig(
            id = "evapotranspiration",
            title = "Evapotranspiración",
            category = ChartCategory.EVAPOTRANSPIRATION,
            description = "ET0 acumulada",
            parameters = listOf(
                ChartParameter(
                    key = "et0",
                    label = "ET0 (mm)",
                    unit = "mm",
                    color = COLOR_ET0,
                    axisPosition = AxisPosition.LEFT,
                    axisId = AXIS_ET0,
                    sourceGroup = ChartDataGroup.ET0,
                    valueKey = ChartValueKey.ET0,
                    seriesOptions = SeriesOptions(
                        style = SeriesStyle.LINE,
                        lineWidth = 3f
                    )
                )
            ),
            axes = listOf(
                ChartAxisConfig(
                    id = AXIS_ET0,
                    position = AxisPosition.LEFT,
                    label = "ET0",
                    unit = "mm",
                    color = COLOR_ET0,
                    min = 0.0,
                    labelCount = 6,
                    formatPattern = "#0.00",
                    scaleFactor = 1.0,
                    forceZeroInRange = true
                )
            )
        )
    }

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
                    axisPosition = AxisPosition.LEFT,
                    axisId = AXIS_PRESSURE,
                    sourceGroup = ChartDataGroup.PRESION,
                    valueKey = ChartValueKey.PRESION,
                    seriesOptions = SeriesOptions(
                        style = SeriesStyle.LINE,
                        lineWidth = 3f
                    )
                )
            ),
            axes = listOf(
                ChartAxisConfig(
                    id = AXIS_PRESSURE,
                    position = AxisPosition.LEFT,
                    label = "Presión atmosférica",
                    unit = "hPa",
                    color = COLOR_PRESSURE,
                    min = 940.0,
                    max = 1050.0,
                    labelCount = 6,
                    formatPattern = "#0.0",
                    scaleFactor = 1.0,
                    forceZeroInRange = false
                )
            )
        )
    }
}
