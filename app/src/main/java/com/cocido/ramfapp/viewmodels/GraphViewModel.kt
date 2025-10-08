package com.cocido.ramfapp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cocido.ramfapp.models.WeatherStation
import com.cocido.ramfapp.models.WeatherData
import com.cocido.ramfapp.common.Resource
import com.cocido.ramfapp.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GraphUiState(
    val isLoading: Boolean = false,
    val weatherData: List<WeatherData> = emptyList(),
    val stations: List<WeatherStation> = emptyList(),
    val currentStationId: String = "00210E7D",
    val selectedParameters: Set<String> = setOf("temperatura"),
    val errorMessage: String? = null,
    val dateRangeLabel: String = "24h"
)

class GraphViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(GraphUiState())
    val uiState: StateFlow<GraphUiState> = _uiState.asStateFlow()

    init {
        loadStations()
    }

    private fun loadStations() {
        viewModelScope.launch {
            repository.getWeatherStations().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                    }
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            stations = resource.data,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Error al cargar estaciones: ${resource.message}"
                        )
                    }
                }
            }
        }
    }

    fun loadWeatherData(from: String, to: String) {
        val currentState = _uiState.value
        Log.d("GraphViewModel", "🔄 Loading weather data:")
        Log.d("GraphViewModel", "   Station: ${currentState.currentStationId}")
        Log.d("GraphViewModel", "   From: $from")
        Log.d("GraphViewModel", "   To: $to")
        
        viewModelScope.launch {
            repository.getChartsData(
                stationName = currentState.currentStationId,
                from = from,
                to = to
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                    }
                    is Resource.Success -> {
                        Log.d("GraphViewModel", "✅ Data loaded successfully: ${resource.data.size} points")
                        _uiState.value = _uiState.value.copy(
                            weatherData = resource.data,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Error al cargar datos: ${resource.message}"
                        )
                    }
                }
            }
        }
    }

    fun selectStation(stationId: String) {
        _uiState.value = _uiState.value.copy(currentStationId = stationId)
    }

    fun toggleParameter(parameter: String) {
        val currentParams = _uiState.value.selectedParameters.toMutableSet()
        if (currentParams.contains(parameter)) {
            currentParams.remove(parameter)
        } else {
            currentParams.add(parameter)
        }
        _uiState.value = _uiState.value.copy(selectedParameters = currentParams)
    }

    fun updateDateRangeLabel(label: String) {
        _uiState.value = _uiState.value.copy(dateRangeLabel = label)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

class GraphViewModelFactory(private val repository: WeatherRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GraphViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GraphViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}