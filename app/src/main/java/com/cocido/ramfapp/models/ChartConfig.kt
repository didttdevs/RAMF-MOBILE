package com.cocido.ramfapp.models

/**
 * Configuración de un gráfico individual
 */
data class ChartConfig(
    val id: String,
    val title: String,
    val parameters: List<ChartParameter>,
    val category: ChartCategory,
    val description: String? = null
)

/**
 * Parámetro individual dentro de un gráfico
 */
data class ChartParameter(
    val key: String,
    val label: String,
    val unit: String,
    val color: Int,
    val axisPosition: AxisPosition = AxisPosition.LEFT,
    val valueExtractor: ((WeatherData) -> Double?)? = null,
    val sourceGroup: ChartDataGroup? = null,
    val valueKey: ChartValueKey? = null,
    val seriesOptions: SeriesOptions = SeriesOptions(),
    val scaleFactor: Double = 1.0
)

/**
 * Categoría de gráfico para filtrado
 */
enum class ChartCategory(val displayName: String) {
    ALL("Todos"),
    TEMPERATURE("Temperatura"),
    HUMIDITY("Humedad"),
    WIND("Viento"),
    PRECIPITATION("Precipitación"),
    PRESSURE("Presión"),
    EVAPOTRANSPIRATION("Evapotranspiración"),
    ENERGY("Energía"),
    COMBINED("Combinados")
}

/**
 * Posición del eje en el gráfico
 */
enum class AxisPosition {
    LEFT,
    RIGHT
}

/**
 * Tipo de gráfico
 */
enum class ChartType {
    SINGLE,      // Un solo parámetro
    COMBINED,    // Múltiples parámetros relacionados
    DUAL_AXIS    // Múltiples parámetros con diferentes escalas
}

/**
 * Grupo de datos entregado por el backend en /charts
 */
enum class ChartDataGroup {
    TEMP_HUM,
    RADIACION,
    ENERGIA,
    LLUVIA,
    VIENTO,
    DIRECCION,
    PRESION,
    ET0
}

/**
 * Clave para extraer valores específicos de un ChartPoint
 */
enum class ChartValueKey(val extractor: (ChartPoint) -> Double?) {
    TEMPERATURA({ it.temperatura }),
    HUMEDAD({ it.humedad }),
    PUNTO_ROCIO({ it.puntoRocio }),
    VPD({ it.vpd }),
    DELTA_T({ it.deltaT }),
    RADIACION_SOLAR({ it.radiacionSolar }),
    VELOCIDAD_VIENTO({ it.velocidadViento }),
    RAFAGA_VIENTO({ it.rafaga }),
    DIRECCION_VIENTO({ it.direccionViento }),
    PRESION({ it.presion }),
    PRECIPITACION({ it.precipitacion }),
    ET0({ it.et0 }),
    BATERIA({ it.bateria }),
    PANEL_SOLAR({ it.panelSolar }),
    HORAS_SOL({ it.horasSol }),
    ORIENTACION({ it.orientacion })
}

/**
 * Opciones visuales para cada serie en un gráfico
 */
data class SeriesOptions(
    val style: SeriesStyle = SeriesStyle.LINE,
    val lineWidth: Float = 2.5f,
    val drawCircles: Boolean = false,
    val drawValues: Boolean = false,
    val fillAlpha: Int = 40,
    val fillColorOverride: Int? = null,
    val dashedIntervals: FloatArray? = null
)

/**
 * Estilos de representación disponibles para una serie
 */
enum class SeriesStyle {
    LINE,
    AREA,
    STEP,
    DASHED_LINE
}

