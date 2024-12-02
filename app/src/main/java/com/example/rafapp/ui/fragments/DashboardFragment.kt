package com.example.rafapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.rafapp.databinding.FragmentDashboardBinding
import com.example.rafapp.viewmodel.WeatherStationViewModel
import com.example.rafapp.models.WeatherStation
import com.example.rafapp.models.Meta

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WeatherStationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observador para actualizar la vista con los datos
        viewModel.weatherStations.observe(viewLifecycleOwner, { stations ->
            if (stations.isNotEmpty()) {
                // Aquí puedes obtener los datos de la estación seleccionada
                val latestStation = stations.first() // Puedes usar la estación seleccionada si lo tienes

                // Actualizamos la vista con los datos de la estación seleccionada
                updateUIWithWeatherData(latestStation)
            } else {
                Toast.makeText(context, "No se encontraron estaciones", Toast.LENGTH_SHORT).show()
            }
        })

        // Observador para manejar los errores
        viewModel.error.observe(viewLifecycleOwner, { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        })

        // Inicializamos la carga de los datos
        refreshData()
    }

    fun refreshData() {
        // Cargar los datos desde la API
        viewModel.fetchWeatherStations()
    }

    private fun updateUIWithWeatherData(weatherStation: WeatherStation) {
        // Comprobamos si `meta` es nulo antes de acceder a sus valores
        val meta = weatherStation.meta
        if (meta != null) {
            // Actualizamos los TextViews y otros elementos según los datos de la estación
            //binding.tvStationName.text = weatherStation.name.original ?: "N/A"
            binding.tvTemperature.text = "${meta.airTemp?.toString() ?: "--.-"} °C"
            binding.tvHumidity.text = "Humidity: ${meta.rh?.toString() ?: "--.-"}%"
            //binding.tvLastUpdated.text = "Last updated: ${weatherStation.dates?.lastCommunication ?: "--/--/----"}"

            // Otros datos adicionales que puedas querer mostrar
            // Por ejemplo, si quieres mostrar la radiación solar:
            //binding.tvSolarRadiation.text = "Solar Radiation: ${meta.solarRadiation ?: "--"} W/m²"
        } else {
            // Si meta es nulo, mostrar un mensaje de error o valor por defecto
            binding.tvTemperature.text = "--.- °C"
            binding.tvHumidity.text = "Humidity: --.-%"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
