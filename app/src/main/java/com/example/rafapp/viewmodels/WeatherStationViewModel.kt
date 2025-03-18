package com.example.rafapp.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rafapp.models.WeatherStation
import com.example.rafapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeatherStationViewModel : ViewModel() {
    private val _weatherStations = MutableLiveData<List<WeatherStation>>()
    val weatherStations: LiveData<List<WeatherStation>> get() = _weatherStations

    private val _selectedStationData = MutableLiveData<WeatherStation>()
    val selectedStationData: LiveData<WeatherStation> get() = _selectedStationData

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun fetchWeatherStations(context: Context) {
        val token = "Bearer " + getToken(context)

        Log.d("API_CALL", "Llamando a /api/station con token: $token")

        RetrofitClient.weatherStationService.getWeatherStations(token).enqueue(object : Callback<List<WeatherStation>> {
            override fun onResponse(call: Call<List<WeatherStation>>, response: Response<List<WeatherStation>>) {
                Log.d("API_RESPONSE", "Código de respuesta: ${response.code()}")

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

    private fun getToken(context: Context): String {
        val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("auth_token", "") ?: ""
    }
}
