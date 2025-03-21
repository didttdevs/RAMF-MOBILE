package com.example.rafapp.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rafapp.models.WeatherStation
import com.example.rafapp.models.TemperatureMaxMin
import com.example.rafapp.models.WeatherDataLastDayResponse
import com.example.rafapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeatherStationViewModel : ViewModel() {
    private val _weatherStations = MutableLiveData<List<WeatherStation>>()
    val weatherStations: LiveData<List<WeatherStation>> get() = _weatherStations

    private val _selectedStationData = MutableLiveData<WeatherStation>()
    val selectedStationData: LiveData<WeatherStation> get() = _selectedStationData

    private val _temperatureMaxMin = MutableLiveData<TemperatureMaxMin>()
    val temperatureMaxMin: LiveData<TemperatureMaxMin> get() = _temperatureMaxMin

    // Cambiado para aceptar una lista de respuestas de clima del último día
    private val _weatherData = MutableLiveData<List<WeatherDataLastDayResponse>>()
    val weatherData: LiveData<List<WeatherDataLastDayResponse>> get() = _weatherData

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // Obtener estaciones
    fun fetchWeatherStations(context: Context) {
        val token = "Bearer " + getToken(context)
        Log.d("API_CALL", "Llamando a /api/station con token: $token")

        RetrofitClient.weatherStationService.getWeatherStations(token).enqueue(object : Callback<List<WeatherStation>> {
            override fun onResponse(call: Call<List<WeatherStation>>, response: Response<List<WeatherStation>>) {
                if (response.isSuccessful && response.body() != null) {
                    _weatherStations.postValue(response.body())
                    Log.d("API_RESPONSE", "Datos recibidos: ${response.body()}")
                } else {
                    _error.postValue("Error: ${response.code()} ${response.message()}")
                    Log.e("API_ERROR", "Error al obtener estaciones: ${response.code()} ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<WeatherStation>>, t: Throwable) {
                _error.postValue("Error de conexión: ${t.localizedMessage}")
                Log.e("NETWORK_ERROR", "Error de conexión: ${t.localizedMessage}")
            }
        })
    }

    // Obtener datos de una estación
    fun fetchStationData(context: Context, stationId: String) {
        val token = "Bearer " + getToken(context)
        Log.d("API_CALL", "Obteniendo datos de la estación con ID: $stationId")

        RetrofitClient.weatherStationService.getWeatherStationById(token, stationId).enqueue(object : Callback<WeatherStation> {
            override fun onResponse(call: Call<WeatherStation>, response: Response<WeatherStation>) {
                if (response.isSuccessful && response.body() != null) {
                    _selectedStationData.postValue(response.body())
                    Log.d("API_RESPONSE", "Datos de estación recibidos: ${response.body()}")
                } else {
                    _error.postValue("Error al obtener los datos de la estación: ${response.code()} ${response.message()}")
                    Log.e("API_ERROR", "Error al obtener datos de estación: ${response.code()} ${response.message()}")
                }
            }

            override fun onFailure(call: Call<WeatherStation>, t: Throwable) {
                _error.postValue("Error de conexión al obtener los datos de la estación: ${t.localizedMessage}")
                Log.e("NETWORK_ERROR", "Error de conexión: ${t.localizedMessage}")
            }
        })
    }

    // Obtener temperaturas máximas y mínimas
    fun fetchTemperatureMaxMin(context: Context, stationId: String) {
        val token = "Bearer " + getToken(context)
        Log.d("API_CALL", "Obteniendo temperaturas max y min de la estación con ID: $stationId")

        RetrofitClient.weatherStationService.getTemperatureMaxMin(token, stationId).enqueue(object : Callback<TemperatureMaxMin> {
            override fun onResponse(call: Call<TemperatureMaxMin>, response: Response<TemperatureMaxMin>) {
                if (response.isSuccessful && response.body() != null) {
                    _temperatureMaxMin.postValue(response.body())
                    Log.d("API_RESPONSE", "Temperaturas max y min recibidas: ${response.body()}")
                } else {
                    _error.postValue("Error al obtener las temperaturas máximas y mínimas: ${response.code()} ${response.message()}")
                    Log.e("API_ERROR", "Error al obtener temperaturas: ${response.code()} ${response.message()}")
                }
            }

            override fun onFailure(call: Call<TemperatureMaxMin>, t: Throwable) {
                _error.postValue("Error de conexión al obtener temperaturas: ${t.localizedMessage}")
                Log.e("NETWORK_ERROR", "Error de conexión: ${t.localizedMessage}")
            }
        })
    }

    // Obtener los datos del clima del último día (ahora manejando una lista)
    fun fetchWeatherDataLastDay(context: Context, stationId: String) {
        val token = "Bearer " + getToken(context)
        Log.d("API_CALL", "Obteniendo datos del clima de la estación con ID: $stationId")

        RetrofitClient.weatherStationService.getWeatherDataLastDay(token, stationId).enqueue(object : Callback<List<WeatherDataLastDayResponse>> {
            override fun onResponse(call: Call<List<WeatherDataLastDayResponse>>, response: Response<List<WeatherDataLastDayResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    _weatherData.postValue(response.body())  // Actualizamos el LiveData con la respuesta
                    Log.d("API_RESPONSE", "Datos del clima del último día recibidos: ${response.body()}")
                } else {
                    _error.postValue("Error al obtener los datos del clima del último día: ${response.code()} ${response.message()}")
                    Log.e("API_ERROR", "Error al obtener datos del clima del último día: ${response.code()} ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<WeatherDataLastDayResponse>>, t: Throwable) {
                _error.postValue("Error de conexión al obtener datos del clima del último día: ${t.localizedMessage}")
                Log.e("NETWORK_ERROR", "Error de conexión: ${t.localizedMessage}")
            }
        })
    }

    private fun getToken(context: Context): String {
        val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("auth_token", "") ?: ""
    }
}
