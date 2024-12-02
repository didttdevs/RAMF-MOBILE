package com.example.rafapp.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.rafapp.R
import com.example.rafapp.databinding.ActivityMainBinding
import com.example.rafapp.ui.fragments.DashboardFragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //binding.refreshButton.setOnClickListener {
        //    refreshData()
        //}

        //binding.btnLogout.setOnClickListener {
        //    logout()
        //}

        //if (savedInstanceState == null) {
        //    supportFragmentManager.beginTransaction()
        //        .replace(R.id.fragmentContainer, DashboardFragment())
        //        .commitNow()
        //}

        // Obtener el gráfico desde el XML
        val lineChart: LineChart = findViewById(R.id.lineChart)

        // Crear un conjunto de datos (entradas)
        val entries = mutableListOf<Entry>()
        entries.add(Entry(0f, 1f))  // x = 0, y = 1
        entries.add(Entry(1f, 3f))  // x = 1, y = 3
        entries.add(Entry(2f, 2f))  // x = 2, y = 2
        entries.add(Entry(3f, 4f))  // x = 3, y = 4
        entries.add(Entry(4f, 2f))  // x = 4, y = 2
        entries.add(Entry(5f, 5f))  // x = 5, y = 5

        // Crear un LineDataSet a partir de las entradas
        val dataSet = LineDataSet(entries, "")  // El nombre de la línea
        dataSet.color = resources.getColor(R.color.colorAccent)  // Cambiar color de la línea
        dataSet.valueTextColor = resources.getColor(R.color.black)  // Cambiar color de los valores

        // Crear el objeto LineData
        val lineData = LineData(dataSet)

        // Establecer los datos en el gráfico
        lineChart.data = lineData

        // Personalizar el gráfico
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.xAxis.granularity = 1f  // Evitar valores intermedios en el eje X
        lineChart.xAxis.labelCount = entries.size  // Establecer número de etiquetas en el eje X

        // Personalizar el eje Y
        lineChart.axisLeft.axisMinimum = 0f  // Establecer el valor mínimo en el eje Y
        lineChart.axisRight.isEnabled = false  // Deshabilitar el eje Y derecho

        // Actualizar la vista del gráfico
        lineChart.invalidate()  // Redibujar el gráfico
    }

    private fun refreshData() {
        //val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        //if (fragment is DashboardFragment) {
        //    fragment.refreshData()
        //}
    }

    private fun logout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    fun updateWeatherSummary(
        lastComm: String,
        temp: String,
        humidity: String,
        maxTemp: String,
        minTemp: String
    ) {
        binding.tvLastCommunication.text = "Última comunicación: $lastComm"
        binding.temperatureTextView.text = "${temp}°C"
        binding.humidityTextView.text = "Humedad: $humidity%"
        binding.maxTemperatureTextView.text = "Máx: ${maxTemp}°"
        binding.minTemperatureTextView.text = "Min: ${minTemp}°"
    }
}