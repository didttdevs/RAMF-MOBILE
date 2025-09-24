package com.cocido.ramfapp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import androidx.lifecycle.LiveData
import com.cocido.ramfapp.common.Resource
import com.cocido.ramfapp.common.UiState
import com.cocido.ramfapp.models.*
import com.cocido.ramfapp.repository.WeatherRepository
import com.cocido.ramfapp.utils.SecurityLogger
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Professional WeatherStationViewModel implementing modern Android architecture
 * with StateFlow, proper error handling, and security logging
 */
class WeatherStationViewModel : ViewModel() {

    private val TAG = "WeatherStationViewModel"
    private val repository = WeatherRepository()
    private val securityLogger = SecurityLogger()

    // ============ UI STATE MANAGEMENT ============

    // Main UI state combining all weather station data
    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    // Individual data streams for specific UI components
    private val _weatherStations = MutableStateFlow<UiState<List<WeatherStation>>>(UiState())
    val weatherStations: StateFlow<UiState<List<WeatherStation>>> = _weatherStations.asStateFlow()

    private val _selectedStation = MutableStateFlow<WeatherStation?>(null)
    val selectedStation: StateFlow<WeatherStation?> = _selectedStation.asStateFlow()

    private val _widgetData = MutableStateFlow<UiState<WidgetData>>(UiState())
    val widgetData: StateFlow<UiState<WidgetData>> = _widgetData.asStateFlow()

    private val _historicalData = MutableStateFlow<UiState<List<WeatherData>>>(UiState())
    val historicalData: StateFlow<UiState<List<WeatherData>>> = _historicalData.asStateFlow()

    private val _chartsData = MutableStateFlow<UiState<List<WeatherData>>>(UiState())
    val chartsData: StateFlow<UiState<List<WeatherData>>> = _chartsData.asStateFlow()

    // Legacy LiveData support for existing MainActivity
    val weatherDataLastDay = _historicalData.asStateFlow().map { it.data ?: emptyList() }.asLiveData()
    val temperatureMaxMin = _widgetData.asStateFlow().map { widgetState ->
        widgetState.data?.let { widget ->
            TemperatureExtremes(max = widget.maxTemperature, min = widget.minTemperature)
        }
    }.asLiveData()
    val isLoading = _uiState.asStateFlow().map { it.isLoading }.asLiveData()
    val error = _uiState.asStateFlow().map { it.error }.asLiveData()

    data class WeatherUiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val selectedStationId: String? = null,
        val lastRefresh: Long = 0L
    )

    // ============ PUBLIC API METHODS ============

    /**
     * Load weather stations with proper state management and security logging
     */
    fun fetchWeatherStations() {
        Log.d(TAG, "Fetching weather stations")
        securityLogger.logUserSecurityEvent("fetch_stations", "main_screen")

        updateLoadingState(true)

        viewModelScope.launch {
            repository.getWeatherStations().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _weatherStations.value = UiState(isLoading = true)
                        updateLoadingState(true)
                    }
                    is Resource.Success -> {
                        val stations = resource.data
                        _weatherStations.value = UiState(data = stations, isLoading = false)

                        // Auto-select first station if none selected
                        if (_selectedStation.value == null && stations.isNotEmpty()) {
                            selectStation(stations[0].id)
                        }

                        updateLoadingState(false)
                        _uiState.value = _uiState.value.copy(error = null)
                        updateLastRefresh()

                        Log.d(TAG, "Weather stations loaded successfully: ${stations.size} stations")
                        securityLogger.logDataAccess("weather_stations", stations.size)
                    }
                    is Resource.Error -> {
                        _weatherStations.value = UiState(error = resource.message, isLoading = false)
                        updateError(resource.message ?: "Error desconocido")
                        updateLoadingState(false)

                        Log.e(TAG, "Error loading weather stations: ${resource.message}")
                        securityLogger.logNetworkSecurityEvent("stations", 500)
                    }
                }
            }
        }
    }

    /**
     * Select a weather station with comprehensive data loading
     */
    fun selectStation(stationId: String) {
        Log.d(TAG, "Selecting station: $stationId")
        securityLogger.logUserSecurityEvent("select_station", "main_screen", additionalInfo = stationId)

        val stations = _weatherStations.value.data ?: emptyList()
        val station = stations.find { it.id == stationId }

        if (station != null) {
            _selectedStation.value = station
            updateSelectedStation(stationId)

            // Load all data for the selected station
            loadStationData(stationId)

            Log.d(TAG, "Station selected successfully: ${station.name}")
        } else {
            updateError("Estación no encontrada: $stationId")
            Log.w(TAG, "Station not found: $stationId")
            securityLogger.logSecurityViolation("invalid_station_access", details = stationId)
        }
    }

    /**
     * Load all data for a specific station (widget + historical + charts)
     */
    private fun loadStationData(stationId: String) {
        // Load widget data (always available)
        fetchWidgetData(stationId)

        // Load historical data (requires authentication)
        fetchHistoricalData(stationId)

        // Load charts data (requires authentication)
        fetchChartsData(stationId)
    }

    /**
     * Fetch widget data with modern state management
     */
    fun fetchWidgetData(stationId: String) {
        Log.d(TAG, "Fetching widget data for station: $stationId")

        viewModelScope.launch {
            repository.getWidgetData(stationId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _widgetData.value = UiState(isLoading = true)
                    }
                    is Resource.Success -> {
                        val data = resource.data
                        _widgetData.value = UiState(data = data, isLoading = false)

                        Log.d(TAG, "Widget data loaded for station: $stationId")
                        Log.d(TAG, "Temperature: ${data.getFormattedTemperature()}")
                    }
                    is Resource.Error -> {
                        _widgetData.value = UiState(error = resource.message, isLoading = false)
                        Log.e(TAG, "Error loading widget data: ${resource.message}")
                    }
                }
            }
        }
    }

    /**
     * Legacy method for compatibility - now delegates to fetchWidgetData
     */
    fun fetchTemperatureMaxMin(stationName: String) {
        fetchWidgetData(stationName)
    }

    /**
     * Fetch historical data with proper authentication and error handling
     */
    fun fetchWeatherDataLastDay(stationId: String) {
        fetchHistoricalData(stationId)
    }

    private fun fetchHistoricalData(stationId: String) {
        Log.d(TAG, "Fetching historical data for station: $stationId")

        viewModelScope.launch {
            val (from, to) = getYesterdayDateRange()

            repository.getWeatherDataTimeRange(stationId, from, to).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _historicalData.value = UiState(isLoading = true)
                    }
                    is Resource.Success -> {
                        val data = resource.data
                        _historicalData.value = UiState(data = data, isLoading = false)

                        Log.d(TAG, "Historical data loaded: ${data.size} records")
                        securityLogger.logDataAccess("historical_data", data.size, stationId)
                    }
                    is Resource.Error -> {
                        _historicalData.value = UiState(error = resource.message, isLoading = false)
                        Log.e(TAG, "Error loading historical data: ${resource.message}")

                        // Handle authentication errors specifically
                        if (resource.message?.contains("autenticación", ignoreCase = true) == true) {
                            securityLogger.logAuthenticationEvent("data_access_denied", false)
                        }
                    }
                }
            }
        }
    }

    private fun fetchChartsData(stationId: String) {
        Log.d(TAG, "Fetching charts data for station: $stationId")

        viewModelScope.launch {
            val (from, to) = getYesterdayDateRange()

            repository.getChartsData(stationId, from, to).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _chartsData.value = UiState(isLoading = true)
                    }
                    is Resource.Success -> {
                        val data = resource.data
                        _chartsData.value = UiState(data = data, isLoading = false)

                        Log.d(TAG, "Charts data loaded: ${data.size} records")
                    }
                    is Resource.Error -> {
                        _chartsData.value = UiState(error = resource.message, isLoading = false)
                        Log.e(TAG, "Error loading charts data: ${resource.message}")
                    }
                }
            }
        }
    }

    private fun getYesterdayDateRange(): Pair<String, String> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)

        // End of yesterday
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfYesterday = dateFormat.format(calendar.time)

        // Start of yesterday
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfYesterday = dateFormat.format(calendar.time)

        return Pair(startOfYesterday, endOfYesterday)
    }

    /**
     * Refresh all data for the current station
     */
    fun refreshData() {
        val currentStationId = _selectedStation.value?.id
        if (currentStationId != null) {
            Log.d(TAG, "Refreshing all data for station: $currentStationId")
            securityLogger.logUserSecurityEvent("refresh_data", "main_screen", additionalInfo = currentStationId)

            loadStationData(currentStationId)
        } else {
            Log.w(TAG, "No station selected for refresh")
        }
    }

    // ============ PRIVATE STATE MANAGEMENT METHODS ============

    private fun updateLoadingState(loading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = loading)
    }

    private fun updateError(error: String) {
        _uiState.value = _uiState.value.copy(error = error)
    }

    private fun updateSelectedStation(stationId: String) {
        _uiState.value = _uiState.value.copy(selectedStationId = stationId)
    }

    private fun updateLastRefresh() {
        _uiState.value = _uiState.value.copy(lastRefresh = System.currentTimeMillis())
    }

    /**
     * Public method to clear errors
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Get current selected station ID
     */
    fun getCurrentStationId(): String? {
        return _selectedStation.value?.id
    }

    /**
     * Check if data needs refresh (older than 5 minutes)
     */
    fun shouldRefreshData(): Boolean {
        val lastRefresh = _uiState.value.lastRefresh
        return System.currentTimeMillis() - lastRefresh > 5 * 60 * 1000 // 5 minutes
    }

    // ============ LEGACY METHODS FOR BACKWARD COMPATIBILITY ============

    // These methods are maintained for compatibility with existing code
    // but internally use the new architecture

    fun fetchPublicChartsData(stationName: String) {
        fetchChartsData(stationName)
    }
}