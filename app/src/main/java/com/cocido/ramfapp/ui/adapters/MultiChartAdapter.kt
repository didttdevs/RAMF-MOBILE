package com.cocido.ramfapp.ui.adapters

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.cocido.ramfapp.R
import com.cocido.ramfapp.models.AxisPosition
import com.cocido.ramfapp.models.ChartCategory
import com.cocido.ramfapp.models.ChartConfig
import com.cocido.ramfapp.models.ChartParameter
import com.cocido.ramfapp.models.ChartValueKey
import com.cocido.ramfapp.models.ChartsPayload
import com.cocido.ramfapp.models.SeriesStyle
import com.cocido.ramfapp.models.WeatherData
import com.cocido.ramfapp.models.getPoints
import com.cocido.ramfapp.utils.ChartUtils
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

/**
 * Adapter profesional para mostrar múltiples gráficos en RecyclerView
 */
class MultiChartAdapter(
    private var charts: List<ChartConfig>,
    private var weatherData: List<WeatherData>,
    private var chartsData: ChartsPayload? = null
) : RecyclerView.Adapter<MultiChartAdapter.ChartViewHolder>() {

    private var onChartOptionsClickListener: ((ChartConfig) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chart_card, parent, false)
        return ChartViewHolder(view)
    }

    private fun ChartValueKey.extractFromWeatherData(weatherData: WeatherData): Double? {
        val sensors = weatherData.sensors
        return when (this) {
            ChartValueKey.TEMPERATURA -> sensors.hcAirTemperature?.avg
            ChartValueKey.HUMEDAD -> sensors.hcRelativeHumidity?.avg
            ChartValueKey.PUNTO_ROCIO -> sensors.dewPoint?.avg
            ChartValueKey.VPD -> sensors.vpd?.avg
            ChartValueKey.DELTA_T -> sensors.deltaT?.avg
            ChartValueKey.RADIACION_SOLAR -> sensors.solarRadiation?.avg
            ChartValueKey.VELOCIDAD_VIENTO -> sensors.usonicWindSpeed?.avg
            ChartValueKey.RAFAGA_VIENTO -> sensors.windGust?.max
            ChartValueKey.DIRECCION_VIENTO -> sensors.usonicWindDir?.last ?: sensors.windOrientation?.result
            ChartValueKey.PRESION -> sensors.airPressure?.avg
            ChartValueKey.PRECIPITACION -> sensors.precipitation?.sum
            ChartValueKey.ET0 -> null
            ChartValueKey.BATERIA -> null
            ChartValueKey.PANEL_SOLAR -> null
            ChartValueKey.HORAS_SOL -> null
            ChartValueKey.ORIENTACION -> sensors.windOrientation?.result
        }
    }

    override fun onBindViewHolder(holder: ChartViewHolder, position: Int) {
        val chartConfig = charts[position]
        
        // Usar datos del backend si están disponibles, sino usar datos legacy
        if (chartsData != null) {
            holder.bindWithBackendData(chartConfig, chartsData!!)
        } else {
            holder.bind(chartConfig, weatherData)
        }
    }

    override fun getItemCount(): Int = charts.size

    fun updateCharts(newCharts: List<ChartConfig>) {
        charts = newCharts
        notifyDataSetChanged()
    }

    fun updateData(newData: List<WeatherData>) {
        weatherData = newData
        notifyDataSetChanged()
    }

    fun updateChartsData(newChartsData: ChartsPayload) {
        chartsData = newChartsData
        notifyDataSetChanged()
    }

    fun setOnChartOptionsClickListener(listener: (ChartConfig) -> Unit) {
        onChartOptionsClickListener = listener
    }

    inner class ChartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val chartTitle: TextView = itemView.findViewById(R.id.chartTitle)
        private val lineChart: LineChart = itemView.findViewById(R.id.lineChart)
        private val chartOptionsButton: ImageView = itemView.findViewById(R.id.chartOptionsButton)
        private val currentValue: TextView = itemView.findViewById(R.id.currentValue)
        private val minValue: TextView = itemView.findViewById(R.id.minValue)
        private val maxValue: TextView = itemView.findViewById(R.id.maxValue)
        private val avgValue: TextView = itemView.findViewById(R.id.avgValue)

        fun bind(chartConfig: ChartConfig, data: List<WeatherData>) {
            chartTitle.text = chartConfig.title

            // Configurar listener de opciones
            chartOptionsButton.setOnClickListener {
                onChartOptionsClickListener?.invoke(chartConfig)
            }

            // Configurar gráfico
            setupChart(lineChart, chartConfig)

            val seriesData = chartConfig.parameters.mapNotNull { parameter ->
                val entries = mapEntriesFromWeatherData(parameter, data)
                if (entries.isNotEmpty()) {
                    SeriesData(parameter, createLineDataSet(entries, parameter))
                } else null
            }

            if (seriesData.isNotEmpty()) {
                val lineData = LineData(seriesData.map { it.dataSet })
                lineChart.data = lineData
                configureAxes(lineChart, seriesData)
                lineChart.animateX(800)
                lineChart.invalidate()
                updateStats(seriesData.firstOrNull()?.dataSet, seriesData.firstOrNull()?.parameter?.unit ?: "")
            } else {
                lineChart.clear()
                currentValue.text = "--"
                minValue.text = "--"
                maxValue.text = "--"
                avgValue.text = "--"
            }
        }

        fun bindWithBackendData(chartConfig: ChartConfig, chartsData: ChartsPayload) {
            chartTitle.text = chartConfig.title

            chartOptionsButton.setOnClickListener {
                onChartOptionsClickListener?.invoke(chartConfig)
            }

            setupChart(lineChart, chartConfig)

            val seriesData = chartConfig.parameters.mapNotNull { parameter ->
                val entries = mapEntriesForParameter(parameter, chartsData)
                if (entries.isNotEmpty()) {
                    SeriesData(parameter, createLineDataSet(entries, parameter))
                } else null
            }

            if (seriesData.isNotEmpty()) {
                val lineData = LineData(seriesData.map { it.dataSet })
                lineChart.data = lineData
                configureAxes(lineChart, seriesData)
                lineChart.animateX(800)
                lineChart.invalidate()
                updateStats(seriesData.firstOrNull()?.dataSet, seriesData.firstOrNull()?.parameter?.unit ?: "")
            } else {
                lineChart.clear()
                currentValue.text = "--"
                minValue.text = "--"
                maxValue.text = "--"
                avgValue.text = "--"
            }
        }

        private fun mapEntriesForParameter(parameter: ChartParameter, chartsData: ChartsPayload): List<Entry> {
            val group = parameter.sourceGroup ?: return emptyList()
            val points = chartsData.charts.getPoints(group) ?: return emptyList()

            return points.mapNotNull { chartPoint ->
                val timestamp = parseBackendTimestamp(chartPoint.date) ?: return@mapNotNull null
                val rawValue = parameter.valueKey?.extractor?.invoke(chartPoint) ?: return@mapNotNull null
                val scaledValue = (rawValue * parameter.scaleFactor).toFloat()
                Entry(timestamp.toFloat(), scaledValue).apply { this.data = rawValue }
            }.sortedBy { it.x }
        }

        private fun mapEntriesFromWeatherData(parameter: ChartParameter, data: List<WeatherData>): List<Entry> {
            return data.mapNotNull { weatherData ->
                val timestamp = parseTimestamp(weatherData.date) ?: return@mapNotNull null
                val rawValue = when {
                    parameter.valueExtractor != null -> parameter.valueExtractor.invoke(weatherData)
                    parameter.valueKey != null -> parameter.valueKey.extractFromWeatherData(weatherData)
                    else -> null
                } ?: return@mapNotNull null

                val scaledValue = (rawValue * parameter.scaleFactor).toFloat()
                Entry(timestamp.toFloat(), scaledValue).apply { this.data = rawValue }
            }.sortedBy { it.x }
        }

        private fun createLineDataSet(entries: List<Entry>, parameter: ChartParameter): LineDataSet {
            val dataSet = LineDataSet(entries, parameter.label).apply {
                color = parameter.color
                setCircleColor(parameter.color)
                lineWidth = parameter.seriesOptions.lineWidth
                setDrawCircles(parameter.seriesOptions.drawCircles)
                circleRadius = 3f
                setDrawCircleHole(false)
                setDrawValues(parameter.seriesOptions.drawValues)
                valueTextColor = Color.DKGRAY
                valueTextSize = 9f
                mode = LineDataSet.Mode.CUBIC_BEZIER
                cubicIntensity = 0.2f
                axisDependency = if (parameter.axisPosition == AxisPosition.LEFT) {
                    YAxis.AxisDependency.LEFT
                } else {
                    YAxis.AxisDependency.RIGHT
                }
                isHighlightEnabled = true
                highLightColor = parameter.color
                highlightLineWidth = 1.5f
                setDrawVerticalHighlightIndicator(true)
                setDrawHorizontalHighlightIndicator(true)
            }

            when (parameter.seriesOptions.style) {
                SeriesStyle.LINE -> {
                    dataSet.setDrawFilled(false)
                }

                SeriesStyle.AREA -> {
                    dataSet.setDrawFilled(true)
                    dataSet.fillAlpha = parameter.seriesOptions.fillAlpha
                    dataSet.fillColor = parameter.seriesOptions.fillColorOverride ?: parameter.color
                    dataSet.setDrawCircles(false)
                }

                SeriesStyle.STEP -> {
                    dataSet.mode = LineDataSet.Mode.STEPPED
                    dataSet.setDrawFilled(false)
                }

                SeriesStyle.DASHED_LINE -> {
                    dataSet.setDrawFilled(false)
                    parameter.seriesOptions.dashedIntervals?.let { intervals ->
                        if (intervals.size >= 2) {
                            dataSet.enableDashedLine(intervals[0], intervals[1], 0f)
                        }
                    }
                }
            }

            return dataSet
        }

        private fun configureAxes(chart: LineChart, seriesData: List<SeriesData>) {
            val leftSeries = seriesData.filter { it.parameter.axisPosition == AxisPosition.LEFT }
            val rightSeries = seriesData.filter { it.parameter.axisPosition == AxisPosition.RIGHT }

            configureAxis(chart.axisLeft, leftSeries, drawGridLines = true)
            configureAxis(chart.axisRight, rightSeries, drawGridLines = false)
        }

        private fun configureAxis(axis: YAxis, items: List<SeriesData>, drawGridLines: Boolean) {
            if (items.isEmpty()) {
                axis.isEnabled = false
                return
            }

            axis.isEnabled = true
            axis.setDrawGridLines(drawGridLines)

            val min = items.minOf { it.dataSet.yMin }
            val max = items.maxOf { it.dataSet.yMax }
            val range = max - min
            val padding = if (range == 0f) {
                if (max == 0f) 1f else abs(max) * 0.1f
            } else {
                range * 0.1f
            }

            axis.axisMinimum = min - padding
            axis.axisMaximum = max + padding
            axis.textColor = items.first().parameter.color
            axis.setLabelCount(6, true)
        }

        private fun parseBackendTimestamp(dateString: String): Long? {
            return try {
                // El backend devuelve fechas en formato "dd-MM-yyyy HH:mm:ss"
                val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
                formatter.parse(dateString)?.time
            } catch (e: Exception) {
                Log.e("MultiChartAdapter", "Error parsing backend timestamp: $dateString", e)
                null
            }
        }

        private fun setupChart(chart: LineChart, config: ChartConfig) {
            chart.apply {
                description = Description().apply { text = "" }
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)
                setDrawGridBackground(false)
                setBackgroundColor(Color.WHITE)

                // Configurar eje X (tiempo)
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    textColor = ContextCompat.getColor(context, R.color.chart_text_color)
                    textSize = 10f
                    setDrawGridLines(true)
                    gridColor = ContextCompat.getColor(context, R.color.chart_grid_color)
                    gridLineWidth = 0.5f
                    setDrawAxisLine(true)
                    axisLineColor = ContextCompat.getColor(context, R.color.chart_axis_color)
                    axisLineWidth = 1f
                    valueFormatter = ChartUtils.createTimeFormatter()
                    labelCount = 5
                    isGranularityEnabled = true
                    setAvoidFirstLastClipping(true)
                }

                // Configurar eje Y izquierdo
                axisLeft.apply {
                    textColor = ContextCompat.getColor(context, R.color.chart_text_color)
                    textSize = 10f
                    setDrawGridLines(true)
                    gridColor = ContextCompat.getColor(context, R.color.chart_grid_color)
                    gridLineWidth = 0.5f
                    setDrawAxisLine(true)
                    axisLineColor = ContextCompat.getColor(context, R.color.chart_axis_color)
                    axisLineWidth = 1f
                    setDrawZeroLine(true)
                    zeroLineColor = ContextCompat.getColor(context, R.color.chart_zero_line_color)
                    zeroLineWidth = 1.5f
                }

                // Configurar eje Y derecho (para gráficos combinados)
                axisRight.apply {
                    val hasRightAxis = config.parameters.any { it.axisPosition == AxisPosition.RIGHT }
                    isEnabled = hasRightAxis
                    textColor = ContextCompat.getColor(context, R.color.chart_text_color)
                    textSize = 10f
                    setDrawGridLines(false)
                    setDrawAxisLine(true)
                    axisLineColor = ContextCompat.getColor(context, R.color.chart_axis_color)
                    axisLineWidth = 1f
                }

                // Configurar leyenda
                legend.apply {
                    isEnabled = config.parameters.size > 1
                    textColor = ContextCompat.getColor(context, R.color.chart_text_color)
                    textSize = 11f
                    form = Legend.LegendForm.LINE
                    formLineWidth = 3f
                    formSize = 10f
                    verticalAlignment = Legend.LegendVerticalAlignment.TOP
                    horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                    orientation = Legend.LegendOrientation.VERTICAL
                    setDrawInside(true)
                    xEntrySpace = 7f
                    yEntrySpace = 5f
                }

                // Márgenes
                setExtraOffsets(10f, 20f, 10f, 10f)
            }
        }

        private fun updateStats(dataSet: LineDataSet?, unit: String) {
            if (dataSet == null || dataSet.entryCount == 0) {
                currentValue.text = "--"
                minValue.text = "--"
                maxValue.text = "--"
                avgValue.text = "--"
                return
            }

            val values = (0 until dataSet.entryCount).map { index ->
                val entry = dataSet.getEntryForIndex(index)
                val raw = (entry.data as? Number)?.toFloat()
                raw ?: entry.y
            }
            val min = values.minOrNull() ?: 0f
            val max = values.maxOrNull() ?: 0f
            val avg = values.average().toFloat()
            val current = values.lastOrNull() ?: 0f

            currentValue.text = String.format("%.1f%s", current, unit)
            minValue.text = String.format("%.1f%s", min, unit)
            maxValue.text = String.format("%.1f%s", max, unit)
            avgValue.text = String.format("%.1f%s", avg, unit)
        }

        private fun parseTimestamp(dateString: String): Long? {
            return try {
                // Formato del backend: dd-MM-yyyy HH:mm:ss
                val backendFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US)
                backendFormat.timeZone = TimeZone.getTimeZone("UTC")
                backendFormat.parse(dateString)?.time
            } catch (e: Exception) {
                try {
                    // Fallback: formato ISO
                    val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                    isoFormat.timeZone = TimeZone.getTimeZone("UTC")
                    isoFormat.parse(dateString)?.time
                } catch (e2: Exception) {
                    null
                }
            }
        }
    }
}

private data class SeriesData(
    val parameter: ChartParameter,
    val dataSet: LineDataSet
)

