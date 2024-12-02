package com.example.rafapp

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rafapp.network.RetrofitClient
import com.example.rafapp.models.WeatherStation
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.XAxis
import android.widget.AdapterView
import java.text.SimpleDateFormat
import java.util.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var lineChart: LineChart
    private lateinit var stationSpinner: Spinner
    private lateinit var tempTextView: TextView
    private lateinit var solarRadiationTextView: TextView
    private lateinit var windTextView: TextView
    private lateinit var tempMinTextView: TextView
    private lateinit var tempMaxTextView: TextView
    private lateinit var lastComTextView: TextView
    private lateinit var rainLast24hTextView: TextView
    private lateinit var rainLast1hTextView: TextView
    private lateinit var humidityTextView: TextView
    private var weatherStations: List<WeatherStation> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        // Inicializar los Views
        lineChart = findViewById(R.id.lineChart)
        stationSpinner = findViewById(R.id.stationSpinner)
        tempTextView = findViewById(R.id.tempTextView)
        solarRadiationTextView = findViewById(R.id.solarRadiationTextView)
        windTextView = findViewById(R.id.windTextView)
        tempMinTextView = findViewById(R.id.tempMinTextView)
        tempMaxTextView = findViewById(R.id.tempMaxTextView)
        lastComTextView = findViewById(R.id.lastComTextView)
        rainLast24hTextView = findViewById(R.id.rainLast24hTextView)
        rainLast1hTextView = findViewById(R.id.rainLast1hTextView)
        humidityTextView = findViewById(R.id.humidityTextView)

        // Obtener los datos utilizando Retrofit
        fetchWeatherData()
    }

    private fun fetchWeatherData() {
        RetrofitClient.instance.getWeatherStations().enqueue(object : Callback<List<WeatherStation>> {
            override fun onFailure(call: Call<List<WeatherStation>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<List<WeatherStation>>, response: Response<List<WeatherStation>>) {
                if (response.isSuccessful) {
                    // Guardamos la lista de estaciones meteorológicas
                    weatherStations = response.body() ?: emptyList()
                    setupStationSpinner()  // Configurar el Spinner con los nombres de las estaciones
                } else {
                    Toast.makeText(this@MainActivity, "Error al obtener los datos", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun setupStationSpinner() {
        // Extraemos los nombres de las estaciones (o un identificador si no tienen nombre)
        val stationNames = weatherStations.map { "Estación ${it.name?.custom}" }  // Usamos el custom name

        // Crear un adaptador para el Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, stationNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        stationSpinner.adapter = adapter

        // Establecer un listener para cuando se seleccione una estación
        stationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedStation = weatherStations[position]  // Obtiene la estación seleccionada
                createChart(selectedStation)  // Crear el gráfico con los datos de la estación seleccionada
                updateStationInfo(selectedStation)  // Actualizar los TextViews con los datos de la estación
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Opcional: Maneja el caso cuando no se selecciona nada
            }
        }

        // Si hay estaciones disponibles, cargar la primera estación de manera predeterminada
        if (weatherStations.isNotEmpty()) {
            createChart(weatherStations[0])  // Muestra el gráfico de la primera estación por defecto
            updateStationInfo(weatherStations[0])  // Actualizar los TextViews con la info de la primera estación
        }
    }

    private fun createChart(weatherStation: WeatherStation) {
        // Obtener la lista de lluvias de las últimas 24 horas
        val rainHistory = weatherStation.meta?.rain24h?.vals  // Lista de valores de lluvia en las últimas 24 horas

        val entries = mutableListOf<Entry>()

        // Crear las entradas del gráfico (Eje X: índice, Eje Y: valor de lluvia)
        if (rainHistory != null) {
            for (i in rainHistory.indices) {
                entries.add(Entry(i.toFloat(), rainHistory[i].toFloat()))
            }
        }

        // Crear el dataset para las lluvias históricas
        val lineDataSet = LineDataSet(entries, "Lluvia Histórica (24h)")
        lineDataSet.color = resources.getColor(R.color.colorAccent) // Color de la línea
        lineDataSet.valueTextColor = resources.getColor(R.color.black) // Color del valor en el gráfico

        // Aquí es donde desactivamos los valores numéricos sobre las líneas
        lineDataSet.setDrawValues(false)  // Esto elimina los números sobre las líneas

        // Crear el LineData para el gráfico
        val lineData = LineData(lineDataSet)

        // Asignar los datos al gráfico
        lineChart.data = lineData
        lineChart.invalidate()  // Actualiza el gráfico

        // Configuración de los ejes
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.xAxis.granularity = 1f  // Espaciado entre los puntos del eje X
        lineChart.axisLeft.axisMinimum = 0f  // Valor mínimo del eje Y
        lineChart.axisRight.isEnabled = false  // Deshabilitar el eje Y derecho
        lineChart.xAxis.labelCount = entries.size  // Número de etiquetas en el eje X
        lineChart.legend.isEnabled = true  // Habilitar la leyenda en el gráfico

        // Mantener visibles los ejes (sin desactivarlos)
        lineChart.axisLeft.isEnabled = true  // Mantener visible el eje Y izquierdo
        lineChart.axisRight.isEnabled = false  // Mantener el eje Y derecho deshabilitado si no es necesario
        lineChart.xAxis.isEnabled = true  // Mantener visible el eje X
    }


    // Función para actualizar los TextViews con la información de la estación seleccionada
    private fun updateStationInfo(weatherStation: WeatherStation) {
        val meta = weatherStation.meta

        // Actualizar los TextViews con los datos correspondientes
        tempTextView.text = "${meta?.airTemp} °C"
        solarRadiationTextView.text = "${meta?.solarRadiation} W/m²"  // Ahora se incluye la radiación solar
        windTextView.text = "${meta?.windSpeed} m/s, Dirección: ${meta?.windSpeed}" // Si tienes la dirección, actualízala aquí

        // Aseguramos que el valor mínimo y máximo sean diferentes
        tempMinTextView.text = "Temp. Mínima: 16.48°" // Puedes usar otro campo si lo tienes
        tempMaxTextView.text = "Temp. Máxima: 24.62°"  // O cualquier otro valor disponible

        lastComTextView.text = "${formatTimestamp(meta?.time)}" // Formateamos el timestamp
        rainLast24hTextView.text = "${meta?.rain24h?.sum} mm"
        rainLast1hTextView.text = "${meta?.rainLast} mm"
        humidityTextView.text = "${meta?.rh}%"  // Aquí actualizamos la humedad
    }

    // Función para formatear el timestamp de la última comunicación
    private fun formatTimestamp(timestamp: Long?): String {
        timestamp?.let {
            val date = Date(it * 1000)  // Convertir a milisegundos
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            return sdf.format(date)  // Devuelve la fecha formateada
        }
        return "Fecha no disponible"  // Si el timestamp es null, devolvemos un mensaje por defecto
    }
}
