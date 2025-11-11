package com.cocido.ramfapp.ui.adapters

import android.graphics.Color
import android.util.Log
import android.view.MotionEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.cocido.ramfapp.R
import com.cocido.ramfapp.models.AxisPosition
import com.cocido.ramfapp.models.ChartAxisConfig
import com.cocido.ramfapp.models.ChartCategory
import com.cocido.ramfapp.models.ChartConfig
import com.cocido.ramfapp.models.ChartParameter
import com.cocido.ramfapp.models.ChartValueKey
import com.cocido.ramfapp.models.ChartsPayload
import com.cocido.ramfapp.models.SeriesStyle
import com.cocido.ramfapp.models.WeatherData
import com.cocido.ramfapp.models.getPoints
import com.cocido.ramfapp.utils.ChartUtils
import com.cocido.ramfapp.ui.views.AxisOverlayEntry
import com.cocido.ramfapp.ui.views.ChartAxisOverlayView
import com.cocido.ramfapp.ui.views.ChartMultiValueMarker
import com.cocido.ramfapp.ui.views.MarkerSeriesInfo
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.listener.ChartTouchListener.ChartGesture
import com.github.mikephil.charting.listener.OnChartGestureListener
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import java.util.concurrent.TimeUnit

/**
 * Adapter profesional para mostrar múltiples gráficos en RecyclerView
 */
class MultiChartAdapter(
    private var charts: List<ChartConfig>,
    private var weatherData: List<WeatherData>,
    private var chartsData: ChartsPayload? = null
) : RecyclerView.Adapter<MultiChartAdapter.ChartViewHolder>() {

    private val THREE_HOURS_MS: Float = TimeUnit.HOURS.toMillis(3L).toFloat()

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
        private val axisOverlay: ChartAxisOverlayView = itemView.findViewById(R.id.chartAxisOverlay)
        private val chartOptionsButton: ImageView = itemView.findViewById(R.id.chartOptionsButton)
        private val currentValue: TextView = itemView.findViewById(R.id.currentValue)
        private val minValue: TextView = itemView.findViewById(R.id.minValue)
        private val maxValue: TextView = itemView.findViewById(R.id.maxValue)
        private val avgValue: TextView = itemView.findViewById(R.id.avgValue)

        fun bind(chartConfig: ChartConfig, weatherSeries: List<WeatherData>) {
            chartTitle.text = chartConfig.title

            chartOptionsButton.setOnClickListener {
                onChartOptionsClickListener?.invoke(chartConfig)
            }

            setupChart(lineChart, chartConfig)

            val seriesData = chartConfig.parameters.mapNotNull { parameter ->
                val entries = mapEntriesFromWeatherData(chartConfig, parameter, weatherSeries)
                buildSeriesData(chartConfig, parameter, entries)
            }

            renderChart(chartConfig, seriesData)
        }

        fun bindWithBackendData(chartConfig: ChartConfig, chartsData: ChartsPayload) {
            chartTitle.text = chartConfig.title

            chartOptionsButton.setOnClickListener {
                onChartOptionsClickListener?.invoke(chartConfig)
            }

            setupChart(lineChart, chartConfig)

            val seriesData = chartConfig.parameters.mapNotNull { parameter ->
                val entries = mapEntriesForParameter(chartConfig, parameter, chartsData)
                buildSeriesData(chartConfig, parameter, entries)
            }

            renderChart(chartConfig, seriesData)
        }

        private fun clearHighlights() {
            if (!lineChart.isEmpty && lineChart.highlighted != null) {
                lineChart.highlightValue(null, false)
                lineChart.invalidate()
            }
        }

        private fun mapEntriesForParameter(
            chartConfig: ChartConfig,
            parameter: ChartParameter,
            chartsData: ChartsPayload
        ): List<Entry> {
            val group = parameter.sourceGroup ?: return emptyList()
            val points = chartsData.charts.getPoints(group) ?: return emptyList()
            val scaleFactor = chartConfig.scaleFactorFor(parameter)

            return points.mapNotNull { chartPoint ->
                val timestamp = parseBackendTimestamp(chartPoint.date) ?: return@mapNotNull null
                val rawValue = parameter.valueKey?.extractor?.invoke(chartPoint) ?: return@mapNotNull null
                Entry(timestamp.toFloat(), (rawValue * scaleFactor).toFloat()).apply { data = rawValue }
            }.sortedBy { it.x }
        }

        private fun mapEntriesFromWeatherData(
            chartConfig: ChartConfig,
            parameter: ChartParameter,
            weatherSeries: List<WeatherData>
        ): List<Entry> {
            val scaleFactor = chartConfig.scaleFactorFor(parameter)

            return weatherSeries.mapNotNull { weatherData ->
                val timestamp = parseTimestamp(weatherData.date) ?: return@mapNotNull null
                val rawValue = when {
                    parameter.valueExtractor != null -> parameter.valueExtractor.invoke(weatherData)
                    parameter.valueKey != null -> parameter.valueKey.extractFromWeatherData(weatherData)
                    else -> null
                } ?: return@mapNotNull null

                Entry(timestamp.toFloat(), (rawValue * scaleFactor).toFloat()).apply { data = rawValue }
            }.sortedBy { it.x }
        }

        private fun buildSeriesData(
            chartConfig: ChartConfig,
            parameter: ChartParameter,
            entries: List<Entry>
        ): SeriesData? {
            if (entries.isEmpty()) return null
            val axisConfig = chartConfig.axisFor(parameter)
            val dataSet = createLineDataSet(entries, parameter)
            val rawValues = entries.mapNotNull { (it.data as? Number)?.toDouble() }
            val rawMin = rawValues.minOrNull()
            val rawMax = rawValues.maxOrNull()
            return SeriesData(parameter, axisConfig, dataSet, rawMin, rawMax)
        }

        private fun renderChart(chartConfig: ChartConfig, seriesData: List<SeriesData>) {
            if (seriesData.isEmpty()) {
                lineChart.clear()
                axisOverlay.clear()
                currentValue.text = "--"
                minValue.text = "--"
                maxValue.text = "--"
                avgValue.text = "--"
                return
            }

            val lineData = LineData(seriesData.map { it.dataSet })
            lineChart.data = lineData
            lineChart.notifyDataSetChanged()

            val markerInfo = buildMarkerSeriesInfo(seriesData)
            val marker = (lineChart.marker as? ChartMultiValueMarker)
                ?: ChartMultiValueMarker(lineChart.context).also { lineChart.marker = it }
            marker.updateSeries(markerInfo)
            lineChart.setDrawMarkers(true)

            configureXAxis(lineChart, seriesData)

            val overlayEntries = configureAxes(lineChart, chartConfig, seriesData)
            val overlayWidthPx = axisOverlay.requiredWidthFor(overlayEntries)
            val density = lineChart.resources.displayMetrics.density
            val baseRightOffsetDp = if (overlayEntries.isEmpty()) 12f else 20f + (overlayWidthPx / density)
            lineChart.setExtraOffsets(8f, 12f, baseRightOffsetDp, 20f)

            axisOverlay.update(lineChart, overlayEntries)

            lineChart.animateX(800)
            lineChart.invalidate()

            val primarySeries = seriesData.firstOrNull { it.parameter.axisPosition == AxisPosition.LEFT }
                ?: seriesData.first()
            updateStats(primarySeries.dataSet, primarySeries.parameter.unit)
        }

        private fun buildMarkerSeriesInfo(seriesData: List<SeriesData>): List<MarkerSeriesInfo> {
            return seriesData.map { series ->
                val pattern = series.axisConfig?.formatPattern?.takeUnless { it.isBlank() } ?: "#0.##"
                val formatter = DecimalFormat(pattern)
                val scaleFactor = sequenceOf(
                    series.axisConfig?.scaleFactor,
                    series.parameter.scaleFactor
                ).firstOrNull { it != null && it != 0.0 } ?: 1.0

                MarkerSeriesInfo(
                    dataSet = series.dataSet,
                    label = series.parameter.label,
                    unit = series.parameter.unit,
                    color = series.parameter.color,
                    formatter = formatter,
                    scaleFactor = scaleFactor
                )
            }
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

        private fun configureAxes(
            chart: LineChart,
            chartConfig: ChartConfig,
            seriesData: List<SeriesData>
        ): List<AxisOverlayEntry> {
            val leftSeries = seriesData.filter { it.parameter.axisPosition == AxisPosition.LEFT }
            val rightSeries = seriesData.filter { it.parameter.axisPosition == AxisPosition.RIGHT }

            val leftConfigs = chartConfig.axes.filter { it.position == AxisPosition.LEFT }
            val rightConfigs = chartConfig.axes.filter { it.position == AxisPosition.RIGHT }
            val sortedRightConfigs = rightConfigs.sortedBy { it.overlayPriority }

            applyAxisConfig(
                chart = chart,
                axis = chart.axisLeft,
                config = leftConfigs.firstOrNull(),
                series = leftSeries,
                drawGridLines = true,
                suppressLabels = false
            )

            val primaryRightAxisId = chartConfig.parameters.firstOrNull { it.axisPosition == AxisPosition.RIGHT }?.axisId
            val primaryRightConfig = sortedRightConfigs.firstOrNull { it.id == primaryRightAxisId }
                ?: sortedRightConfigs.firstOrNull()
            val primaryRightSeries = primaryRightConfig?.let { config ->
                rightSeries.filter { it.axisConfig?.id == config.id }
            } ?: rightSeries

            applyAxisConfig(
                chart = chart,
                axis = chart.axisRight,
                config = primaryRightConfig,
                series = primaryRightSeries,
                drawGridLines = false,
                suppressLabels = primaryRightConfig != null
            )

            return buildOverlayEntries(sortedRightConfigs, seriesData)
        }

        private fun applyAxisConfig(
            chart: LineChart,
            axis: YAxis,
            config: ChartAxisConfig?,
            series: List<SeriesData>,
            drawGridLines: Boolean,
            suppressLabels: Boolean
        ) {
            if (config == null || series.isEmpty()) {
                axis.isEnabled = series.isNotEmpty()
                axis.setDrawGridLines(drawGridLines && series.isNotEmpty())
                if (!series.isNotEmpty()) {
                    axis.resetAxisMinimum()
                    axis.resetAxisMaximum()
                }
                return
            }

            val context = chart.context

            axis.isEnabled = true
            val textColor = ContextCompat.getColor(context, R.color.chart_text_color)
            axis.textColor = textColor
            axis.textSize = 10f
            axis.axisLineColor = ContextCompat.getColor(context, R.color.chart_axis_color)
            axis.axisLineWidth = 1.2f
            axis.setDrawAxisLine(true)
            axis.setDrawGridLines(drawGridLines)
            axis.gridColor = if (drawGridLines) {
                ContextCompat.getColor(context, R.color.chart_grid_color)
            } else {
                ContextCompat.getColor(context, android.R.color.transparent)
            }
            axis.gridLineWidth = if (drawGridLines) 0.6f else 0f
            axis.valueFormatter = buildAxisFormatter(config)

            val scale = config.scaleFactor
            val providedMin = config.min?.let { (it * scale).toFloat() }
            val providedMax = config.max?.let { (it * scale).toFloat() }

            val dataMin = series.minOfOrNull { it.dataSet.yMin }
            val dataMax = series.maxOfOrNull { it.dataSet.yMax }

            var minValue = providedMin ?: dataMin ?: 0f
            var maxValue = providedMax ?: dataMax ?: minValue + 1f

            if (config.forceZeroInRange) {
                minValue = kotlin.math.min(minValue, 0f)
                maxValue = kotlin.math.max(maxValue, 0f)
            }

            if (abs(maxValue - minValue) < 0.0001f) {
                maxValue = minValue + 1f
            }

            axis.axisMinimum = minValue
            axis.axisMaximum = maxValue

            config.labelCount?.let { axis.setLabelCount(it, true) }
            axis.setDrawLabels(!suppressLabels)
            if (suppressLabels) {
                axis.textColor = textColor
            }

            val drawZeroLine = config.forceZeroInRange && minValue <= 0f && maxValue >= 0f
            axis.setDrawZeroLine(drawZeroLine)
            if (drawZeroLine) {
                axis.zeroLineColor = ContextCompat.getColor(context, R.color.chart_zero_line_color)
                axis.zeroLineWidth = 1.5f
            }
        }

        private fun buildOverlayEntries(
            orderedRightConfigs: List<ChartAxisConfig>,
            seriesData: List<SeriesData>
        ): List<AxisOverlayEntry> {
            if (orderedRightConfigs.isEmpty()) return emptyList()

            return orderedRightConfigs
                .mapNotNull { axisConfig ->
                    val associatedSeries = seriesData.filter { it.axisConfig?.id == axisConfig.id }
                    if (associatedSeries.isEmpty()) return@mapNotNull null
                    val ticks = resolveTicks(axisConfig, associatedSeries)
                    if (ticks.isEmpty()) return@mapNotNull null
                    AxisOverlayEntry(axisConfig, ticks, YAxis.AxisDependency.RIGHT)
                }
        }

        private fun resolveTicks(axisConfig: ChartAxisConfig, series: List<SeriesData>): List<Double> {
            axisConfig.labelValues?.takeIf { it.isNotEmpty() }?.let { return it }

            val rawMin = series.mapNotNull { it.rawMin }.minOrNull()
            val rawMax = series.mapNotNull { it.rawMax }.maxOrNull()

            val min = axisConfig.min ?: rawMin
            val max = axisConfig.max ?: rawMax

            if (min == null || max == null) return emptyList()

            val count = axisConfig.labelCount ?: 5
            return generateTicks(min, max, count, axisConfig.forceZeroInRange)
        }

        private fun generateTicks(
            minValue: Double,
            maxValue: Double,
            count: Int,
            includeZero: Boolean
        ): List<Double> {
            if (count <= 0) return emptyList()

            var min = minValue
            var max = maxValue

            if (includeZero) {
                min = kotlin.math.min(min, 0.0)
                max = kotlin.math.max(max, 0.0)
            }

            if (abs(max - min) < 1e-6) {
                return listOf(min, max).distinct()
            }

            if (count == 1) return listOf(max)

            val step = (max - min) / (count - 1)
            return (0 until count).map { index -> min + step * index }
        }

        private fun buildAxisFormatter(config: ChartAxisConfig): ValueFormatter {
            val pattern = config.formatPattern.ifBlank { "#0.##" }
            val decimalFormat = DecimalFormat(pattern)
            val scaleFactor = if (config.scaleFactor == 0.0) 1.0 else config.scaleFactor

            return object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val actual = value.toDouble() / scaleFactor
                    return decimalFormat.format(actual)
                }
            }
        }

        private fun ChartConfig.axisFor(parameter: ChartParameter): ChartAxisConfig? {
            val targetId = parameter.axisId ?: return null
            return axes.firstOrNull { it.id == targetId }
        }

        private fun ChartConfig.scaleFactorFor(parameter: ChartParameter): Double {
            return axisFor(parameter)?.scaleFactor ?: parameter.scaleFactor
        }

        private fun configureXAxis(chart: LineChart, seriesData: List<SeriesData>) {
            val entries = seriesData.flatMap { it.dataSet.values }
            if (entries.isEmpty()) return

            val interval = THREE_HOURS_MS.toDouble()
            if (interval <= 0.0) return

            val minX = entries.minOf { it.x.toDouble() }
            val maxX = entries.maxOf { it.x.toDouble() }

            val minBucket = floor(minX / interval)
            val maxBucket = ceil(maxX / interval)

            val adjustedMin = (minBucket * interval).toFloat()
            val adjustedMax = (maxBucket * interval).toFloat()

            val stepCount = (maxBucket - minBucket).toInt().coerceAtLeast(1)
            val labelCount = (stepCount + 1).coerceAtMost(12)

            val range = (maxX - minX).coerceAtLeast(interval)
            val padding = (interval * 0.15).coerceAtMost(range * 0.1)
            val axisMin = (minX - padding).toFloat().coerceAtLeast(adjustedMin)
            val axisMax = (maxX + padding).toFloat().coerceAtMost(adjustedMax)

            chart.xAxis.apply {
                axisMinimum = axisMin
                axisMaximum = axisMax
                granularity = THREE_HOURS_MS
                isGranularityEnabled = true
                setLabelCount(labelCount, true)
                setAvoidFirstLastClipping(true)
            }
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
                isHighlightPerTapEnabled = true
                isHighlightPerDragEnabled = true
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
                    granularity = THREE_HOURS_MS
                    isGranularityEnabled = true
                    setAvoidFirstLastClipping(false)
                    labelRotationAngle = 0f
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
                }

                // Configurar eje Y derecho (para gráficos combinados)
                axisRight.apply {
                    val hasRightAxis = config.axes.any { it.position == AxisPosition.RIGHT }
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
                    verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                    orientation = Legend.LegendOrientation.HORIZONTAL
                    setDrawInside(false)
                    isWordWrapEnabled = true
                    xEntrySpace = 12f
                    yEntrySpace = 8f
                }

                // Márgenes base (se ajustan dinámicamente según overlay)
                setExtraOffsets(12f, 18f, 24f, 28f)

                setOnChartGestureListener(object : OnChartGestureListener {
                    override fun onChartSingleTapped(me: MotionEvent?) {
                        if (me == null) return
                        val highlight = getHighlightByTouchPoint(me.x, me.y)
                        if (highlight == null) {
                            clearHighlights()
                        }
                    }

                    override fun onChartLongPressed(me: MotionEvent?) {}

                    override fun onChartDoubleTapped(me: MotionEvent?) {}

                    override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {}

                    override fun onChartFling(
                        me1: MotionEvent?,
                        me2: MotionEvent?,
                        velocityX: Float,
                        velocityY: Float
                    ) {
                    }

                    override fun onChartGestureStart(
                        me: MotionEvent?,
                        lastPerformedGesture: ChartGesture?
                    ) {
                    }

                    override fun onChartGestureEnd(
                        me: MotionEvent?,
                        lastPerformedGesture: ChartGesture?
                    ) {
                    }

                    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {}
                })
            }

            axisOverlay.setOnClickListener { clearHighlights() }
            itemView.setOnClickListener { clearHighlights() }
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
    val axisConfig: ChartAxisConfig?,
    val dataSet: LineDataSet,
    val rawMin: Double?,
    val rawMax: Double?
)

