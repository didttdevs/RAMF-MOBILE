package com.cocido.ramfapp.ui.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cocido.ramfapp.R
import com.cocido.ramfapp.models.WeatherStation
import com.cocido.ramfapp.models.WidgetData
import com.cocido.ramfapp.viewmodels.WeatherStationViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var btnBack: ImageButton
    private lateinit var btnNotifications: ImageButton
    private lateinit var filterContainer: LinearLayout
    private lateinit var tvSelectedParameter: TextView
    
    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private val viewModel: WeatherStationViewModel by viewModels()
    
    private var weatherStations: List<WeatherStation> = listOf()
    private var selectedParameter = "temperatura"
    private val stationMarkers = mutableMapOf<String, Marker>()
    private val stationWidgetData = mutableMapOf<String, WidgetData>()
    
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val TAG = "MapActivity"
        
        // Coordenadas por defecto (Argentina)
        private val DEFAULT_LOCATION = LatLng(-34.6118, -58.3960) // Buenos Aires
        private const val DEFAULT_ZOOM = 6f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        initViews()
        setupListeners()
        setupObservers()
        
        // Inicializar el mapa
        mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        
        // Cargar datos
        loadWeatherStations()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnNotifications = findViewById(R.id.btnNotifications)
        filterContainer = findViewById(R.id.filterContainer)
        tvSelectedParameter = findViewById(R.id.tvSelectedParameter)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnNotifications.setOnClickListener {
            Toast.makeText(this, "Notificaciones - En desarrollo", Toast.LENGTH_SHORT).show()
        }

        filterContainer.setOnClickListener {
            showParameterSelectionDialog()
        }
    }
    
    private fun setupObservers() {
        viewModel.weatherStations.observe(this) { stations ->
            if (stations.isNotEmpty()) {
                weatherStations = stations
                addMarkersToMap()
                loadWidgetDataForStations()
            }
        }
        
        viewModel.error.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            Log.e(TAG, "Error: $errorMessage")
        }
    }

    private fun showParameterSelectionDialog() {
        val parameters = arrayOf(
            "Temperatura del aire",
            "Humedad relativa", 
            "Velocidad del viento",
            "Precipitación",
            "Radiación solar",
            "Humedad del suelo"
        )
        
        val parameterKeys = arrayOf(
            "temperatura",
            "humedad",
            "viento",
            "precipitacion",
            "radiacion",
            "humedad_suelo"
        )

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Seleccionar Parámetro")
        builder.setItems(parameters) { dialog, which ->
            tvSelectedParameter.text = parameters[which]
            selectedParameter = parameterKeys[which]
            updateMarkersWithParameter(selectedParameter)
            dialog.dismiss()
        }
        builder.show()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        
        // Configurar el mapa
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        
        // Solicitar permisos de ubicación
        if (checkLocationPermission()) {
            enableMyLocation()
        } else {
            requestLocationPermission()
        }
        
        // Configurar la cámara inicial
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM))
        
        // Configurar listener para marcadores
        mMap.setOnMarkerClickListener { marker ->
            showStationInfo(marker)
            true
        }
        
        // Si ya tenemos estaciones, agregar marcadores
        if (weatherStations.isNotEmpty()) {
            addMarkersToMap()
        }
    }
    
    private fun loadWeatherStations() {
        viewModel.fetchWeatherStations()
    }
    
    private fun loadWidgetDataForStations() {
        weatherStations.forEach { station ->
            viewModel.fetchWidgetData(station.id)
        }
        
        // Observar los datos del widget para cada estación
        viewModel.widgetData.observe(this) { widgetData ->
            widgetData?.let { data ->
                // Encontrar a qué estación pertenecen estos datos
                val stationId = findStationIdForWidgetData(data)
                if (stationId != null) {
                    stationWidgetData[stationId] = data
                    updateMarkerForStation(stationId)
                }
            }
        }
    }
    
    private fun findStationIdForWidgetData(widgetData: WidgetData): String? {
        // En este caso, como el ViewModel maneja una estación a la vez,
        // necesitamos asociar los datos con la estación correcta
        // Por simplicidad, asumimos que los datos corresponden a la última estación consultada
        return weatherStations.firstOrNull()?.id
    }
    
    private fun addMarkersToMap() {
        if (!::mMap.isInitialized) return
        
        // Limpiar marcadores existentes
        stationMarkers.values.forEach { it.remove() }
        stationMarkers.clear()
        
        weatherStations.forEach { station ->
            val coordinates = station.position?.coordinates
            if (coordinates != null && coordinates.size >= 2) {
                val latLng = LatLng(coordinates[1], coordinates[0]) // [lng, lat] -> LatLng(lat, lng)
                
                val markerOptions = MarkerOptions()
                    .position(latLng)
                    .title(station.name ?: "Estación Desconocida")
                    .snippet(getMarkerSnippet(station.id))
                
                // Personalizar el ícono según el parámetro seleccionado
                setMarkerIcon(markerOptions, station.id)
                
                val marker = mMap.addMarker(markerOptions)
                marker?.let {
                    stationMarkers[station.id] = it
                }
            }
        }
    }
    
    private fun getMarkerSnippet(stationId: String): String {
        val widgetData = stationWidgetData[stationId]
        if (widgetData != null) {
            return when (selectedParameter) {
                "temperatura" -> "${String.format("%.1f", widgetData.temperature)}°C"
                "humedad" -> "${String.format("%.1f", widgetData.relativeHumidity)}%"
                "viento" -> "${String.format("%.1f", widgetData.windSpeed)} m/s"
                "precipitacion" -> "${String.format("%.1f", widgetData.rainLastHour)} mm"
                "radiacion" -> "${String.format("%.0f", widgetData.solarRadiation)} W/m²"
                else -> "Datos no disponibles"
            }
        }
        return "Cargando datos..."
    }
    
    private fun setMarkerIcon(markerOptions: MarkerOptions, stationId: String) {
        val widgetData = stationWidgetData[stationId]
        if (widgetData != null) {
            when (selectedParameter) {
                "temperatura" -> {
                    val temp = widgetData.temperature
                    val hue = when {
                        temp < 0 -> BitmapDescriptorFactory.HUE_CYAN
                        temp < 15 -> BitmapDescriptorFactory.HUE_BLUE
                        temp < 25 -> BitmapDescriptorFactory.HUE_GREEN
                        temp < 35 -> BitmapDescriptorFactory.HUE_YELLOW
                        else -> BitmapDescriptorFactory.HUE_RED
                    }
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(hue))
                }
                "humedad" -> {
                    val humidity = widgetData.relativeHumidity
                    val hue = when {
                        humidity < 30 -> BitmapDescriptorFactory.HUE_RED
                        humidity < 60 -> BitmapDescriptorFactory.HUE_YELLOW
                        humidity < 80 -> BitmapDescriptorFactory.HUE_GREEN
                        else -> BitmapDescriptorFactory.HUE_BLUE
                    }
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(hue))
                }
                else -> markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            }
        } else {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        }
    }
    
    private fun updateMarkersWithParameter(parameter: String) {
        stationMarkers.forEach { (stationId, marker) ->
            marker.snippet = getMarkerSnippet(stationId)
            
            // Recrear el marcador con el nuevo ícono
            val position = marker.position
            val title = marker.title
            marker.remove()
            
            val markerOptions = MarkerOptions()
                .position(position)
                .title(title)
                .snippet(getMarkerSnippet(stationId))
            
            setMarkerIcon(markerOptions, stationId)
            
            val newMarker = mMap.addMarker(markerOptions)
            newMarker?.let {
                stationMarkers[stationId] = it
            }
        }
    }
    
    private fun updateMarkerForStation(stationId: String) {
        val marker = stationMarkers[stationId]
        if (marker != null) {
            marker.snippet = getMarkerSnippet(stationId)
        }
    }
    
    private fun showStationInfo(marker: Marker) {
        val stationName = marker.title ?: "Estación Desconocida"
        val stationData = marker.snippet ?: "Sin datos"
        
        Toast.makeText(
            this,
            "$stationName\n$stationData",
            Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }
    
    private fun enableMyLocation() {
        if (checkLocationPermission()) {
            try {
                mMap.isMyLocationEnabled = true
            } catch (e: SecurityException) {
                Log.e(TAG, "Error enabling location: ${e.message}")
            }
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && 
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation()
                } else {
                    Toast.makeText(
                        this,
                        "Permiso de ubicación denegado",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}