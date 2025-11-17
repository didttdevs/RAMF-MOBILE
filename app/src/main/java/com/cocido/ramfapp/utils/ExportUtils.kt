package com.cocido.ramfapp.utils

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.cocido.ramfapp.BuildConfig
import com.cocido.ramfapp.models.ChartDataGroup
import com.cocido.ramfapp.models.ChartPoint
import com.cocido.ramfapp.models.ChartsPayload
import com.cocido.ramfapp.models.WeatherData
import com.cocido.ramfapp.models.WeatherStation
import com.cocido.ramfapp.models.WidgetData
import com.cocido.ramfapp.models.getPoints
import com.cocido.ramfapp.ui.components.showErrorMessage
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utilidades profesionales para exportación de datos
 * Soporta CSV, compartir y gestión de archivos
 */
object ExportUtils {

    private const val TAG = "ExportUtils"
    private const val EXPORT_DIRECTORY = "RAF_Exports"
    private val decimalFormat = DecimalFormat("0.####", DecimalFormatSymbols(Locale.US))

    private data class SeriesDefinition(
        val label: String,
        val extractor: (ChartPoint) -> Double?
    )

    private val GROUP_SERIES_DEFINITIONS = mapOf(
        ChartDataGroup.RADIACION to listOf(
            SeriesDefinition("Radiación solar (W/m²)") { it.radiacionSolar }
        ),
        ChartDataGroup.ENERGIA to listOf(
            SeriesDefinition("Panel solar (mV)") { it.panelSolar },
            SeriesDefinition("Batería (mV)") { it.bateria }
        ),
        ChartDataGroup.LLUVIA to listOf(
            SeriesDefinition("Precipitación (mm)") { it.precipitacion }
        ),
        ChartDataGroup.TEMP_HUM to listOf(
            SeriesDefinition("Temp. aire (°C)") { it.temperatura },
            SeriesDefinition("Humedad relativa (%)") { it.humedad },
            SeriesDefinition("Punto de rocío (°C)") { it.puntoRocio },
            SeriesDefinition("VPD (kPa)") { it.vpd },
            SeriesDefinition("Delta T (°C)") { it.deltaT }
        ),
        ChartDataGroup.VIENTO to listOf(
            SeriesDefinition("Vel. viento (m/s)") { it.velocidadViento },
            SeriesDefinition("Ráfaga (m/s)") { it.rafaga }
        ),
        ChartDataGroup.DIRECCION to listOf(
            SeriesDefinition("Dir. viento (°)") { it.direccionViento },
            SeriesDefinition("Orientación (°)") { it.orientacion }
        ),
        ChartDataGroup.PRESION to listOf(
            SeriesDefinition("Presión (hPa)") { it.presion }
        ),
        ChartDataGroup.ET0 to listOf(
            SeriesDefinition("ET0 (mm)") { it.et0 }
        )
    )
    
    /**
     * Formatos de exportación soportados
     */
    enum class ExportFormat(val extension: String, val mimeType: String) {
        CSV("csv", "text/csv"),
        JSON("json", "application/json"),
        TXT("txt", "text/plain")
    }

    /**
     * Resultado de exportación
     */
    sealed class ExportResult {
        data class Success(val file: File, val uri: Uri) : ExportResult()
        data class Error(val message: String, val exception: Exception? = null) : ExportResult()
    }

    const val WHATSAPP_PACKAGE = "com.whatsapp"

    /**
     * Exporta datos meteorológicos a CSV
     */
    fun exportWeatherDataToCsv(
        context: Context,
        weatherData: List<WeatherData>,
        stationName: String,
        dateRange: String? = null
    ): ExportResult {
        return try {
            val fileName = generateFileName(stationName, "weather_data", ExportFormat.CSV, dateRange)
            val file = createExportFile(context, fileName)

            FileWriter(file).use { writer ->
                // Header
                writer.append("Fecha,Temperatura (°C),Humedad (%),Presión (hPa),Viento (m/s),Dirección Viento,Radiación (W/m²),Precipitación (mm)\n")

                // Data rows
                weatherData.forEach { data ->
                    writer.append(
                        "${data.date}," +
                        "${data.sensors.hcAirTemperature?.avg ?: ""}," +
                        "${data.sensors?.hcRelativeHumidity?.avg ?: ""}," +
                        "${data.sensors?.airPressure?.avg ?: ""}," +
                        "${data.sensors?.usonicWindSpeed?.avg ?: ""}," +
                        "${data.sensors?.usonicWindDir?.last ?: ""}," +
                        "${data.sensors?.solarRadiation?.avg ?: ""}," +
                        "${data.sensors?.precipitation?.sum ?: ""}\n"
                    )
                }
            }

            val uri = getFileUri(context, file)
            Log.d(TAG, "CSV exported successfully: ${file.absolutePath}")
            ExportResult.Success(file, uri)

        } catch (e: IOException) {
            Log.e(TAG, "Error exporting to CSV", e)
            ExportResult.Error("Error al exportar datos: ${e.message}", e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during export", e)
            ExportResult.Error("Error inesperado: ${e.message}", e)
        }
    }

    /**
     * Exporta datos de widget a CSV simple
     */
    fun exportWidgetDataToCsv(
        context: Context,
        widgetData: WidgetData,
        stationName: String
    ): ExportResult {
        return try {
            val fileName = generateFileName(stationName, "current_data", ExportFormat.CSV)
            val file = createExportFile(context, fileName)

            FileWriter(file).use { writer ->
                writer.append("Parámetro,Valor,Unidad\n")
                writer.append("Estación,$stationName,\n")
                writer.append("Fecha,${widgetData.timestamp},\n")
                writer.append("Temperatura,${widgetData.temperature},°C\n")
                writer.append("Temperatura Máxima,${widgetData.maxTemperature},°C\n")
                writer.append("Temperatura Mínima,${widgetData.minTemperature},°C\n")
                writer.append("Humedad Relativa,${widgetData.relativeHumidity},%\n")
                writer.append("Punto de Rocío,${widgetData.dewPoint},°C\n")
                writer.append("Presión Atmosférica,${widgetData.airPressure},hPa\n")
                writer.append("Radiación Solar,${widgetData.solarRadiation},W/m²\n")
                writer.append("Velocidad del Viento,${widgetData.windSpeed},m/s\n")
                writer.append("Dirección del Viento,${widgetData.windDirection},°\n")
                writer.append("Precipitación Última Hora,${widgetData.rainLastHour},mm\n")
                writer.append("Precipitación Día,${widgetData.rainDay},mm\n")
                writer.append("Precipitación 24h,${widgetData.rain24h},mm\n")
                writer.append("Precipitación 48h,${widgetData.rain48h},mm\n")
                writer.append("Precipitación 7 días,${widgetData.rain7d},mm\n")
            }

            val uri = getFileUri(context, file)
            Log.d(TAG, "Widget data exported successfully: ${file.absolutePath}")
            ExportResult.Success(file, uri)

        } catch (e: Exception) {
            Log.e(TAG, "Error exporting widget data", e)
            ExportResult.Error("Error al exportar datos: ${e.message}", e)
        }
    }

    /**
     * Exporta lista de estaciones a CSV
     */
    fun exportStationsToCsv(
        context: Context,
        stations: List<WeatherStation>
    ): ExportResult {
        return try {
            val fileName = generateFileName("estaciones", "list", ExportFormat.CSV)
            val file = createExportFile(context, fileName)

            FileWriter(file).use { writer ->
                writer.append("ID,Nombre,Ubicación,Latitud,Longitud,Altitud,Estado,Última Comunicación\n")

                stations.forEach { station ->
                    writer.append(
                        "${station.id}," +
                        "\"${station.name}\"," +
                        "\"${station.name}\"," +
                        "${station.position?.coordinates?.get(1) ?: ""}," +
                        "${station.position?.coordinates?.get(0) ?: ""}," +
                        "," + // altitude
                        "," + // status
                        "\"${station.lastCommunication ?: ""}\"\n"
                    )
                }
            }

            val uri = getFileUri(context, file)
            Log.d(TAG, "Stations exported successfully: ${file.absolutePath}")
            ExportResult.Success(file, uri)

        } catch (e: Exception) {
            Log.e(TAG, "Error exporting stations", e)
            ExportResult.Error("Error al exportar estaciones: ${e.message}", e)
        }
    }

    /**
     * Exporta los datos de gráficos combinados del backend a CSV (formato equivalente a la web).
     */
    fun exportChartsDataToCsv(
        context: Context,
        chartsPayload: ChartsPayload,
        stationName: String,
        dateRange: String? = null
    ): ExportResult {
        return try {
            val fileName = generateFileName(
                stationName = stationName,
                dataType = "charts",
                format = ExportFormat.CSV,
                dateRange = dateRange,
                useFriendlyName = true
            )
            val file = createExportFile(context, fileName)

            FileWriter(file).use { writer ->
                writer.append("group,serie,date,value\n")

                ChartDataGroup.values().forEach { group ->
                    val points = chartsPayload.charts.getPoints(group) ?: return@forEach
                    val seriesDefinitions = GROUP_SERIES_DEFINITIONS[group] ?: return@forEach

                    points.forEach { point ->
                        seriesDefinitions.forEach { definition ->
                            val value = definition.extractor(point) ?: return@forEach
                            writer.append(
                                "${group.name.lowercase()},${definition.label},${point.date},${formatValue(value)}\n"
                            )
                        }
                    }
                }
            }

            val uri = getFileUri(context, file)
            Log.d(TAG, "Charts data exported successfully: ${file.absolutePath}")
            ExportResult.Success(file, uri)

        } catch (e: IOException) {
            Log.e(TAG, "Error exporting charts data", e)
            ExportResult.Error("Error al exportar datos: ${e.message}", e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during charts export", e)
            ExportResult.Error("Error inesperado: ${e.message}", e)
        }
    }

    /**
     * Comparte un archivo mediante Intent
     */
    fun shareFile(
        context: Context,
        file: File,
        uri: Uri? = null,
        title: String = "Compartir datos meteorológicos",
        targetPackage: String? = null
    ) {
        try {
            val shareUri = uri ?: getFileUri(context, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = ExportFormat.CSV.mimeType
                putExtra(Intent.EXTRA_STREAM, shareUri)
                putExtra(Intent.EXTRA_SUBJECT, "Datos Meteorológicos RAF")
                putExtra(Intent.EXTRA_TEXT, "Datos exportados desde RAF App")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                clipData = ClipData.newUri(context.contentResolver, file.name, shareUri)
                targetPackage?.let { setPackage(it) }
            }

            targetPackage?.let {
                context.grantUriPermission(
                    it,
                    shareUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }

            val resolved = shareIntent.resolveActivity(context.packageManager)
            if (targetPackage != null && resolved == null) {
                context.showErrorMessage("Aplicación no instalada")
                return
            }

            val chooser = Intent.createChooser(shareIntent, title).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }

            context.startActivity(chooser)
            Log.d(TAG, "Share intent created for file: ${file.name} - targetPackage=$targetPackage")

        } catch (e: Exception) {
            Log.e(TAG, "Error sharing file", e)
            context.showErrorMessage("Error al compartir archivo: ${e.message}")
        }
    }

    /**
     * Abre un archivo con la aplicación apropiada
     */
    fun openFile(context: Context, file: File) {
        try {
            val uri = getFileUri(context, file)

            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, ExportFormat.CSV.mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(openIntent, "Abrir con"))
            Log.d(TAG, "Open intent created for file: ${file.name}")

        } catch (e: Exception) {
            Log.e(TAG, "Error opening file", e)
            context.showErrorMessage("No se encontró aplicación para abrir el archivo")
        }
    }

    /**
     * Crea un archivo de exportación en el directorio de la app
     */
    private fun createExportFile(context: Context, fileName: String): File {
        // Usar directorio público de Documentos (Android 10+)
        val exportDir = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), EXPORT_DIRECTORY)
        } else {
            @Suppress("DEPRECATION")
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), EXPORT_DIRECTORY)
        }

        // Crear directorio si no existe
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }

        return File(exportDir, fileName)
    }

    /**
     * Obtiene Uri del archivo usando FileProvider
     */
    private fun getFileUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            file
        )
    }

    /**
     * Genera un nombre de archivo único y descriptivo
     */
    private fun generateFileName(
        stationName: String,
        dataType: String,
        format: ExportFormat,
        dateRange: String? = null,
        useFriendlyName: Boolean = false
    ): String {
        if (useFriendlyName) {
            val sanitizedName = stationName.replace(Regex("[\\\\/:*?\"<>|]"), "_")
            return "$sanitizedName.${format.extension}"
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val sanitizedStationName = stationName.replace(Regex("[^a-zA-Z0-9]"), "_")
        
        val rangePrefix = if (dateRange != null) {
            "_${dateRange.replace(Regex("[^a-zA-Z0-9]"), "_")}"
        } else {
            ""
        }

        return "RAF_${sanitizedStationName}_${dataType}${rangePrefix}_${timestamp}.${format.extension}"
    }

    /**
     * Obtiene el tamaño formateado de un archivo
     */
    fun getFormattedFileSize(file: File): String {
        val bytes = file.length()
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
            else -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
        }
    }

    /**
     * Elimina archivos de exportación antiguos (más de 7 días)
     */
    fun cleanOldExports(context: Context, daysOld: Int = 7) {
        try {
            val exportDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), EXPORT_DIRECTORY)
            if (!exportDir.exists()) return

            val cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)

            exportDir.listFiles()?.forEach { file ->
                if (file.lastModified() < cutoffTime) {
                    val deleted = file.delete()
                    if (deleted) {
                        Log.d(TAG, "Deleted old export: ${file.name}")
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning old exports", e)
        }
    }

    /**
     * Obtiene la lista de archivos exportados
     */
    fun getExportedFiles(context: Context): List<File> {
        val exportDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), EXPORT_DIRECTORY)
        if (!exportDir.exists()) return emptyList()

        return exportDir.listFiles()?.toList()?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    private fun formatValue(value: Double): String {
        return decimalFormat.format(value)
    }

    /**
     * Verifica si hay espacio suficiente para exportar
     */
    fun hasEnoughSpace(context: Context, requiredBytes: Long = 10 * 1024 * 1024): Boolean {
        return try {
            val exportDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val usableSpace = exportDir?.usableSpace ?: 0
            usableSpace > requiredBytes
        } catch (e: Exception) {
            Log.e(TAG, "Error checking available space", e)
            false
        }
    }
}
