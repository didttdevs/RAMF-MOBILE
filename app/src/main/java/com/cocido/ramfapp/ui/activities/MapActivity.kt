package com.cocido.ramfapp.ui.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
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
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MapActivity : BaseActivity(), OnMapReadyCallback {
    
    override fun requiresAuthentication(): Boolean {
        return true
    }

    private lateinit var btnBack: ImageButton
    private lateinit var btnNotifications: ImageButton
    private lateinit var stationInfoCard: MaterialCardView
    private lateinit var temperatureContainer: LinearLayout
    private lateinit var currentConditionsContainer: View
    private lateinit var precipitationContainer: View
    private lateinit var tvStationName: TextView
    private lateinit var tvStationUpdated: TextView
    private lateinit var tvStationStatus: TextView
    private lateinit var tvTempCurrent: TextView
    private lateinit var tvTempMax: TextView
    private lateinit var tvTempMin: TextView
    private lateinit var tvHumidity: TextView
    private lateinit var tvDewPoint: TextView
    private lateinit var tvPressure: TextView
    private lateinit var tvWindSpeed: TextView
    private lateinit var tvWindDirection: TextView
    private lateinit var tvSolarRadiation: TextView
    private lateinit var tvRain1h: TextView
    private lateinit var tvRainToday: TextView
    private lateinit var tvRain24h: TextView
    private lateinit var tvRain48h: TextView
    private lateinit var tvRain7d: TextView
    private lateinit var btnCloseStationInfo: ImageButton
    
    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private val viewModel: WeatherStationViewModel by viewModels()
    
    private var weatherStations: List<WeatherStation> = listOf()
    private val stationMarkers = mutableMapOf<String, Marker>()
    private val stationWidgetData = mutableMapOf<String, WidgetData>()
    private val weatherStationMap = mutableMapOf<String, WeatherStation>()
    private var selectedStationId: String? = null
    private var highlightedStationId: String? = null
    
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val TAG = "MapActivity"
        
        // Coordenadas por defecto (Argentina)
        private val FORMOSA_CENTER = LatLng(-24.6000, -60.1000) // Centro aproximado provincia
        private const val FIXED_ZOOM_FORMOSA = 6.8f
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
        stationInfoCard = findViewById(R.id.stationInfoCard)
        temperatureContainer = findViewById(R.id.temperatureContainer)
        currentConditionsContainer = findViewById(R.id.currentConditionsContainer)
        precipitationContainer = findViewById(R.id.precipitationContainer)
        tvStationName = findViewById(R.id.tvStationName)
        tvStationUpdated = findViewById(R.id.tvStationUpdated)
        tvStationStatus = findViewById(R.id.tvStationStatus)
        tvTempCurrent = findViewById(R.id.tvTempCurrent)
        tvTempMax = findViewById(R.id.tvTempMax)
        tvTempMin = findViewById(R.id.tvTempMin)
        tvHumidity = findViewById(R.id.tvHumidity)
        tvDewPoint = findViewById(R.id.tvDewPoint)
        tvPressure = findViewById(R.id.tvPressure)
        tvWindSpeed = findViewById(R.id.tvWindSpeed)
        tvWindDirection = findViewById(R.id.tvWindDirection)
        tvSolarRadiation = findViewById(R.id.tvSolarRadiation)
        tvRain1h = findViewById(R.id.tvRain1h)
        tvRainToday = findViewById(R.id.tvRainToday)
        tvRain24h = findViewById(R.id.tvRain24h)
        tvRain48h = findViewById(R.id.tvRain48h)
        tvRain7d = findViewById(R.id.tvRain7d)
        btnCloseStationInfo = findViewById(R.id.btnCloseStationInfo)
        stationInfoCard.visibility = View.GONE
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnNotifications.setOnClickListener {
            Toast.makeText(this, "Notificaciones - En desarrollo", Toast.LENGTH_SHORT).show()
        }

        btnCloseStationInfo.setOnClickListener {
            hideStationInfoCard()
        }
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.weatherStations.collect { stationsState ->
                if (stationsState.hasData) {
                    weatherStations = stationsState.data!!
                    weatherStationMap.clear()
                    weatherStations.forEach { station ->
                        weatherStationMap[station.id] = station
                    }
                    addMarkersToMap()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.widgetData.collect { widgetState ->
                when {
                    widgetState.isLoading -> {
                        if (selectedStationId != null) {
                            showStationLoading()
                        }
                    }
                    widgetState.hasData -> {
                        val data = widgetState.data!!
                        val stationId = findStationIdForWidgetData(data)
                        if (stationId != null) {
                            stationWidgetData[stationId] = data
                            updateMarkerForStation(stationId)
                            if (selectedStationId == stationId) {
                                weatherStationMap[stationId]?.let { station ->
                                    showStationData(station, data)
                                }
                            }
                        }
                    }
                    widgetState.error != null -> {
                        if (selectedStationId != null) {
                            showStationError(widgetState.error)
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                uiState.error?.let { errorMessage ->
                    Toast.makeText(this@MapActivity, errorMessage, Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Error: $errorMessage")
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        
        // Configurar el mapa
        mMap.uiSettings.apply {
            isZoomControlsEnabled = false
            isCompassEnabled = false
            isMyLocationButtonEnabled = false
            isScrollGesturesEnabled = false
            isZoomGesturesEnabled = false
            isTiltGesturesEnabled = false
            isRotateGesturesEnabled = false
        }
        
        // Solicitar permisos de ubicación
        if (checkLocationPermission()) {
            enableMyLocation()
        } else {
            requestLocationPermission()
        }
        
        // Configurar la cámara inicial
        mMap.setOnMapLoadedCallback {
            mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    FORMOSA_CENTER,
                    FIXED_ZOOM_FORMOSA
                )
            )
        }
        
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
    
    private fun findStationIdForWidgetData(widgetData: WidgetData): String? {
        widgetData.stationName?.let { widgetName ->
            val normalizedWidgetName = widgetName.trim().lowercase(Locale.getDefault())
            val matchByName = weatherStations.firstOrNull { station ->
                station.name?.trim()?.lowercase(Locale.getDefault()) == normalizedWidgetName
            }
            if (matchByName != null) {
                return matchByName.id
            }
        }
        return selectedStationId ?: weatherStations.firstOrNull()?.id
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
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                
                val marker = mMap.addMarker(markerOptions)
                marker?.let {
                    it.tag = station.id
                    stationMarkers[station.id] = it
                }
            }
        }

        highlightedStationId?.let { selectedId ->
            stationMarkers[selectedId]?.setIcon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)
            )
        }
    }
    
    private fun getMarkerSnippet(stationId: String): String {
        val widgetData = stationWidgetData[stationId]
        if (widgetData != null) {
            val temperature = widgetData.getFormattedTemperature()
            val humidity = widgetData.getFormattedHumidity()
            return "$temperature · Humedad $humidity"
        }
        return "Toca para ver detalles"
    }
    
    private fun updateMarkerForStation(stationId: String) {
        val marker = stationMarkers[stationId]
        marker?.snippet = getMarkerSnippet(stationId)
    }

    private fun updateMarkerSelection(newSelectedId: String?) {
        if (highlightedStationId == newSelectedId) return

        highlightedStationId?.let { previousId ->
            stationMarkers[previousId]?.setIcon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            )
        }

        highlightedStationId = newSelectedId

        highlightedStationId?.let { currentId ->
            stationMarkers[currentId]?.setIcon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)
            )
        }
    }

    private fun showStationBaseInfo(station: WeatherStation) {
        stationInfoCard.visibility = View.VISIBLE
        tvStationName.text = station.name ?: station.id
        tvStationUpdated.text = "Actualizado: --"
        showStationLoading()
    }

    private fun showStationLoading() {
        stationInfoCard.visibility = View.VISIBLE
        tvStationStatus.visibility = View.VISIBLE
        tvStationStatus.text = "Cargando datos..."
        temperatureContainer.visibility = View.GONE
        currentConditionsContainer.visibility = View.GONE
        precipitationContainer.visibility = View.GONE
    }

    private fun showStationData(station: WeatherStation, widgetData: WidgetData) {
        stationInfoCard.visibility = View.VISIBLE
        tvStationName.text = station.name ?: widgetData.stationName ?: station.id
        tvStationUpdated.text = "Actualizado: ${formatTimestamp(widgetData.lastUpdate ?: widgetData.timestamp)}"
        tvStationStatus.visibility = View.GONE
        temperatureContainer.visibility = View.VISIBLE
        currentConditionsContainer.visibility = View.VISIBLE
        precipitationContainer.visibility = View.VISIBLE

        tvTempCurrent.text = widgetData.getFormattedTemperature()
        tvTempMax.text = widgetData.getFormattedMaxTemperature()
        tvTempMin.text = widgetData.getFormattedMinTemperature()
        tvHumidity.text = widgetData.getFormattedHumidity()
        tvDewPoint.text = widgetData.getFormattedDewPoint()
        tvPressure.text = widgetData.getFormattedPressure()
        tvWindSpeed.text = widgetData.getFormattedWindSpeed()
        tvWindDirection.text = widgetData.getWindDirectionText()
        tvSolarRadiation.text = widgetData.getFormattedSolarRadiation()
        tvRain1h.text = widgetData.getFormattedRainLastHour()
        tvRainToday.text = widgetData.getFormattedRainToday()
        tvRain24h.text = widgetData.getFormattedRain24h()
        tvRain48h.text = widgetData.getFormattedRain48h()
        tvRain7d.text = widgetData.getFormattedRain7d()
    }

    private fun showStationError(message: String?) {
        stationInfoCard.visibility = View.VISIBLE
        tvStationStatus.visibility = View.VISIBLE
        tvStationStatus.text = message ?: "No se pudieron cargar los datos"
        temperatureContainer.visibility = View.GONE
        currentConditionsContainer.visibility = View.GONE
        precipitationContainer.visibility = View.GONE
        tvStationUpdated.text = "Actualizado: --"
    }

    private fun hideStationInfoCard() {
        stationInfoCard.visibility = View.GONE
        selectedStationId = null
        updateMarkerSelection(null)
    }

    private fun formatTimestamp(rawTimestamp: String?): String {
        if (rawTimestamp.isNullOrBlank()) {
            return "Sin datos"
        }
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = parser.parse(rawTimestamp)
            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            formatter.timeZone = TimeZone.getDefault()
            date?.let { formatter.format(it) } ?: "Sin datos"
        } catch (ex: Exception) {
            Log.e(TAG, "Error al formatear fecha: ${ex.message}")
            "Sin datos"
        }
    }
    
    private fun showStationInfo(marker: Marker) {
        val stationEntry = stationMarkers.entries.firstOrNull { it.value == marker }
        val stationId = stationEntry?.key
        if (stationId != null) {
            val station = weatherStationMap[stationId] ?: weatherStations.firstOrNull { it.id == stationId }
            if (station != null) {
                selectedStationId = stationId
                updateMarkerSelection(stationId)
                showStationBaseInfo(station)
                stationWidgetData[stationId]?.let { cachedData ->
                    showStationData(station, cachedData)
                }
                viewModel.fetchWidgetData(stationId)
            } else {
                Toast.makeText(this, "Estación no encontrada", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Estación no encontrada", Toast.LENGTH_SHORT).show()
        }
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