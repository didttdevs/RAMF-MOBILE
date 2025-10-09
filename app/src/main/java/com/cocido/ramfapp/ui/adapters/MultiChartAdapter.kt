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
import com.cocido.ramfapp.models.ChartConfig
import com.cocido.ramfapp.models.ChartParameter
import com.cocido.ramfapp.models.WeatherData
import com.cocido.ramfapp.models.ChartsPayload
import com.cocido.ramfapp.models.ChartPoint
import com.cocido.ramfapp.models.ChartCategory
import com.cocido.ramfapp.utils.ChartUtils
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

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

            // Mapear datos y crear datasets
            val dataSets = chartConfig.parameters.mapNotNull { parameter ->
                val entries = data.mapNotNull { weatherData ->
                    val timestamp = parseTimestamp(weatherData.date)
                    val value = parameter.valueExtractor(weatherData)

                    if (timestamp != null && value != null) {
                        Entry(timestamp.toFloat(), value.toFloat())
                    } else null
                }.sortedBy { it.x }

                if (entries.isNotEmpty()) {
                    createLineDataSet(entries, parameter.label, parameter.color, parameter.unit)
                } else null
            }

            if (dataSets.isNotEmpty()) {
                val lineData = LineData(dataSets)
                lineChart.data = lineData
                
                // Configurar escalas independientes para gráficos combinados
                if (chartConfig.parameters.size > 1) {
                    setupIndependentScales(lineChart, dataSets, chartConfig.parameters)
                }
                
                lineChart.animateX(800)
                lineChart.invalidate()

                // Actualizar estadísticas (usa el primer parámetro)
                updateStats(dataSets.firstOrNull(), chartConfig.parameters.firstOrNull()?.unit ?: "")
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

            // Configurar listener de opciones
            chartOptionsButton.setOnClickListener {
                onChartOptionsClickListener?.invoke(chartConfig)
            }

            // Configurar gráfico
            setupChart(lineChart, chartConfig)

            // Obtener datos del grupo correspondiente del backend
            // El backend agrupa por tipo de sensor, no por categoría de gráfico
            val groupData = when {
                // Temperatura y Humedad usan el mismo grupo del backend
                chartConfig.category == ChartCategory.TEMPERATURE -> chartsData.charts.tempHum
                chartConfig.category == ChartCategory.HUMIDITY -> chartsData.charts.tempHum
                chartConfig.category == ChartCategory.COMBINED -> {
                    // Para gráficos COMBINED, determinar el grupo por el ID del gráfico
                    when (chartConfig.id) {
                        "radiation" -> chartsData.charts.radiacion
                        else -> chartsData.charts.tempHum
                    }
                }
                
                // Viento
                chartConfig.category == ChartCategory.WIND -> chartsData.charts.viento
                
                // Presión
                chartConfig.category == ChartCategory.PRESSURE -> chartsData.charts.presion
                
                // Precipitación (el backend devuelve datos en el grupo lluvia)
                chartConfig.category == ChartCategory.PRECIPITATION -> {
                    // Primero intentar lluvia, si no hay datos usar tempHum para Delta T
                    chartsData.charts.lluvia ?: chartsData.charts.tempHum
                }
                
                else -> null
            }

            if (groupData != null && groupData.isNotEmpty()) {
                // Crear datasets basados en los datos del backend
                val dataSets = chartConfig.parameters.mapNotNull { parameter ->
                    val entries = groupData.mapNotNull { chartPoint ->
                        val timestamp = parseBackendTimestamp(chartPoint.date)
                        val value = extractValueFromChartPoint(chartPoint, parameter.key)

                        if (timestamp != null && value != null) {
                            Entry(timestamp.toFloat(), value.toFloat())
                        } else null
                    }.sortedBy { it.x }

                    if (entries.isNotEmpty()) {
                        createLineDataSet(entries, parameter.label, parameter.color, parameter.unit)
                    } else null
                }

                if (dataSets.isNotEmpty()) {
                    val lineData = LineData(dataSets)
                    lineChart.data = lineData
                    
                    // Configurar escalas independientes para gráficos combinados
                    if (chartConfig.parameters.size > 1) {
                        setupIndependentScales(lineChart, dataSets, chartConfig.parameters)
                    }
                    
                    lineChart.animateX(800)
                    lineChart.invalidate()

                    // Actualizar estadísticas
                    updateStats(dataSets.firstOrNull(), chartConfig.parameters.firstOrNull()?.unit ?: "")
                } else {
                    lineChart.clear()
                    currentValue.text = "--"
                    minValue.text = "--"
                    maxValue.text = "--"
                    avgValue.text = "--"
                }
            } else {
                lineChart.clear()
                currentValue.text = "--"
                minValue.text = "--"
                maxValue.text = "--"
                avgValue.text = "--"
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

        private fun extractValueFromChartPoint(chartPoint: ChartPoint, parameterKey: String): Double? {
            return when (parameterKey.lowercase()) {
                "temperatura" -> chartPoint.temperatura
                "humedad" -> chartPoint.humedad
                "dewpoint", "punto_rocio" -> chartPoint.puntoRocio
                "vpd" -> chartPoint.vpd
                "deltat", "delta_t" -> chartPoint.deltaT
                "radiacion", "radiation" -> chartPoint.radiacionSolar
                "viento", "wind" -> chartPoint.velocidadViento
                "windgust", "rafaga" -> chartPoint.rafaga
                "winddir", "direccion_viento" -> chartPoint.direccionViento
                "presion", "pressure" -> chartPoint.presion
                "precipitacion", "rain" -> chartPoint.precipitacion
                "bateria", "battery" -> chartPoint.bateria
                "panel_solar", "solar_panel" -> chartPoint.panelSolar
                "et0" -> chartPoint.et0
                "horas_sol", "sunshine" -> chartPoint.horasSol
                "orientacion", "orientation" -> chartPoint.orientacion
                else -> null
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
                    if (config.parameters.size > 1) {
                        isEnabled = true
                        textColor = ContextCompat.getColor(context, R.color.chart_text_color)
                        textSize = 10f
                        setDrawGridLines(false)
                        setDrawAxisLine(true)
                        axisLineColor = ContextCompat.getColor(context, R.color.chart_axis_color)
                        axisLineWidth = 1f
                    } else {
                        isEnabled = false
                    }
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

        private fun createLineDataSet(
            entries: List<Entry>,
            label: String,
            color: Int,
            unit: String
        ): LineDataSet {
            return LineDataSet(entries, "$label ($unit)").apply {
                this.color = color
                setCircleColor(color)
                lineWidth = 2.5f
                circleRadius = 3f
                setDrawCircleHole(false)
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                cubicIntensity = 0.2f
                setDrawFilled(false)
                setDrawHighlightIndicators(true)
                highLightColor = color
                highlightLineWidth = 1.5f
            }
        }

        private fun setupIndependentScales(chart: LineChart, dataSets: List<LineDataSet>, parameters: List<ChartParameter>) {
            if (dataSets.size < 2) return
            
            // Calcular rangos para cada dataset
            val scales = dataSets.mapIndexed { index, dataSet ->
                val values = (0 until dataSet.entryCount).map { dataSet.getEntryForIndex(it).y }
                val min = values.minOrNull() ?: 0f
                val max = values.maxOrNull() ?: 0f
                val padding = (max - min) * 0.1f // 10% de padding
                
                ScaleInfo(
                    min = min - padding,
                    max = max + padding,
                    parameter = parameters[index]
                )
            }
            
            // Configurar eje izquierdo con el primer parámetro
            chart.axisLeft.apply {
                val firstScale = scales[0]
                axisMinimum = firstScale.min
                axisMaximum = firstScale.max
                setLabelCount(6, true)
                textColor = firstScale.parameter.color
                setDrawGridLines(true)
            }
            
            // Configurar eje derecho con el segundo parámetro (si existe)
            if (scales.size >= 2) {
                chart.axisRight.apply {
                    isEnabled = true
                    val secondScale = scales[1]
                    axisMinimum = secondScale.min
                    axisMaximum = secondScale.max
                    setLabelCount(6, true)
                    textColor = secondScale.parameter.color
                    setDrawGridLines(false) // No líneas de cuadrícula para eje derecho
                }
            }
            
            // Para más de 2 parámetros, usar colores y estilos diferentes
            if (scales.size > 2) {
                Log.w("MultiChartAdapter", "Gráfico con ${scales.size} parámetros - usando escalas automáticas")
                
                // Configurar estilos de línea diferentes para distinguir parámetros
                dataSets.forEachIndexed { index, dataSet ->
                    when (index) {
                        0 -> {
                            // Primer parámetro: línea sólida, eje izquierdo
                            dataSet.lineWidth = 3f
                            dataSet.setDrawCircles(false)
                            dataSet.axisDependency = YAxis.AxisDependency.LEFT
                        }
                        1 -> {
                            // Segundo parámetro: línea sólida, eje derecho
                            dataSet.lineWidth = 3f
                            dataSet.setDrawCircles(false)
                            dataSet.axisDependency = YAxis.AxisDependency.RIGHT
                        }
                        2 -> {
                            // Tercer parámetro: línea punteada, eje derecho
                            dataSet.lineWidth = 2f
                            dataSet.enableDashedLine(10f, 5f, 0f)
                            dataSet.setDrawCircles(false)
                            dataSet.axisDependency = YAxis.AxisDependency.RIGHT
                        }
                        3 -> {
                            // Cuarto parámetro: línea con puntos, eje derecho
                            dataSet.lineWidth = 2f
                            dataSet.setDrawCircles(true)
                            dataSet.circleRadius = 4f
                            dataSet.axisDependency = YAxis.AxisDependency.RIGHT
                        }
                    }
                }
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

            val values = (0 until dataSet.entryCount).map { dataSet.getEntryForIndex(it).y }
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

private data class ScaleInfo(
    val min: Float,
    val max: Float,
    val parameter: ChartParameter
)

