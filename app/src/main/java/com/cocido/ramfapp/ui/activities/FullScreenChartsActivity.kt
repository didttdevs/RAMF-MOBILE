package com.cocido.ramfapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.cocido.ramfapp.R
import com.cocido.ramfapp.databinding.ActivityFullScreenChartsBinding
import com.cocido.ramfapp.models.ChartCategory
import com.cocido.ramfapp.models.ChartConfig
import com.cocido.ramfapp.ui.adapters.MultiChartAdapter
import com.cocido.ramfapp.utils.ChartConfigFactory
import com.cocido.ramfapp.utils.ExportUtils
import com.cocido.ramfapp.viewmodels.GraphViewModel
import com.cocido.ramfapp.viewmodels.GraphViewModelFactory
import com.cocido.ramfapp.repository.WeatherRepository
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity profesional para mostrar múltiples gráficos meteorológicos
 * Fase 2: Layout vertical con scroll, gráficos combinados y filtros avanzados
 */
class FullScreenChartsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityFullScreenChartsBinding
    private lateinit var multiChartAdapter: MultiChartAdapter
    
    private val viewModel: GraphViewModel by viewModels {
        GraphViewModelFactory(WeatherRepository())
    }
    
    private var currentCharts: List<ChartConfig> = emptyList()
    private var selectedCategory: ChartCategory = ChartCategory.ALL
    
    companion object {
        const val EXTRA_STATION_ID = "station_id"
        const val EXTRA_STATION_NAME = "station_name"
        private const val TAG = "FullScreenCharts"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullScreenChartsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupChartSelector()
        setupCategoryFilters()
        setupRecyclerView()
        setupTimeFilters()
        setupExportButton()
        observeViewModel()
        loadInitialData()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = intent.getStringExtra(EXTRA_STATION_NAME) ?: "Gráficos"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupChartSelector() {
        val chartTypes = arrayOf(
            "Todos los gráficos",
            "Gráficos combinados",
            "Gráficos individuales",
            "Temperatura",
            "Humedad",
            "Viento",
            "Precipitación",
            "Presión",
            "Radiación"
        )
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, chartTypes)
        binding.chartSelectorDropdown.setAdapter(adapter)
        binding.chartSelectorDropdown.setText(chartTypes[0], false)
        
        binding.chartSelectorDropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedType = chartTypes[position]
            Log.d(TAG, "Chart type selected: $selectedType")
            updateChartsForType(selectedType)
        }
    }
    
    private fun setupCategoryFilters() {
        // Configurar chips individuales para mejor control
        binding.chipAllCategories.setOnClickListener {
            clearOtherChips(binding.chipAllCategories)
            binding.chipAllCategories.isChecked = true
            Log.d(TAG, "Category filter selected: ALL")
            selectedCategory = ChartCategory.ALL
            updateChartsForCategory(ChartCategory.ALL)
        }
        
        binding.chipTemperature.setOnClickListener {
            clearOtherChips(binding.chipTemperature)
            binding.chipTemperature.isChecked = true
            Log.d(TAG, "Category filter selected: TEMPERATURE")
            selectedCategory = ChartCategory.TEMPERATURE
            updateChartsForCategory(ChartCategory.TEMPERATURE)
        }
        
        binding.chipHumidity.setOnClickListener {
            clearOtherChips(binding.chipHumidity)
            binding.chipHumidity.isChecked = true
            Log.d(TAG, "Category filter selected: HUMIDITY")
            selectedCategory = ChartCategory.HUMIDITY
            updateChartsForCategory(ChartCategory.HUMIDITY)
        }
        
        binding.chipWind.setOnClickListener {
            clearOtherChips(binding.chipWind)
            binding.chipWind.isChecked = true
            Log.d(TAG, "Category filter selected: WIND")
            selectedCategory = ChartCategory.WIND
            updateChartsForCategory(ChartCategory.WIND)
        }
        
        binding.chipPrecipitation.setOnClickListener {
            clearOtherChips(binding.chipPrecipitation)
            binding.chipPrecipitation.isChecked = true
            Log.d(TAG, "Category filter selected: PRECIPITATION")
            selectedCategory = ChartCategory.PRECIPITATION
            updateChartsForCategory(ChartCategory.PRECIPITATION)
        }
        
        binding.chipPressure.setOnClickListener {
            clearOtherChips(binding.chipPressure)
            binding.chipPressure.isChecked = true
            Log.d(TAG, "Category filter selected: PRESSURE")
            selectedCategory = ChartCategory.PRESSURE
            updateChartsForCategory(ChartCategory.PRESSURE)
        }
        
        binding.chipRadiation.setOnClickListener {
            clearOtherChips(binding.chipRadiation)
            binding.chipRadiation.isChecked = true
            Log.d(TAG, "Category filter selected: RADIATION")
            selectedCategory = ChartCategory.RADIATION
            updateChartsForCategory(ChartCategory.RADIATION)
        }
        
        // Seleccionar "Todos" por defecto
        binding.chipAllCategories.isChecked = true
    }
    
    private fun clearOtherChips(selectedChip: com.google.android.material.chip.Chip) {
        binding.chipAllCategories.isChecked = false
        binding.chipTemperature.isChecked = false
        binding.chipHumidity.isChecked = false
        binding.chipWind.isChecked = false
        binding.chipPrecipitation.isChecked = false
        binding.chipPressure.isChecked = false
        binding.chipRadiation.isChecked = false
    }
    
    private fun setupRecyclerView() {
        currentCharts = ChartConfigFactory.getAllCharts()
        multiChartAdapter = MultiChartAdapter(currentCharts, emptyList())
        
        binding.chartsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@FullScreenChartsActivity)
            adapter = multiChartAdapter
            setHasFixedSize(false)
        }
        
        // Configurar listener para opciones de gráficos
        multiChartAdapter.setOnChartOptionsClickListener { chartConfig ->
            showChartOptions(chartConfig)
        }
    }
    
    private fun setupTimeFilters() {
        // Configurar header expandible
        setupExpandableFilters()
        
        // Configurar selectores de fecha
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        
        binding.fromDateEditText.setOnClickListener {
            showDateTimePicker { selectedDate ->
                binding.fromDateEditText.setText(dateFormatter.format(selectedDate))
            }
        }
        
        binding.toDateEditText.setOnClickListener {
            showDateTimePicker { selectedDate ->
                binding.toDateEditText.setText(dateFormatter.format(selectedDate))
            }
        }
        
        // Configurar chips de filtros rápidos
        binding.chipGroupTimeRange.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val (from, to) = when (checkedIds[0]) {
                    R.id.chip1Hour -> generateTimeRange(1, Calendar.HOUR_OF_DAY)
                    R.id.chip6Hours -> generateTimeRange(6, Calendar.HOUR_OF_DAY)
                    R.id.chip1Day -> generateTimeRange(1, Calendar.DAY_OF_YEAR)
                    R.id.chip1Week -> generateTimeRange(7, Calendar.DAY_OF_YEAR)
                    R.id.chip1Month -> generateTimeRange(30, Calendar.DAY_OF_YEAR)
                    else -> generateTimeRange(1, Calendar.DAY_OF_YEAR)
                }
                
                binding.fromDateEditText.setText(dateFormatter.format(Date(from)))
                binding.toDateEditText.setText(dateFormatter.format(Date(to)))
                
                applyTimeFilter(from, to)
            }
        }
        
        // Configurar botón buscar
        binding.searchButton.setOnClickListener {
            applyCustomDateFilter()
        }
    }
    
    private fun setupExportButton() {
        binding.fabExport.setOnClickListener {
            exportCurrentData()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    if (state.errorMessage != null) {
                        showError(state.errorMessage)
                    }
                    
                    if (state.weatherData.isNotEmpty()) {
                        Log.d(TAG, "Updating charts with ${state.weatherData.size} data points")
                        multiChartAdapter.updateData(state.weatherData)
                    }
                }
            }
        }
    }
    
    private fun loadInitialData() {
        val stationId = intent.getStringExtra(EXTRA_STATION_ID)
        val stationName = intent.getStringExtra(EXTRA_STATION_NAME)
        
        if (stationId != null) {
            viewModel.selectStation(stationId)
            
            // Cargar datos de las últimas 24 horas
            val (from, to) = generateLast24HoursRange()
            Log.d(TAG, "Loading data for station: $stationName (ID: $stationId)")
            viewModel.loadWeatherData(from, to)
        } else {
            showError("Error: No se especificó la estación para mostrar")
        }
    }
    
    private fun updateChartsForType(type: String) {
        currentCharts = ChartConfigFactory.getChartsByType(type)
        multiChartAdapter.updateCharts(currentCharts)
        Log.d(TAG, "📊 Updated charts for TYPE '$type':")
        currentCharts.forEach { chart ->
            Log.d(TAG, "   - ${chart.title} (${chart.category})")
        }
    }
    
    private fun updateChartsForCategory(category: ChartCategory) {
        currentCharts = ChartConfigFactory.getChartsByCategory(category)
        multiChartAdapter.updateCharts(currentCharts)
        Log.d(TAG, "🏷️ Updated charts for CATEGORY '$category':")
        currentCharts.forEach { chart ->
            Log.d(TAG, "   - ${chart.title} (${chart.category})")
        }
    }
    
    private fun showChartOptions(chartConfig: ChartConfig) {
        // TODO: Implementar menú de opciones (exportar solo este gráfico, etc.)
        Snackbar.make(
            binding.root,
            "Opciones para: ${chartConfig.title}",
            Snackbar.LENGTH_SHORT
        ).show()
    }
    
    private fun exportCurrentData() {
        val stationName = intent.getStringExtra(EXTRA_STATION_NAME) ?: "Estación"
        val weatherData = viewModel.uiState.value.weatherData
        
        if (weatherData.isEmpty()) {
            Snackbar.make(
                binding.root,
                "No hay datos para exportar",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                val result = ExportUtils.exportWeatherDataToCsv(
                    context = this@FullScreenChartsActivity,
                    weatherData = weatherData,
                    stationName = stationName,
                    dateRange = null
                )
                
                when (result) {
                    is ExportUtils.ExportResult.Success -> {
                        ExportUtils.shareFile(this@FullScreenChartsActivity, result.file)
                        
                        Snackbar.make(
                            binding.root,
                            "Datos exportados exitosamente",
                            Snackbar.LENGTH_LONG
                        ).setAction("Ver") {
                            ExportUtils.shareFile(this@FullScreenChartsActivity, result.file)
                        }.show()
                    }
                    is ExportUtils.ExportResult.Error -> {
                        showError("Error al exportar: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error exporting data", e)
                showError("Error al exportar datos: ${e.message}")
            }
        }
    }
    
    private fun setupExpandableFilters() {
        binding.filtersHeader.setOnClickListener {
            val isVisible = binding.filtersContent.visibility == View.VISIBLE
            
            if (isVisible) {
                binding.filtersContent.visibility = View.GONE
                rotateIcon(binding.expandIcon, 180f, 0f)
            } else {
                binding.filtersContent.visibility = View.VISIBLE
                rotateIcon(binding.expandIcon, 0f, 180f)
            }
        }
    }
    
    private fun rotateIcon(imageView: View, fromDegrees: Float, toDegrees: Float) {
        val rotateAnimation = RotateAnimation(
            fromDegrees, toDegrees,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 200
            fillAfter = true
        }
        imageView.startAnimation(rotateAnimation)
    }
    
    private fun showDateTimePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        
        android.app.DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                android.app.TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(year, month, dayOfMonth, hourOfDay, minute, 0)
                        onDateSelected(calendar.time)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    
    private fun generateTimeRange(amount: Int, field: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(field, -amount)
        val startTime = calendar.timeInMillis
        return Pair(startTime, endTime)
    }
    
    private fun generateLast24HoursRange(): Pair<String, String> {
        val calendar = Calendar.getInstance()
        val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        
        val endDate = calendar.time
        calendar.add(Calendar.HOUR_OF_DAY, -24)
        val startDate = calendar.time
        
        return Pair(isoFormatter.format(startDate), isoFormatter.format(endDate))
    }
    
    private fun applyTimeFilter(fromMillis: Long, toMillis: Long) {
        val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        
        val fromIso = isoFormatter.format(Date(fromMillis))
        val toIso = isoFormatter.format(Date(toMillis))
        
        Log.d(TAG, "🕒 Applying time filter:")
        Log.d(TAG, "   From: $fromIso")
        Log.d(TAG, "   To: $toIso")
        Log.d(TAG, "   Station ID: ${viewModel.uiState.value.currentStationId}")
        
        viewModel.loadWeatherData(fromIso, toIso)
    }
    
    private fun applyCustomDateFilter() {
        val fromText = binding.fromDateEditText.text.toString()
        val toText = binding.toDateEditText.text.toString()
        
        if (fromText.isNotEmpty() && toText.isNotEmpty()) {
            try {
                val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val fromDate = dateFormatter.parse(fromText)
                val toDate = dateFormatter.parse(toText)
                
                if (fromDate != null && toDate != null) {
                    applyTimeFilter(fromDate.time, toDate.time)
                }
            } catch (e: Exception) {
                showError("Error en formato de fecha. Use: dd/MM/yyyy HH:mm")
            }
        } else {
            showError("Por favor seleccione fechas de inicio y fin")
        }
    }
    
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
