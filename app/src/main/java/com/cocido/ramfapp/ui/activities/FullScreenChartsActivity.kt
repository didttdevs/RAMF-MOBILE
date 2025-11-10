package com.cocido.ramfapp.ui.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.cocido.ramfapp.R
import com.cocido.ramfapp.databinding.ActivityFullScreenChartsBinding
import com.cocido.ramfapp.models.ChartCategory
import com.cocido.ramfapp.models.ChartConfig
import com.cocido.ramfapp.repository.WeatherRepository
import com.cocido.ramfapp.ui.adapters.MultiChartAdapter
import com.cocido.ramfapp.utils.ChartConfigFactory
import com.cocido.ramfapp.utils.ExportUtils
import com.cocido.ramfapp.viewmodels.GraphViewModel
import com.cocido.ramfapp.viewmodels.GraphViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity profesional para mostrar múltiples gráficos meteorológicos
 * Fase 2: Layout vertical con scroll, gráficos combinados y filtros avanzados
 */
class FullScreenChartsActivity : BaseActivity() {
    
    private lateinit var binding: ActivityFullScreenChartsBinding
    private lateinit var multiChartAdapter: MultiChartAdapter
    private var pendingExportFile: File? = null
    
    private val viewModel: GraphViewModel by viewModels {
        GraphViewModelFactory(WeatherRepository())
    }
    
    private var currentCharts: List<ChartConfig> = emptyList()
    private var selectedCategory: ChartCategory = ChartCategory.ALL
    private lateinit var createDocumentLauncher: ActivityResultLauncher<String>
    
    companion object {
        const val EXTRA_STATION_ID = "station_id"
        const val EXTRA_STATION_NAME = "station_name"
        private const val TAG = "FullScreenCharts"
    }
    
    override fun requiresAuthentication(): Boolean {
        return true
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullScreenChartsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupChartSelector()
        setupRecyclerView()
        setupExportButton()
        setupCreateDocumentLauncher()
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
            "Todos",
            "Temp/Humedad",
            "Radiación",
            "Precipitación",
            "Viento",
            "Evapotranspiración",
            "Presión"
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
    
    
    private fun setupRecyclerView() {
        currentCharts = ChartConfigFactory.getAllCharts()
        multiChartAdapter = MultiChartAdapter(currentCharts, emptyList(), null)
        
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
    
    
    private fun setupExportButton() {
        binding.fabExport.setOnClickListener {
            exportCurrentData()
        }
    }

    private fun setupCreateDocumentLauncher() {
        createDocumentLauncher = registerForActivityResult(
            ActivityResultContracts.CreateDocument(ExportUtils.ExportFormat.CSV.mimeType)
        ) { uri ->
            if (uri == null) {
                pendingExportFile = null
                return@registerForActivityResult
            }

            val sourceFile = pendingExportFile ?: return@registerForActivityResult

            lifecycleScope.launch {
                try {
                    copyFileToUri(sourceFile, uri)
                    Snackbar.make(
                        binding.root,
                        "Archivo guardado correctamente",
                        Snackbar.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    Log.e(TAG, "Error writing file to selected location", e)
                    showError("No se pudo guardar el archivo: ${e.message}")
                } finally {
                    pendingExportFile = null
                }
            }
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                viewModel.uiState.collectLatest { state ->
                    if (state.errorMessage != null) {
                        showError(state.errorMessage)
                        }
                        
                        // Usar datos de gráficos del backend si están disponibles
                        if (state.chartsData != null) {
                            Log.d(TAG, "Updating charts with backend data")
                            Log.d(TAG, "   tempHum points: ${state.chartsData.charts.tempHum?.size ?: 0}")
                            Log.d(TAG, "   viento points: ${state.chartsData.charts.viento?.size ?: 0}")
                            Log.d(TAG, "   presion points: ${state.chartsData.charts.presion?.size ?: 0}")
                            multiChartAdapter.updateChartsData(state.chartsData)
                        } else if (state.weatherData.isNotEmpty()) {
                            Log.d(TAG, "Updating charts with legacy data: ${state.weatherData.size} points")
                            multiChartAdapter.updateData(state.weatherData)
                        }
                    }
                }
                
                // Observer para sesión expirada
                launch {
                    viewModel.navigateToLogin.collectLatest { navigate ->
                        if (navigate) {
                            Log.d(TAG, "Received navigateToLogin event. Redirecting to LoginActivity.")
                            val intent = Intent(this@FullScreenChartsActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
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

            // Cargar datos de las últimas 24 horas (como en la web)
            val (from, to) = generateLast24HoursRange()
            
            Log.d(TAG, "🚀 Loading last 24h data for station: $stationName (ID: $stationId)")
            Log.d(TAG, "   From: $from")
            Log.d(TAG, "   To: $to")
            
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
    
    
    private fun showChartOptions(chartConfig: ChartConfig) {
        Snackbar.make(
            binding.root,
            "Opciones para: ${chartConfig.title}",
            Snackbar.LENGTH_SHORT
        ).show()
    }
    
    private fun exportCurrentData() {
        val stationName = intent.getStringExtra(EXTRA_STATION_NAME) ?: "Estación"
        val uiState = viewModel.uiState.value
        val chartsData = uiState.chartsData
        val weatherData = uiState.weatherData

        lifecycleScope.launch {
            try {
                val result = when {
                    chartsData != null -> ExportUtils.exportChartsDataToCsv(
                        context = this@FullScreenChartsActivity,
                        chartsPayload = chartsData,
                        stationName = stationName,
                        dateRange = uiState.dateRangeLabel
                    )
                    weatherData.isNotEmpty() -> ExportUtils.exportWeatherDataToCsv(
                        context = this@FullScreenChartsActivity,
                        weatherData = weatherData,
                        stationName = stationName,
                        dateRange = uiState.dateRangeLabel
                    )
                    else -> {
                        Snackbar.make(
                            binding.root,
                            "No hay datos para exportar",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        return@launch
                    }
                }

                when (result) {
                    is ExportUtils.ExportResult.Success -> {
                        pendingExportFile = result.file
                        showExportOptionsSheet(result.file, result.uri)
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
    
    /**
     * Genera el rango de fechas para las últimas 24 horas
     * (coincide con el comportamiento de la página web)
     */
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
    
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showExportOptionsSheet(file: File, uri: Uri) {
        val dialog = BottomSheetDialog(this)
        val sheetBinding = com.cocido.ramfapp.databinding.SheetExportOptionsBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        sheetBinding.optionShareGeneral.setOnClickListener {
            dialog.dismiss()
            ExportUtils.shareFile(this, file, uri)
        }

        sheetBinding.optionShareWhatsapp.setOnClickListener {
            dialog.dismiss()
            ExportUtils.shareFile(
                context = this,
                file = file,
                uri = uri,
                targetPackage = ExportUtils.WHATSAPP_PACKAGE
            )
        }

        sheetBinding.optionSaveDevice.setOnClickListener {
            dialog.dismiss()
            val suggestedName = file.name
            createDocumentLauncher.launch(suggestedName)
        }

        dialog.show()
    }

    private fun copyFileToUri(source: File, destinationUri: Uri) {
        contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
            FileInputStream(source).use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw IllegalStateException("No se pudo abrir destino para escribir")
    }
}
