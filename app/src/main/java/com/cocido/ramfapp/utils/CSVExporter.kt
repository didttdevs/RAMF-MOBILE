package com.cocido.ramfapp.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import com.cocido.ramfapp.models.WeatherData
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utilidad para exportar datos meteorológicos a CSV
 */
object CSVExporter {

    /**
     * Exporta datos meteorológicos a CSV y comparte el archivo
     */
    fun exportWeatherDataToCSV(
        context: Context,
        weatherData: List<WeatherData>,
        stationName: String,
        fromDate: String,
        toDate: String
    ): Uri? {
        return try {
            val csvContent = generateCSVContent(weatherData, stationName, fromDate, toDate)
            val fileName = generateFileName(stationName, fromDate, toDate)
            val file = createCSVFile(context, fileName, csvContent)
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Genera el contenido CSV de los datos meteorológicos
     */
    private fun generateCSVContent(
        weatherData: List<WeatherData>,
        stationName: String,
        fromDate: String,
        toDate: String
    ): String {
        val csv = StringBuilder()
        
        // Encabezado del archivo
        csv.appendLine("Datos Meteorológicos - Estación: $stationName")
        csv.appendLine("Período: $fromDate a $toDate")
        csv.appendLine("Generado: ${getCurrentDateTime()}")
        csv.appendLine() // Línea en blanco
        
        // Encabezados de columnas
        csv.appendLine("Fecha,Hora,Temperatura (°C),Humedad Relativa (%),Precipitación (mm),Velocidad del Viento (km/h),Dirección del Viento (°),Radiación Solar (W/m²),Presión Atmosférica (hPa)")
        
        // Datos
        weatherData.forEach { data ->
            val dateTime = parseDateTime(data.date)
            val sensors = data.sensors
            csv.appendLine("${dateTime.first},${dateTime.second},${sensors.hcAirTemperature?.avg},${sensors.hcRelativeHumidity?.avg},${sensors.precipitation?.sum},${sensors.usonicWindSpeed?.avg},${sensors.usonicWindDir?.last},${sensors.solarRadiation?.avg},${sensors.airPressure?.avg}")
        }
        
        return csv.toString()
    }

    /**
     * Crea el archivo CSV en el almacenamiento externo
     */
    private fun createCSVFile(context: Context, fileName: String, content: String): File {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val ramfDir = File(downloadsDir, "RAMF")
        
        // Crear directorio si no existe
        if (!ramfDir.exists()) {
            ramfDir.mkdirs()
        }
        
        val file = File(ramfDir, fileName)
        val writer = FileWriter(file)
        writer.write(content)
        writer.close()
        
        return file
    }

    /**
     * Genera el nombre del archivo CSV
     */
    private fun generateFileName(stationName: String, fromDate: String, toDate: String): String {
        val cleanStationName = stationName.replace(" ", "_").replace("/", "-")
        val cleanFromDate = fromDate.replace(":", "-").replace("T", "_").substring(0, 10)
        val cleanToDate = toDate.replace(":", "-").replace("T", "_").substring(0, 10)
        return "RAMF_${cleanStationName}_${cleanFromDate}_to_${cleanToDate}.csv"
    }

    /**
     * Parsea la fecha y hora del timestamp
     */
    private fun parseDateTime(timestamp: String): Pair<String, String> {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val outputTimeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            
            val date = inputFormat.parse(timestamp)
            val dateString = outputDateFormat.format(date ?: Date())
            val timeString = outputTimeFormat.format(date ?: Date())
            
            Pair(dateString, timeString)
        } catch (e: Exception) {
            Pair("N/A", "N/A")
        }
    }

    /**
     * Obtiene la fecha y hora actual
     */
    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * Comparte el archivo CSV
     */
    fun shareCSVFile(context: Context, fileUri: Uri, stationName: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            putExtra(Intent.EXTRA_SUBJECT, "Datos Meteorológicos - $stationName")
            putExtra(Intent.EXTRA_TEXT, "Datos meteorológicos exportados desde RAMF - Red Agrometeorológica de Formosa")
        }
        
        context.startActivity(Intent.createChooser(intent, "Compartir datos CSV"))
    }
}







