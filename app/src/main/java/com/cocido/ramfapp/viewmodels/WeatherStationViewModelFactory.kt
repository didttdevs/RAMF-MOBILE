package com.cocido.ramfapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cocido.ramfapp.repository.WeatherRepository

class WeatherStationViewModelFactory(
    private val weatherRepository: WeatherRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherStationViewModel::class.java)) {
            return WeatherStationViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}



