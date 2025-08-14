package com.example.rafapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.rafapp.databinding.FragmentWeatherInfoBinding
import com.example.rafapp.models.WeatherData
import com.example.rafapp.viewmodel.WeatherStationViewModel

class WeatherInfoFragment : Fragment() {

    private lateinit var binding: FragmentWeatherInfoBinding
    private lateinit var viewModel: WeatherStationViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeatherInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[WeatherStationViewModel::class.java]

        // Escuchamos solo los datos del último día
        viewModel.weatherDataLast.observe(viewLifecycleOwner) { weatherDataList ->
            val latest = weatherDataList.firstOrNull()
            updateWeatherDataFromSensors(latest)
        }
    }

    private fun updateWeatherDataFromSensors(data: WeatherData?) {
        val sensors = data?.sensors

        binding.humidityTextView.text =
            "${sensors?.hcRelativeHumidity?.avg ?: "---"} %"
        binding.dewPointTextView.text =
            "${sensors?.dewPoint?.avg ?: "---"} °C"
        binding.airPressureTextView.text =
            "${sensors?.airPressure?.avg ?: "---"} hPa"
        binding.solarRadiationTextView.text =
            "${sensors?.solarRadiation?.avg ?: "---"} W/m²"
        binding.windDirectionTextView.text =
            "${sensors?.usonicWindDir?.last ?: "---"}°"
        binding.windSpeedTextView.text =
            "${sensors?.usonicWindSpeed?.avg ?: "---"} m/s"
        binding.windGustTextView.text =
            "${sensors?.windGust?.max ?: "---"} m/s"
        binding.rainLast1hTextView.text =
            "--- mm" // No viene directamente en sensors, deberías agregarlo si es necesario
        binding.rainLast24hTextView.text =
            "${sensors?.precipitation?.sum ?: "---"} mm"
        binding.rainLast48hTextView.text =
            "--- mm" // Igual que rain1h
        binding.rainLast7dTextView.text =
            "--- mm"
    }
}
