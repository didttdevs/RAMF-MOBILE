package com.example.rafapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.rafapp.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class GraphFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_graph, container, false)
        val lineChart = view.findViewById<LineChart>(R.id.lineChart)

        // Datos de ejemplo para el gráfico
        val entries = mutableListOf<Entry>().apply {
            add(Entry(0f, 20f))
            add(Entry(1f, 25f))
            add(Entry(2f, 23f))
            add(Entry(3f, 30f))
        }

        // Configurar el conjunto de datos
        val dataSet = LineDataSet(entries, "Temperatura (°C)").apply {
            color = resources.getColor(R.color.purple_500, null) // Color de la línea
            valueTextColor = resources.getColor(R.color.black, null) // Color del texto de los valores
            lineWidth = 2f // Grosor de la línea
            setDrawCircles(true) // Mostrar puntos en los datos
            setDrawValues(false) // No mostrar valores encima de los puntos
        }

        // Crear el objeto LineData y asignarlo al gráfico
        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // Configurar el eje X (etiquetas de tiempo, por ejemplo)
        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(arrayOf("00:00", "06:00", "12:00", "18:00"))
        xAxis.textColor = resources.getColor(R.color.black, null)

        // Configurar el eje Y
        val leftAxis = lineChart.axisLeft
        leftAxis.textColor = resources.getColor(R.color.black, null)
        lineChart.axisRight.isEnabled = false // Deshabilitar el eje derecho

        // Configurar la descripción y otras propiedades del gráfico
        lineChart.description.isEnabled = false // Deshabilitar descripción
        lineChart.setTouchEnabled(true) // Habilitar interacciones táctiles
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)
        lineChart.setPinchZoom(true)

        // Refrescar el gráfico
        lineChart.invalidate()

        return view
    }
}