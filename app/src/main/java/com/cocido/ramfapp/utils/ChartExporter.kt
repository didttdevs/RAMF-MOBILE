package com.cocido.ramfapp.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Environment
import android.view.View
import com.github.mikephil.charting.charts.Chart
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utilidad para exportar gráficos como imágenes
 */
object ChartExporter {

    /**
     * Exporta un gráfico como imagen PNG
     */
    fun exportChartAsImage(
        context: Context,
        chart: Chart<*>,
        stationName: String,
        chartType: String,
        fromDate: String,
        toDate: String
    ): Uri? {
        return try {
            val bitmap = createChartBitmap(chart)
            val fileName = generateImageFileName(stationName, chartType, fromDate, toDate)
            val file = saveBitmapToFile(context, fileName, bitmap)
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Exporta una vista como imagen PNG
     */
    fun exportViewAsImage(
        context: Context,
        view: View,
        stationName: String,
        viewType: String,
        fromDate: String,
        toDate: String
    ): Uri? {
        return try {
            val bitmap = createViewBitmap(view)
            val fileName = generateImageFileName(stationName, viewType, fromDate, toDate)
            val file = saveBitmapToFile(context, fileName, bitmap)
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Crea un bitmap del gráfico
     */
    private fun createChartBitmap(chart: Chart<*>): Bitmap {
        // Forzar el dibujado del gráfico
        chart.measure(
            View.MeasureSpec.makeMeasureSpec(chart.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(chart.height, View.MeasureSpec.EXACTLY)
        )
        chart.layout(0, 0, chart.measuredWidth, chart.measuredHeight)
        
        val bitmap = Bitmap.createBitmap(
            chart.measuredWidth,
            chart.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        
        val canvas = Canvas(bitmap)
        chart.draw(canvas)
        
        return bitmap
    }

    /**
     * Crea un bitmap de una vista
     */
    private fun createViewBitmap(view: View): Bitmap {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        
        val bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        
        return bitmap
    }

    /**
     * Guarda el bitmap como archivo PNG
     */
    private fun saveBitmapToFile(context: Context, fileName: String, bitmap: Bitmap): File {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val ramfDir = File(downloadsDir, "RAMF")
        
        // Crear directorio si no existe
        if (!ramfDir.exists()) {
            ramfDir.mkdirs()
        }
        
        val file = File(ramfDir, fileName)
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.close()
        
        return file
    }

    /**
     * Genera el nombre del archivo de imagen
     */
    private fun generateImageFileName(
        stationName: String,
        chartType: String,
        fromDate: String,
        toDate: String
    ): String {
        val cleanStationName = stationName.replace(" ", "_").replace("/", "-")
        val cleanChartType = chartType.replace(" ", "_").lowercase()
        val cleanFromDate = fromDate.replace(":", "-").replace("T", "_").substring(0, 10)
        val cleanToDate = toDate.replace(":", "-").replace("T", "_").substring(0, 10)
        return "RAMF_${cleanStationName}_${cleanChartType}_${cleanFromDate}_to_${cleanToDate}.png"
    }

    /**
     * Comparte la imagen del gráfico
     */
    fun shareChartImage(context: Context, fileUri: Uri, stationName: String, chartType: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            putExtra(Intent.EXTRA_SUBJECT, "Gráfico $chartType - $stationName")
            putExtra(Intent.EXTRA_TEXT, "Gráfico meteorológico exportado desde RAMF - Red Agrometeorológica de Formosa")
        }
        
        context.startActivity(Intent.createChooser(intent, "Compartir gráfico"))
    }

    /**
     * Crea un bitmap con texto personalizado (para gráficos sin datos)
     */
    fun createNoDataBitmap(width: Int, height: Int, message: String): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Fondo blanco
        canvas.drawColor(Color.WHITE)
        
        // Texto centrado
        val paint = Paint().apply {
            color = Color.GRAY
            textSize = 48f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        
        val x = width / 2f
        val y = height / 2f
        
        canvas.drawText(message, x, y, paint)
        
        return bitmap
    }
}









