package com.cocido.ramfapp.ui.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.cocido.ramfapp.R
import com.cocido.ramfapp.models.WeatherStation
import com.cocido.ramfapp.models.WidgetData
import kotlin.math.sqrt

/**
 * Custom View para mostrar un mapa interactivo con pines clickeables
 * Similar a la funcionalidad de la página web
 */
class InteractiveMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Datos del mapa
    private var weatherStations: List<WeatherStation> = emptyList()
    private var stationWidgetData: Map<String, WidgetData> = emptyMap()
    private var selectedParameter: String = "temperatura"
    
    // Paint objects
    private val pinPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectedPinPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    // Bitmap del mapa
    private var mapBitmap: Bitmap? = null
    private var mapRect: RectF? = null
    
    // Pines
    private val pins = mutableListOf<MapPin>()
    private var selectedPin: MapPin? = null
    
    // Callbacks
    private var onPinClickListener: ((WeatherStation, WidgetData?) -> Unit)? = null
    
    // Colores
    private val pinColors = mapOf(
        "temperatura" to Color.parseColor("#FF5722"),
        "humedad" to Color.parseColor("#2196F3"),
        "precipitacion" to Color.parseColor("#4CAF50"),
        "viento" to Color.parseColor("#FF9800"),
        "radiacion" to Color.parseColor("#9C27B0"),
        "energia" to Color.parseColor("#607D8B")
    )
    
    init {
        setupPaints()
    }
    
    private fun setupPaints() {
        // Pin paint
        pinPaint.style = Paint.Style.FILL
        pinPaint.color = Color.parseColor("#FF5722")
        
        // Selected pin paint
        selectedPinPaint.style = Paint.Style.FILL
        selectedPinPaint.color = Color.parseColor("#FFC107")
        
        // Text paint
        textPaint.color = Color.WHITE
        textPaint.textSize = 24f
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.DEFAULT_BOLD
        
        // Background paint
        backgroundPaint.color = Color.parseColor("#F5F5F5")
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setupMapBounds()
    }
    
    private fun setupMapBounds() {
        // Configurar los límites del mapa basado en las coordenadas reales de Formosa
        // Formosa: Lat -22.0 a -26.5, Lon -57.0 a -62.5
        mapRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Dibujar fondo
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        
        // Dibujar mapa si está disponible
        mapBitmap?.let { bitmap ->
            val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
            val dstRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
            canvas.drawBitmap(bitmap, srcRect, dstRect, null)
        }
        
        // Dibujar pines
        pins.forEach { pin ->
            drawPin(canvas, pin)
        }
    }
    
    private fun drawPin(canvas: Canvas, pin: MapPin) {
        val isSelected = pin == selectedPin
        val paint = if (isSelected) selectedPinPaint else pinPaint
        
        // Color del pin basado en el parámetro seleccionado
        val color = getPinColor(pin.station, selectedParameter)
        paint.color = color
        
        // Dibujar círculo del pin
        canvas.drawCircle(pin.x, pin.y, pin.radius, paint)
        
        // Dibujar borde del pin
        val borderPaint = Paint(paint)
        borderPaint.style = Paint.Style.STROKE
        borderPaint.color = Color.WHITE
        borderPaint.strokeWidth = 3f
        canvas.drawCircle(pin.x, pin.y, pin.radius, borderPaint)
        
        // Dibujar valor del parámetro
        val value = getPinValue(pin.station, selectedParameter)
        canvas.drawText(value, pin.x, pin.y + 8f, textPaint)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val clickedPin = findPinAt(event.x, event.y)
                if (clickedPin != null) {
                    selectedPin = clickedPin
                    invalidate()
                    
                    val widgetData = stationWidgetData[clickedPin.station.id]
                    onPinClickListener?.invoke(clickedPin.station, widgetData)
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }
    
    private fun findPinAt(x: Float, y: Float): MapPin? {
        return pins.find { pin ->
            val distance = sqrt((x - pin.x) * (x - pin.x) + (y - pin.y) * (y - pin.y))
            distance <= pin.radius + 10f // Margen de tolerancia
        }
    }
    
    private fun getPinColor(station: WeatherStation, parameter: String): Int {
        val widgetData = stationWidgetData[station.id] ?: return Color.GRAY
        
        return when (parameter) {
            "temperatura" -> getTemperatureColor(widgetData.temperature)
            "humedad" -> getHumidityColor(widgetData.relativeHumidity)
            "precipitacion" -> getPrecipitationColor(widgetData.rainLastHour)
            "viento" -> getWindColor(widgetData.windSpeed)
            "radiacion" -> getRadiationColor(widgetData.solarRadiation)
            "energia" -> getEnergyColor(0.0)
            else -> pinColors[parameter] ?: Color.BLUE
        }
    }
    
    private fun getPinValue(station: WeatherStation, parameter: String): String {
        val widgetData = stationWidgetData[station.id] ?: return "N/A"
        
        return when (parameter) {
            "temperatura" -> "${String.format("%.1f", widgetData.temperature)}°"
            "humedad" -> "${String.format("%.1f", widgetData.relativeHumidity)}%"
            "precipitacion" -> "${String.format("%.1f", widgetData.rainLastHour)}mm"
            "viento" -> "${String.format("%.1f", widgetData.windSpeed)}km/h"
            "radiacion" -> "${String.format("%.0f", widgetData.solarRadiation)}W"
            "energia" -> "N/A"
            else -> "N/A"
        }
    }
    
    // Funciones de color basadas en valores
    private fun getTemperatureColor(temperature: Double): Int {
        return when {
            temperature < 0 -> Color.parseColor("#2196F3") // Azul
            temperature < 10 -> Color.parseColor("#00BCD4") // Cian
            temperature < 20 -> Color.parseColor("#4CAF50") // Verde
            temperature < 30 -> Color.parseColor("#FFEB3B") // Amarillo
            else -> Color.parseColor("#F44336") // Rojo
        }
    }
    
    private fun getHumidityColor(humidity: Double): Int {
        return when {
            humidity < 30 -> Color.parseColor("#F44336") // Rojo
            humidity < 50 -> Color.parseColor("#FF9800") // Naranja
            humidity < 70 -> Color.parseColor("#4CAF50") // Verde
            else -> Color.parseColor("#2196F3") // Azul
        }
    }
    
    private fun getPrecipitationColor(precipitation: Double): Int {
        return when {
            precipitation == 0.0 -> Color.parseColor("#9E9E9E") // Gris
            precipitation < 5 -> Color.parseColor("#FFEB3B") // Amarillo
            precipitation < 20 -> Color.parseColor("#4CAF50") // Verde
            else -> Color.parseColor("#2196F3") // Azul
        }
    }
    
    private fun getWindColor(windSpeed: Double): Int {
        return when {
            windSpeed < 10 -> Color.parseColor("#4CAF50") // Verde
            windSpeed < 20 -> Color.parseColor("#FFEB3B") // Amarillo
            windSpeed < 30 -> Color.parseColor("#FF9800") // Naranja
            else -> Color.parseColor("#F44336") // Rojo
        }
    }
    
    private fun getRadiationColor(radiation: Double): Int {
        return when {
            radiation < 100 -> Color.parseColor("#9E9E9E") // Gris
            radiation < 300 -> Color.parseColor("#FFEB3B") // Amarillo
            radiation < 600 -> Color.parseColor("#FF9800") // Naranja
            else -> Color.parseColor("#F44336") // Rojo
        }
    }
    
    private fun getEnergyColor(energy: Double): Int {
        return when {
            energy < 50 -> Color.parseColor("#F44336") // Rojo
            energy < 80 -> Color.parseColor("#FF9800") // Naranja
            else -> Color.parseColor("#4CAF50") // Verde
        }
    }
    
    // API pública
    fun setMapBitmap(bitmap: Bitmap) {
        mapBitmap = bitmap
        invalidate()
    }
    
    fun setWeatherStations(stations: List<WeatherStation>) {
        weatherStations = stations
        createPins()
        invalidate()
    }
    
    fun setStationWidgetData(data: Map<String, WidgetData>) {
        stationWidgetData = data
        invalidate()
    }
    
    fun setSelectedParameter(parameter: String) {
        selectedParameter = parameter
        invalidate()
    }
    
    fun setOnPinClickListener(listener: (WeatherStation, WidgetData?) -> Unit) {
        onPinClickListener = listener
    }
    
    private fun createPins() {
        pins.clear()
        
        weatherStations.forEach { station ->
            val coordinates = station.position?.coordinates
            if (coordinates != null && coordinates.size >= 2) {
                val longitude = coordinates[0]
                val latitude = coordinates[1]
                
                // Convertir coordenadas a píxeles en el mapa
                val pixelPoint = convertLatLngToPixel(latitude, longitude)
                
                pins.add(MapPin(
                    station = station,
                    x = pixelPoint.x,
                    y = pixelPoint.y,
                    radius = 20f
                ))
            }
        }
    }
    
    private fun convertLatLngToPixel(latitude: Double, longitude: Double): PointF {
        // Límites geográficos de Formosa
        val minLat = -26.5
        val maxLat = -22.0
        val minLng = -62.5
        val maxLng = -57.0
        
        // Normalizar coordenadas (0-1)
        val normalizedLat = (latitude - minLat) / (maxLat - minLat)
        val normalizedLng = (longitude - minLng) / (maxLng - minLng)
        
        // Convertir a píxeles
        val x = normalizedLng * width
        val y = (1 - normalizedLat) * height // Invertir Y porque el origen está arriba
        
        return PointF(x.toFloat(), y.toFloat())
    }
    
    data class MapPin(
        val station: WeatherStation,
        val x: Float,
        val y: Float,
        val radius: Float
    )
}
