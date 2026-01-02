package com.cocido.ramfapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.cocido.ramfapp.R
import com.cocido.ramfapp.common.Constants
import com.cocido.ramfapp.utils.Constants as UtilConstants
import com.cocido.ramfapp.models.*
import com.cocido.ramfapp.ui.activities.FullScreenChartsActivity
import com.cocido.ramfapp.utils.AuthManager
import com.cocido.ramfapp.ui.components.showErrorMessage
import com.cocido.ramfapp.utils.SecurityLogger
import com.cocido.ramfapp.viewmodels.WeatherStationViewModel
import com.google.android.material.card.MaterialCardView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Professional MainActivity implementing Clean Architecture principles
 * with proper state management, security logging, and error handling
 */
class MainActivity : BaseActivity() {

    private val TAG = "MainActivity"
    private val securityLogger = SecurityLogger()
    private val viewModel: WeatherStationViewModel by viewModels()

    // UI Components
    private lateinit var stationSpinner: MaterialAutoCompleteTextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var tempTextView: TextView
    private lateinit var lastComTextView: TextView
    private lateinit var tempMinTextView: TextView
    private lateinit var tempMaxTextView: TextView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var navHeaderProfileImage: ImageView
    private lateinit var navHeaderName: TextView

    // State Management
    private var weatherStations: List<WeatherStation> = emptyList()
    private var selectedStationPosition = 0
    private var isInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Security logging for app lifecycle
        securityLogger.logAppSecurityEvent("start", this)

        // Initialize authentication and validate user session
        if (!initializeAuthentication()) {
            return // Early exit if authentication fails
        }

        // Setup UI and theme
        setupUI()

        // Initialize components
        initializeComponents()

        // Setup navigation
        setupNavigation()

        // Setup observers for reactive UI updates
        setupObservers()

        // Load initial data
        loadInitialData()

        isInitialized = true
        Log.d(TAG, "MainActivity initialized successfully")
    }

    private fun initializeAuthentication(): Boolean {
        AuthManager.initialize(this)

        return if (!AuthManager.isUserLoggedIn()) {
            Log.d(TAG, "User not authenticated, redirecting to login")
            securityLogger.logAuthenticationEvent("session_expired", false)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            false
        } else {
            Log.d(TAG, "User authenticated successfully")
            securityLogger.logAuthenticationEvent("session_valid", true)
            true
        }
    }

    private fun setupUI() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)
        
        actionBar?.hide()
    }

    private fun initializeComponents() {
        // Initialize UI components
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.nav_view)
        stationSpinner = findViewById(R.id.stationSpinner)
        swipeRefreshLayout = findViewById(R.id.swiperefresh)
        tempTextView = findViewById(R.id.tempTextView)
        lastComTextView = findViewById(R.id.lastComTextView)
        tempMinTextView = findViewById(R.id.tempMinTextView)
        tempMaxTextView = findViewById(R.id.tempMaxTextView)

        // Initialize UI with default values
        initializeUIWithDefaults()

        // Setup weather fragment
        if (supportFragmentManager.findFragmentById(R.id.weatherDataFragment) == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.weatherDataFragment, com.cocido.ramfapp.ui.fragments.WeatherInfoFragment())
                .commit()
        }

        // Setup navigation header
        setupNavigationHeader()

        // Setup refresh listener with debouncing
        setupRefreshListener()
    }

    private fun setupNavigationHeader() {
        val headerView = navigationView.getHeaderView(0)
        navHeaderProfileImage = headerView.findViewById(R.id.navHeaderProfileImage)
        navHeaderName = headerView.findViewById(R.id.navHeaderUsername)

        // Intentar actualizar usuario desde el servidor primero
        lifecycleScope.launch {
            val freshUser = AuthManager.fetchAndUpdateCurrentUser()
            if (freshUser != null) {
                Log.d(TAG, "Fresh user data loaded from /api/auth/me")
                updateNavigationHeaderUI(freshUser)
            } else {
                // Fallback a datos en caché
                Log.d(TAG, "Using cached user data")
                AuthManager.getCurrentUser()?.let { cachedUser ->
                    updateNavigationHeaderUI(cachedUser)
                } ?: run {
                    Log.w(TAG, "No user found in AuthManager")
                    navHeaderName.text = "Usuario"
                }
            }
        }
    }

    private fun updateNavigationHeaderUI(user: User) {
        val fullName = user.getFullName()
        navHeaderName.text = fullName

        Log.d(TAG, "Navigation header set for: $fullName (${user.email})")

        // Load avatar with error handling
        if (!user.avatar.isNullOrBlank()) {
            try {
                Glide.with(this)
                    .load(user.avatar)
                    .circleCrop()
                    .error(R.drawable.ic_weather_sunny) // Fallback image
                    .into(navHeaderProfileImage)
            } catch (e: Exception) {
                Log.w(TAG, "Error loading user avatar", e)
            }
        }

        securityLogger.logUserSecurityEvent("profile_loaded", "navigation_header")
    }

    private fun setupRefreshListener() {
        var lastRefreshTime = 0L

        swipeRefreshLayout.setOnRefreshListener {
            val currentTime = System.currentTimeMillis()

            // Debounce refresh requests (prevent spam)
            if (currentTime - lastRefreshTime > Constants.UI.DEBOUNCE_DELAY) {
                lastRefreshTime = currentTime
                refreshData()
                securityLogger.logUserSecurityEvent("refresh_requested", "main_screen")
                
                // Safety timeout: stop refresh after 15 seconds max
                lifecycleScope.launch {
                    kotlinx.coroutines.delay(15000)
                    if (swipeRefreshLayout.isRefreshing) {
                        Log.w(TAG, "Refresh timeout - forcing stop")
                        swipeRefreshLayout.isRefreshing = false
                    }
                }
            } else {
                swipeRefreshLayout.isRefreshing = false
                Log.d(TAG, "Refresh request debounced")
            }
        }
    }

    private fun setupNavigation() {
        findViewById<ImageButton>(R.id.menuButton).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
            securityLogger.logUserSecurityEvent("menu_opened", "main_screen")
        }

        navigationView.setNavigationItemSelectedListener { item: MenuItem ->
            handleNavigationItemSelected(item)
        }
    }

    private fun handleNavigationItemSelected(item: MenuItem): Boolean {
        val itemName = resources.getResourceEntryName(item.itemId)
        securityLogger.logUserSecurityEvent("navigation_item_selected", "navigation_drawer", additionalInfo = itemName)

        return when (item.itemId) {
            R.id.nav_home -> {
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
            R.id.nav_charts -> {
                openChartsActivity()
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
            R.id.nav_map_view -> {
                openMapActivity()
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
            R.id.nav_profile -> {
                try {
                    val intent = Intent(this, UserProfileActivity::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.w(TAG, "UserProfileActivity not found", e)
                    showFeatureInDevelopment("Profile")
                }
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
            R.id.nav_settings -> {
                showFeatureInDevelopment("Configuración")
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
            R.id.nav_logout -> {
                performLogout()
                true
            }
            else -> false
        }
    }

    private fun showFeatureInDevelopment(featureName: String) {
        val message = when (featureName) {
            "Soil Moisture" -> "Humedad del Suelo - Funcionalidad en desarrollo"
            "Station Overview" -> "Vista General - Los endpoints de la API no están disponibles aún"
            "Device Management" -> "Gestión de Dispositivos - Requiere permisos administrativos"
            "Configuración" -> "Configuración - En desarrollo"
            else -> "$featureName - Funcionalidad limitada por la API"
        }
        showErrorMessage(message)
        Log.d(TAG, "Feature in development accessed: $featureName")
    }

    private fun loadInitialData() {
        Log.d(TAG, "Loading initial data")
        viewModel.fetchWeatherStations()
    }
    
    private fun openChartsActivity() {
        if (weatherStations.isNotEmpty()) {
            val currentStation = weatherStations[selectedStationPosition]
            val intent = Intent(this, FullScreenChartsActivity::class.java).apply {
                putExtra(FullScreenChartsActivity.EXTRA_STATION_ID, currentStation.id)
                putExtra(FullScreenChartsActivity.EXTRA_STATION_NAME, currentStation.name)
                // Abrimos con temperatura por defecto
                putExtra("selected_parameter", "temperatura")
            }
            startActivity(intent)
        }
    }

    private fun openMapActivity() {
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
    }

    private fun setupObservers() {
        Log.d(TAG, "Setting up observers")

        // Observe loading state
        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                updateLoadingState(uiState.isLoading)
                uiState.error?.let { error -> handleError(error) }
            }
        }

        // Observe weather stations
        lifecycleScope.launch {
            viewModel.weatherStations.collect { stationsState ->
                when {
                    stationsState.isLoading -> {
                        Log.d(TAG, "Loading weather stations...")
                    }
                    stationsState.hasError -> {
                        Log.e(TAG, "Error loading stations: ${stationsState.error}")
                        handleStationLoadingError(stationsState.error!!)
                    }
                    stationsState.hasData -> {
                        val stations = stationsState.data!!
                        Log.d(TAG, "Stations loaded: ${stations.size} stations")
                        handleStationsLoaded(stations)
                    }
                }
            }
        }

        // Observe selected station
        lifecycleScope.launch {
            viewModel.selectedStation.collect { station ->
                station?.let { updateSelectedStationUI(it) }
            }
        }

        // Observe widget data
        lifecycleScope.launch {
            viewModel.widgetData.collect { widgetState ->
                when {
                    widgetState.isLoading -> {
                        // Keep refresh indicator spinning while loading
                    }
                    widgetState.hasData -> {
                        val widget = widgetState.data!!
                        updateWeatherDisplay(widget)
                        // Stop refresh indicator when data is loaded
                        swipeRefreshLayout.isRefreshing = false
                        Log.d(TAG, "Widget data updated for temperature: ${widget.getFormattedTemperature()}")
                    }
                    widgetState.hasError -> {
                        Log.e(TAG, "Widget data error: ${widgetState.error}")
                        displayFallbackWeatherData()
                        // Stop refresh indicator on error too
                        swipeRefreshLayout.isRefreshing = false
                    }
                }
            }
        }

        // Observe historical data for fallback display
        lifecycleScope.launch {
            viewModel.historicalData.collect { historicalState ->
                if (historicalState.hasData && shouldUseFallbackData()) {
                    val latestData = historicalState.data!!.firstOrNull()
                    latestData?.let { updateFallbackDisplay(it) }
                }
            }
        }

        // Legacy LiveData support for existing components
        setupLegacyObservers()
    }

    private fun setupLegacyObservers() {
        // Support for existing temperature max/min display
        viewModel.temperatureMaxMin.observe(this) { tempExtremes ->
            tempExtremes?.let {
                tempMinTextView.text = "Min: ${String.format("%.1f", it.min)}°"
                tempMaxTextView.text = "Max: ${String.format("%.1f", it.max)}°"
            }
        }

        // Support for loading state
        viewModel.isLoading.observe(this) { isLoading ->
            updateLoadingState(isLoading)
        }

        // Support for error display
        viewModel.error.observe(this) { error ->
            error?.let { handleError(it) }
        }
    }

    private fun handleStationsLoaded(stations: List<WeatherStation>) {
        weatherStations = stations
        setupStationSpinner(stations)

        // Auto-select Formosa station as default, or first station if Formosa not found
        if (selectedStationPosition == 0 && stations.isNotEmpty()) {
            val formosaStation = stations.find { it.id == UtilConstants.DEFAULT_STATION_ID }
            if (formosaStation != null) {
                val formosaPosition = stations.indexOf(formosaStation)
                selectStation(formosaStation, formosaPosition)
                Log.d(TAG, "Auto-selected default Formosa station: ${formosaStation.name} (ID: ${formosaStation.id})")
            } else {
                val firstStation = stations[0]
                selectStation(firstStation, 0)
                Log.d(TAG, "Formosa station not found, auto-selected first station: ${firstStation.name}")
            }
        }
    }

    private fun selectStation(station: WeatherStation, position: Int) {
        selectedStationPosition = position
        stationSpinner.setText(station.name ?: "Desconocida", false)

        // Update ViewModel with selected station
        viewModel.selectStation(station.id)

        // Show loading indicator
        showLoadingIndicator("Cargando datos de ${station.name}...")

        Log.d(TAG, "Station selected: ${station.name} (ID: ${station.id})")
        securityLogger.logUserSecurityEvent("station_selected", "main_screen", additionalInfo = station.id)
    }

    private fun updateSelectedStationUI(station: WeatherStation) {
        updateMainUI(station)
    }

    private fun updateWeatherDisplay(widget: WidgetData) {
        // Update temperature display
        tempTextView.text = widget.getFormattedTemperature()

        // Update weather condition icon
        val condition = determineSkyConditionFromWidget(widget, isDaytime = true)
        updateBackgroundAndIcon(condition, isDaytime = true)

        // Update timestamp
        updateLastUpdateTime(widget.timestamp)

        logWidgetDataDetails(widget)
    }

    private fun logWidgetDataDetails(widget: WidgetData) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Widget data details:")
            Log.d(TAG, "  Temperature: ${widget.getFormattedTemperature()}")
            Log.d(TAG, "  Humidity: ${widget.getFormattedHumidity()}")
            Log.d(TAG, "  Wind: ${widget.getFormattedWindSpeed()} ${widget.getWindDirectionText()}")
            Log.d(TAG, "  Pressure: ${widget.getFormattedPressure()}")
        }
    }

    private fun shouldUseFallbackData(): Boolean {
        return tempTextView.text.isNullOrBlank() || tempTextView.text == "--°C"
    }

    private fun updateFallbackDisplay(weatherData: WeatherData) {
        weatherData.sensors.hcAirTemperature?.avg?.let { temperature ->
            tempTextView.text = String.format(Locale.getDefault(), "%.1f°C", temperature)
            Log.d(TAG, "Using fallback temperature data: $temperature°C")
        }

        val condition = determineSkyConditionFromSensors(weatherData.sensors, isDaytime = true)
        updateBackgroundAndIcon(condition, isDaytime = true)
    }

    private fun displayFallbackWeatherData() {
        tempTextView.text = "--°C"
        tempMinTextView.text = "Min: --°"
        tempMaxTextView.text = "Max: --°"
        lastComTextView.text = "Datos no disponibles"
    }

    private fun updateLoadingState(isLoading: Boolean) {
        swipeRefreshLayout.isRefreshing = isLoading
    }

    private fun handleError(error: String) {
        if (error.isNotEmpty()) {
            // Show user-friendly error message
            showErrorMessage(error)
            Log.e(TAG, "UI Error: $error")

            // Log security event for error tracking
            securityLogger.logNetworkSecurityEvent("error", 500)
        }
    }

    private fun handleStationLoadingError(error: String) {
        displayFallbackWeatherData()
        showLoadingIndicator("Error cargando estaciones")
    }

    private fun showLoadingIndicator(message: String) {
        lastComTextView.text = message
    }

    private fun initializeUIWithDefaults() {
        // Inicializar UI con valores por defecto claros
        tempTextView.text = "--°C"
        tempMinTextView.text = "Min: --°"
        tempMaxTextView.text = "Max: --°"
        lastComTextView.text = "Última actualización: Cargando..."
        
        Log.d("MainActivity", "UI initialized with default values")
    }

    private fun updateLastUpdateTime(timestamp: String) {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            val date = dateFormat.parse(timestamp)
            val displayFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
            val formattedTime = displayFormat.format(date ?: Date())
            
            // Actualizar el TextView de última actualización si existe
            findViewById<TextView>(R.id.lastComTextView)?.text = "Última actualización: $formattedTime"
        } catch (e: Exception) {
            Log.e("MainActivity", "Error parsing timestamp: $timestamp", e)
        }
    }

    private fun refreshData() {
        Log.d(TAG, "Refreshing data")

        if (!isInitialized) {
            Log.w(TAG, "MainActivity not fully initialized, skipping refresh")
            swipeRefreshLayout.isRefreshing = false
            return
        }

        // Use ViewModel's centralized refresh method
        viewModel.refreshData()

        // If no station selected or stations empty, reload stations
        if (weatherStations.isEmpty()) {
            Log.d(TAG, "No stations available, reloading stations")
            viewModel.fetchWeatherStations()
        }
    }

    // Legacy method for backward compatibility
    private fun fetchWeatherData() {
        refreshData()
    }

    private fun setupStationSpinner(stations: List<WeatherStation>) {
        Log.d(TAG, "Setting up station spinner with ${stations.size} stations")

        val stationNames = stations.map { it.name ?: "Estación Desconocida" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, stationNames)
        stationSpinner.setAdapter(adapter)

        stationSpinner.setOnItemClickListener { _, _, position, _ ->
            if (position >= 0 && position < stations.size) {
                val selectedStation = stations[position]
                selectStation(selectedStation, position)
            } else {
                Log.w(TAG, "Invalid station position selected: $position")
                securityLogger.logSecurityViolation("invalid_station_selection", details = "position=$position,total=${stations.size}")
            }
        }

        securityLogger.logUserSecurityEvent("station_spinner_setup", "main_screen", additionalInfo = "${stations.size}_stations")
    }

    private fun updateMainUI(station: WeatherStation) {
        // Update last communication time
        lastComTextView.text = formatLastCommunication(station.lastCommunication)

        // Ensure temperature display has a default value
        if (tempTextView.text.isNullOrBlank() || tempTextView.text == "null") {
            tempTextView.text = "--°C"
        }

        Log.d(TAG, "Updated main UI for station: ${station.name}")
    }

    private fun formatLastCommunication(isoDate: String?): String {
        if (isoDate == null) return "--/--/----"
        
        return try {
            // Parsear fecha ISO: "2025-08-18T11:31:25.000Z"
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(isoDate)
            
            // Formatear para mostrar: "18/08/25 09:31"
            val outputFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
            outputFormat.timeZone = TimeZone.getDefault() // Hora local
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            // Si no se puede parsear, mostrar la fecha original recortada
            isoDate.take(10) // Solo la parte de fecha
        }
    }

    private fun performLogout() {
        Log.d(TAG, "Performing logout")

        val userId = AuthManager.getCurrentUser()?.id
        securityLogger.logAuthenticationEvent("logout", true, additionalInfo = "user_initiated")

        try {
            AuthManager.logout()

            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()

            Log.d(TAG, "Logout completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout", e)
            securityLogger.logSecurityViolation("logout_error", details = e.message)
            // Force finish activity even if logout fails
            finish()
        }
    }

    // Legacy method for backward compatibility
    private fun logout() {
        performLogout()
    }

    private fun determineSkyConditionFromSensors(sensors: Sensors, isDaytime: Boolean): String {
        val rh = sensors.hcRelativeHumidity?.avg ?: 0.0
        val sr = sensors.solarRadiation?.avg ?: 0.0
        return if (isDaytime) {
            when {
                sr > 500 -> "Despejado"
                sr in 100.0..500.0 -> "Parcialmente Nublado"
                sr <= 100 -> "Nublado"
                else -> "Muy Nublado"
            }
        } else {
            when {
                rh < 80.0 && sr > 1000 -> "Noche Despejada"
                rh >= 80.0 -> "Noche Parcialmente Nublada"
                else -> "Noche Muy Nublada"
            }
        }
    }

    private fun determineSkyConditionFromWidget(widget: WidgetData, isDaytime: Boolean): String {
        val rh = widget.relativeHumidity
        val sr = widget.solarRadiation.toDouble()
        return if (isDaytime) {
            when {
                sr > 500 -> "Despejado"
                sr in 100.0..500.0 -> "Parcialmente Nublado"
                sr <= 100 -> "Nublado"
                else -> "Muy Nublado"
            }
        } else {
            when {
                rh < 80.0 && sr > 1000 -> "Noche Despejada"
                rh >= 80.0 -> "Noche Parcialmente Nublada"
                else -> "Noche Muy Nublada"
            }
        }
    }

    private fun updateBackgroundAndIcon(weatherCondition: String, @Suppress("UNUSED_PARAMETER") isDaytime: Boolean) {
        @Suppress("UNUSED_VARIABLE")
        val cardView = findViewById<MaterialCardView>(R.id.card_temp)
        val weatherIcon = findViewById<ImageView>(R.id.weatherIcon)
        val iconRes = when (weatherCondition) {
            "Despejado" -> R.drawable.ic_weather_sunny
            "Parcialmente Nublado" -> R.drawable.ic_weather_sun_cloud
            "Nublado" -> R.drawable.ic_weather_cloud
            "Muy Nublado" -> R.drawable.ic_weather_very_cloudy
            "Noche Despejada" -> R.drawable.ic_weather_clear_night
            "Noche Parcialmente Nublada" -> R.drawable.ic_weather_cloudy_night
            else -> R.drawable.ic_weather_sunny
        }
        weatherIcon.setImageResource(iconRes)
        // Si querés cambiar fondo del card según condición, podés hacerlo acá con setCardBackgroundColor(...)
    }
}
