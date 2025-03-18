package com.example.rafapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.rafapp.databinding.FragmentWeatherInfoBinding
import com.example.rafapp.models.WeatherStation

class WeatherInfoFragment : Fragment() {

    private var _binding: FragmentWeatherInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWeatherInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun updateWeatherData(weatherStation: WeatherStation) {
        val meta = weatherStation.meta

        // Mostrar la humedad relativa
        binding.humidityTextView.text = "${meta?.rh ?: "--"}%"

        // Mostrar la radiación solar
        binding.solarRadiationTextView.text = "${meta?.solarRadiation ?: "--"} W/m²"

        // Mostrar la velocidad del viento
        binding.windTextView.text = "${meta?.windSpeed ?: "--"} m/s"

        // Mostrar la precipitación en la última hora
        binding.rainLast1hTextView.text = "${meta?.rain1h ?: "--"} mm"

        // Mostrar la precipitación en las últimas 24 horas
        binding.rainLast24hTextView.text = "${meta?.rain24h?.sum ?: "--"} mm"

        // Mostrar la precipitación en las últimas 48 horas
        binding.rainLast48hTextView.text = "${meta?.rain48h?.sum ?: "--"} mm"

        // Mostrar la precipitación en los últimos 7 días
        binding.rainLast7dTextView.text = "${meta?.rain7d?.sum ?: "--"} mm"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
