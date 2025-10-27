package com.cocido.ramfapp.utils

import android.graphics.Point
import kotlin.math.*

/**
 * Data class para representar los bounds de un mapa
 */
data class MapBounds(
    val north: Double,
    val south: Double,
    val east: Double,
    val west: Double
)

/**
 * Utilidad para convertir coordenadas geográficas (lat/lng) a píxeles en el mapa estático de Formosa
 * 
 * Considera los bounds de Formosa:
 * - Norte: -22.0°
 * - Sur: -26.5°
 * - Este: -57.5°
 * - Oeste: -61.5°
 */
object MapCoordinateConverter {
    
    // Bounds de Formosa
    private const val FORMOSA_NORTH = -22.0
    private const val FORMOSA_SOUTH = -26.5
    private const val FORMOSA_EAST = -57.5
    private const val FORMOSA_WEST = -61.5
    
    // Dimensiones del mapa estático (en píxeles)
    private const val MAP_WIDTH = 1920
    private const val MAP_HEIGHT = 1080
    
    /**
     * Convierte coordenadas lat/lng a píxeles en el mapa estático
     * 
     * @param latitude Latitud (-22.0 a -26.5)
     * @param longitude Longitud (-61.5 a -57.5)
     * @return Point con coordenadas en píxeles (x, y)
     */
    fun latLngToPixel(latitude: Double, longitude: Double): Point {
        // Validar que las coordenadas estén dentro de Formosa
        if (!isWithinFormosaBounds(latitude, longitude)) {
            throw IllegalArgumentException("Coordenadas fuera de los límites de Formosa")
        }
        
        // Calcular posición relativa (0.0 a 1.0)
        val relativeX = (longitude - FORMOSA_WEST) / (FORMOSA_EAST - FORMOSA_WEST)
        val relativeY = (latitude - FORMOSA_NORTH) / (FORMOSA_SOUTH - FORMOSA_NORTH)
        
        // Convertir a píxeles
        val pixelX = (relativeX * MAP_WIDTH).toInt()
        val pixelY = (relativeY * MAP_HEIGHT).toInt()
        
        // Asegurar que estén dentro de los límites del mapa
        val clampedX = pixelX.coerceIn(0, MAP_WIDTH - 1)
        val clampedY = pixelY.coerceIn(0, MAP_HEIGHT - 1)
        
        return Point(clampedX, clampedY)
    }
    
    /**
     * Convierte píxeles del mapa a coordenadas lat/lng
     * 
     * @param x Coordenada X en píxeles
     * @param y Coordenada Y en píxeles
     * @return Pair<Double, Double> con latitud y longitud
     */
    fun pixelToLatLng(x: Int, y: Int): Pair<Double, Double> {
        val clampedX = x.coerceIn(0, MAP_WIDTH - 1)
        val clampedY = y.coerceIn(0, MAP_HEIGHT - 1)
        
        val relativeX = clampedX.toDouble() / MAP_WIDTH
        val relativeY = clampedY.toDouble() / MAP_HEIGHT
        
        val longitude = FORMOSA_WEST + (relativeX * (FORMOSA_EAST - FORMOSA_WEST))
        val latitude = FORMOSA_NORTH + (relativeY * (FORMOSA_SOUTH - FORMOSA_NORTH))
        
        return Pair(latitude, longitude)
    }
    
    /**
     * Verifica si las coordenadas están dentro de los límites de Formosa
     */
    fun isWithinFormosaBounds(latitude: Double, longitude: Double): Boolean {
        return latitude in FORMOSA_SOUTH..FORMOSA_NORTH && 
               longitude in FORMOSA_WEST..FORMOSA_EAST
    }
    
    /**
     * Calcula la distancia entre dos puntos en el mapa (en píxeles)
     */
    fun calculateDistanceInPixels(point1: Point, point2: Point): Double {
        val dx = point1.x - point2.x
        val dy = point1.y - point2.y
        return sqrt((dx * dx + dy * dy).toDouble())
    }
    
    /**
     * Calcula la distancia real entre dos coordenadas geográficas (en km)
     * Usa la fórmula de Haversine
     */
    fun calculateRealDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadius = 6371.0 // Radio de la Tierra en km
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return earthRadius * c
    }
    
    /**
     * Obtiene el centro de Formosa en coordenadas
     */
    fun getFormosaCenter(): Pair<Double, Double> {
        val centerLat = (FORMOSA_NORTH + FORMOSA_SOUTH) / 2
        val centerLng = (FORMOSA_EAST + FORMOSA_WEST) / 2
        return Pair(centerLat, centerLng)
    }
    
    /**
     * Obtiene el centro de Formosa en píxeles
     */
    fun getFormosaCenterInPixels(): Point {
        val center = getFormosaCenter()
        return latLngToPixel(center.first, center.second)
    }
    
    /**
     * Calcula el zoom level apropiado para mostrar toda Formosa
     */
    fun getOptimalZoomLevel(): Float {
        // Para un mapa de 1920x1080, zoom level 7-8 es apropiado
        return 7.5f
    }
    
    /**
     * Obtiene los bounds de Formosa como string para debugging
     */
    fun getFormosaBoundsString(): String {
        return "Norte: $FORMOSA_NORTH°, Sur: $FORMOSA_SOUTH°, Este: $FORMOSA_EAST°, Oeste: $FORMOSA_WEST°"
    }
}

