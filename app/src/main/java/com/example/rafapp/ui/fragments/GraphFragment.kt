package com.example.rafapp.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.rafapp.databinding.FragmentGraphBinding
import com.example.rafapp.models.SensorData
import com.example.rafapp.models.WeatherDataLastDayResponse
import com.example.rafapp.viewmodel.WeatherStationViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class GraphFragment : Fragment() {

    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WeatherStationViewModel by activityViewModels()
    private var currentData: List<WeatherDataLastDayResponse> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGraphBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.weatherData.observe(viewLifecycleOwner) { data ->
            currentData = data
            updateChart("temperature") // por defecto temperatura
        }

        binding.parameterChipGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.chipTemperature.id -> updateChart("temperature")
                binding.chipHumidity.id -> updateChart("humidity")
                binding.chipRadiation.id -> updateChart("radiation")
                binding.chipWind.id -> updateChart("wind")
                binding.chipRain.id -> updateChart("rain")
            }
        }
    }

    private fun updateChart(parameter: String) {
        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()

        currentData.forEachIndexed { index, data ->
            val value = when (parameter) {
                "temperature" -> data.sensors.dewPoint?.avg
                "humidity" -> data.sensors.vPD?.avg
                "radiation" -> data.sensors.pressure?.avg
                "wind" -> data.sensors.windSpeed?.avg
                "rain" -> data.sensors.windOrientation?.result
                else -> null
            }

            value?.let {
                entries.add(Entry(index.toFloat(), it.toFloat()))
                labels.add(data.date.takeLast(5)) // solo hora o simplificado
            }
        }

        val label = when (parameter) {
            "temperature" -> "Temperatura (°C)"
            "humidity" -> "Humedad (%)"
            "radiation" -> "Radiación (W/m2)"
            "wind" -> "Viento (km/h)"
            "rain" -> "Precipitación"
            else -> "Dato"
        }

        val dataSet = LineDataSet(entries, label).apply {
            color = Color.MAGENTA
            valueTextColor = Color.BLACK
            lineWidth = 2f
            setDrawCircles(true)
            setDrawValues(false)
        }

        val lineData = LineData(dataSet)
        binding.lineChart.data = lineData

        binding.lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.lineChart.xAxis.textColor = Color.BLACK
        binding.lineChart.axisLeft.textColor = Color.BLACK
        binding.lineChart.axisRight.isEnabled = false
        binding.lineChart.description.isEnabled = false
        binding.lineChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
