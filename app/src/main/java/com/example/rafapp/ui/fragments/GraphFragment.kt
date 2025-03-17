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

class GraphFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_graph, container, false)
        val lineChart = view.findViewById<LineChart>(R.id.lineChart)

        val entries = mutableListOf<Entry>()
        entries.add(Entry(1f, 20f))
        entries.add(Entry(2f, 25f))
        entries.add(Entry(3f, 23f))
        entries.add(Entry(4f, 30f))

        val dataSet = LineDataSet(entries, "Temperatura")
        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.invalidate()

        return view
    }
}
