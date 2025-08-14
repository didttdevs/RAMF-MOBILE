package com.example.rafapp.utils

import android.graphics.Color
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

object ChartUtils {
    
    val PARAMETER_COLORS = mapOf(
        "temperatura" to Color.rgb(255, 87, 34),      // Orange
        "humedad" to Color.rgb(33, 150, 243),         // Blue
        "radiacion" to Color.rgb(255, 193, 7),        // Yellow/Amber
        "precipitacion" to Color.rgb(76, 175, 80),    // Green
        "direccionViento" to Color.rgb(156, 39, 176), // Purple
        "vientoVel" to Color.rgb(96, 125, 139),       // Blue Grey
        "solarDuration" to Color.rgb(255, 152, 0),    // Orange (lighter)
        "dewPoint" to Color.rgb(0, 150, 136),         // Teal
        "airPressure" to Color.rgb(121, 85, 72),      // Brown
        "windGust" to Color.rgb(158, 158, 158)        // Grey
    )
    
    val PARAMETER_UNITS = mapOf(
        "temperatura" to "°C",
        "humedad" to "%",
        "radiacion" to "W/m²",
        "precipitacion" to "mm",
        "direccionViento" to "°",
        "vientoVel" to "m/s",
        "solarDuration" to "h",
        "dewPoint" to "°C",
        "airPressure" to "hPa",
        "windGust" to "m/s"
    )
    
    val PARAMETER_LABELS = mapOf(
        "temperatura" to "Temperatura del aire",
        "humedad" to "Humedad relativa",
        "radiacion" to "Radiación solar",
        "precipitacion" to "Precipitación",
        "direccionViento" to "Dirección del viento",
        "vientoVel" to "Velocidad del viento",
        "solarDuration" to "Duración solar",
        "dewPoint" to "Punto de rocío",
        "airPressure" to "Presión atmosférica",
        "windGust" to "Ráfagas de viento"
    )
    
    fun createLineDataSet(
        entries: List<Entry>,
        parameter: String,
        isMultiParameter: Boolean = false
    ): LineDataSet {
        val color = PARAMETER_COLORS[parameter] ?: Color.rgb(33, 150, 243)
        val label = getParameterLabel(parameter)
        
        return LineDataSet(entries, label).apply {
            // Color configuration
            this.color = color
            lineWidth = if (isMultiParameter) 2f else 2.5f
            
            // Circle configuration
            setDrawCircles(!isMultiParameter)
            setCircleColor(color)
            circleRadius = if (isMultiParameter) 2f else 3f
            setDrawCircleHole(true)
            circleHoleRadius = if (isMultiParameter) 1f else 1.5f
            circleHoleColor = Color.WHITE
            
            // Fill configuration
            setDrawFilled(false)
            
            // Value configuration
            setDrawValues(false)
            valueTextColor = Color.DKGRAY
            valueTextSize = 9f
            
            // Line style
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.15f
            
            // Highlight configuration
            isHighlightEnabled = true
            setDrawHorizontalHighlightIndicator(false)
            setDrawVerticalHighlightIndicator(true)
            highlightLineWidth = 1f
            highLightColor = color
        }
    }
    
    fun getParameterColor(parameter: String): Int {
        return PARAMETER_COLORS[parameter] ?: Color.rgb(33, 150, 243)
    }
    
    fun getParameterUnit(parameter: String): String {
        return PARAMETER_UNITS[parameter] ?: ""
    }
    
    fun getParameterLabel(parameter: String): String {
        val baseLabel = PARAMETER_LABELS[parameter] ?: parameter
        val unit = getParameterUnit(parameter)
        return if (unit.isNotEmpty()) "$baseLabel ($unit)" else baseLabel
    }
    
    fun createTimeFormatter(): ValueFormatter {
        return object : ValueFormatter() {
            private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            
            override fun getFormattedValue(value: Float): String {
                return try {
                    timeFormatter.format(Date(value.toLong()))
                } catch (e: Exception) {
                    ""
                }
            }
        }
    }
    
    fun createDateFormatter(): ValueFormatter {
        return object : ValueFormatter() {
            private val dateFormatter = SimpleDateFormat("dd/MM", Locale.getDefault())
            
            override fun getFormattedValue(value: Float): String {
                return try {
                    dateFormatter.format(Date(value.toLong()))
                } catch (e: Exception) {
                    ""
                }
            }
        }
    }
    
    fun optimizeEntries(entries: List<Entry>, maxEntries: Int = Constants.MAX_CHART_ENTRIES): List<Entry> {
        if (entries.size <= maxEntries) return entries
        
        val step = entries.size / maxEntries
        return entries.filterIndexed { index, _ -> index % step == 0 }
    }
    
    fun validateEntries(entries: List<Entry>): List<Entry> {
        return entries.filter { entry ->
            entry.x.isFinite() && entry.y.isFinite() && !entry.y.isNaN()
        }.sortedBy { it.x }
    }
}