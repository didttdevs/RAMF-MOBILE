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
    val valueExtractor: (WeatherData) -> Double?
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

