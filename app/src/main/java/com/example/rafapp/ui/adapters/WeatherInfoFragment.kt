package com.example.rafapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.rafapp.databinding.FragmentWeatherInfoBinding
import com.example.rafapp.models.WeatherDataLastDayResponse
import com.example.rafapp.models.WeatherStation
import com.example.rafapp.viewmodel.WeatherStationViewModel

class WeatherInfoFragment : Fragment() {

    private var _binding: FragmentWeatherInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: WeatherStationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeatherInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener el ViewModel
        viewModel = ViewModelProvider(requireActivity()).get(WeatherStationViewModel::class.java)

        // Observador para los datos de la estación seleccionada
        viewModel.selectedStationData.observe(viewLifecycleOwner, Observer { weatherStation ->
            updateWeatherData(weatherStation)
        })

        // Observador para los datos de clima del último día
        viewModel.weatherData.observe(viewLifecycleOwner, Observer { weatherDataList ->
            updateWeatherDataLastDay(weatherDataList.firstOrNull())
        })
    }

    // Actualizar la UI con los datos de la estación
    fun updateWeatherData(weatherStation: WeatherStation) {
        val meta = weatherStation.meta

        // Mostrar la humedad relativa
        binding.humidityTextView.text = "${meta?.rh ?: "--"}%"

        // Mostrar la radiación solar
        binding.solarRadiationTextView.text = "${meta?.solarRadiation ?: "--"} W/m²"

        // Mostrar la velocidad del viento
        binding.windSpeedTextView.text = "${meta?.windSpeed ?: "--"} m/s"

        // Mostrar la precipitación en la última hora
        binding.rainLast1hTextView.text = "${meta?.rain1h ?: "--"} mm"

        // Mostrar la precipitación en las últimas 24 horas
        binding.rainLast24hTextView.text = "${meta?.rain24h?.sum ?: "--"} mm"

        // Mostrar la precipitación en las últimas 48 horas
        binding.rainLast48hTextView.text = "${meta?.rain48h?.sum ?: "--"} mm"

        // Mostrar la precipitación en los últimos 7 días
        binding.rainLast7dTextView.text = "${meta?.rain7d?.sum ?: "--"} mm"
    }

    // Actualizar la UI con los datos del clima del último día (por ejemplo, viento, presión, punto de rocío)
    fun updateWeatherDataLastDay(weatherData: WeatherDataLastDayResponse?) {
        if (weatherData == null) return

        val sensors = weatherData.sensors
        Log.e("WIND_DIRECTION", "Dirección del viento: ${sensors.windOrientation?.result}")
        // Mostrar la dirección del viento con grados convertidos a dirección cardinal
        val windDirection = sensors.windOrientation?.result?.let { getWindDirection(it) }
        binding.windDirectionTextView.text = windDirection ?: "--"


        // Mostrar la velocidad del viento
        binding.windSpeedTextView.text = "${sensors.uSonicWindSpeed?.avg ?: "--"} km/h"

        // Mostrar la presión del aire
        binding.airPressureTextView.text = "${sensors.vPD?.avg ?: "--"} kPa"

        // Mostrar el punto de rocío
        binding.dewPointTextView.text = "${sensors.dewPoint?.avg ?: "--"}°C"
    }

    // Función para convertir grados a dirección cardinal
    fun getWindDirection(degrees: Double?): String {
        val normalizedDegrees = degrees?.let { (it % 360) } ?: 0.0
        Log.e("WIND_DIRECTION", "Dirección del viento2: ${normalizedDegrees}")
        return when {
            normalizedDegrees in 0.0..5.0 -> "Norte, N"
            normalizedDegrees in 5.0..15.0 -> "Norte por el Noroeste, NpNE"
            normalizedDegrees in 15.0..25.0 -> "Norte Noroeste, NNE"
            normalizedDegrees in 25.0..35.0 -> "Noroeste por el Norte, NEpN"
            normalizedDegrees in 35.0..45.0 -> "Noroeste, NE"
            normalizedDegrees in 45.0..55.0 -> "Noroeste por el Este, NEpE"
            normalizedDegrees in 55.0..65.0 -> "Este Noroeste, ENE"
            normalizedDegrees in 65.0..75.0 -> "Este por el Noroeste, EpNE"
            normalizedDegrees in 75.0..85.0 -> "Este, E"
            normalizedDegrees in 85.0..95.0 -> "Este, E"
            normalizedDegrees in 95.0..105.0 -> "Este por el Suroeste, EpSE"
            normalizedDegrees in 105.0..115.0 -> "Este Suroeste, ESE"
            normalizedDegrees in 115.0..125.0 -> "Suroeste por el Este, SEpE"
            normalizedDegrees in 125.0..135.0 -> "Suroeste, SE"
            normalizedDegrees in 135.0..145.0 -> "Suroeste por el Sur, SEpS"
            normalizedDegrees in 145.0..155.0 -> "Sur Suroeste, SSE"
            normalizedDegrees in 155.0..165.0 -> "Sur por el Suroeste, SpSE"
            normalizedDegrees in 165.0..175.0 -> "Sur, S"
            normalizedDegrees in 175.0..185.0 -> "Sur, S"
            normalizedDegrees in 185.0..195.0 -> "Sur por el Suroeste, SpSO"
            normalizedDegrees in 195.0..205.0 -> "Sur Suroeste, SSO"
            normalizedDegrees in 205.0..215.0 -> "Suroeste por el Sur, SOpS"
            normalizedDegrees in 215.0..225.0 -> "Suroeste, SO"
            normalizedDegrees in 225.0..235.0 -> "Suroeste por el Oeste, SOpO"
            normalizedDegrees in 235.0..245.0 -> "Oeste Suroeste, OSO"
            normalizedDegrees in 245.0..255.0 -> "Oeste por el Suroeste, OpSO"
            normalizedDegrees in 255.0..265.0 -> "Oeste, O"
            normalizedDegrees in 265.0..275.0 -> "Oeste, O"
            normalizedDegrees in 275.0..285.0 -> "Oeste por el Noroeste, OpNO"
            normalizedDegrees in 285.0..295.0 -> "Oeste Noroeste, ONO"
            normalizedDegrees in 295.0..305.0 -> "Noroeste por el Oeste, NOpO"
            normalizedDegrees in 305.0..315.0 -> "Noroeste, NO"
            normalizedDegrees in 315.0..325.0 -> "Noroeste por el Norte, NOpN"
            normalizedDegrees in 325.0..335.0 -> "Norte Noroeste, NNO"
            normalizedDegrees in 335.0..345.0 -> "Norte por el Noroeste, NpNO"
            normalizedDegrees in 345.0..355.0 -> "Norte, N"
            normalizedDegrees in 355.0..360.0 -> "Norte, N"
            else -> "Desconocido"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
