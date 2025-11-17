package com.cocido.ramfapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cocido.ramfapp.databinding.ActivityReportDetailBinding
import com.cocido.ramfapp.models.Report
import com.cocido.ramfapp.network.RetrofitClient
import com.cocido.ramfapp.ui.components.showErrorMessage
import com.cocido.ramfapp.ui.components.showInfoMessage
import com.cocido.ramfapp.utils.AuthManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity para mostrar el detalle completo de un reporte
 */
class ReportDetailActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityReportDetailBinding
    private var reportId: String? = null
    private var report: Report? = null
    
    companion object {
        const val EXTRA_REPORT_ID = "report_id"
        const val REQUEST_CODE_EDIT_REPORT = 1001
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Obtener ID del reporte del intent
        reportId = intent.getStringExtra(EXTRA_REPORT_ID)
        if (reportId == null) {
            showError("ID de reporte no válido")
            finish()
            return
        }
        
        setupToolbar()
        setupListeners()
        loadReportDetail()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Detalle del Reporte"
        }
    }
    
    private fun setupListeners() {
        binding.btnEditReport.setOnClickListener {
            editReport()
        }
        
        binding.btnDeleteReport.setOnClickListener {
            deleteReport()
        }
        
        binding.btnShareReport.setOnClickListener {
            shareReport()
        }
    }
    
    private fun loadReportDetail() {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                val token = AuthManager.getAccessToken()
                if (token == null) {
                    showError("Sesión expirada")
                    return@launch
                }
                
                // TODO: Implementar servicio de reportes
                val response = null
                
                // TODO: Implementar lógica de respuesta
                showError("Servicio de reportes no implementado")
            } catch (e: Exception) {
                showError("Error de conexión: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun displayReportDetail(report: Report) {
        // Título
        binding.tvTitle.text = report.title
        
        // Descripción
        binding.tvDescription.text = report.description
        
        // Estación
        binding.tvStation.text = report.stationName
        
        // Fecha de creación
        try {
            val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .parse(report.createdAt)
            val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(date ?: Date())
            binding.tvCreatedAt.text = formattedDate
        } catch (e: Exception) {
            binding.tvCreatedAt.text = report.createdAt
        }
        
        // Fecha de actualización
        try {
            val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .parse(report.updatedAt)
            val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(date ?: Date())
            binding.tvUpdatedAt.text = formattedDate
        } catch (e: Exception) {
            binding.tvUpdatedAt.text = report.updatedAt
        }
        
        // Autor
        val authorName = if (report.author != null) {
            "${report.author!!.firstName ?: ""} ${report.author!!.lastName ?: ""}".trim()
        } else {
            "Desconocido"
        }
        binding.tvAuthor.text = authorName
        
        // Estado
        binding.tvStatus.text = report.status ?: "Pendiente"
        setStatusColor(report.status)
        
        // Prioridad
        binding.tvPriority.text = report.priority ?: "Normal"
        setPriorityColor(report.priority)
        
        // Adjuntos
        if (report.attachments.isNullOrEmpty()) {
            binding.layoutAttachments.visibility = View.GONE
        } else {
            binding.layoutAttachments.visibility = View.VISIBLE
            displayAttachments(report.attachments!!)
        }
        
        // Mostrar botones según permisos
        // TODO: Verificar permisos del usuario para editar/eliminar
        binding.btnEditReport.visibility = View.VISIBLE
        binding.btnDeleteReport.visibility = View.VISIBLE
    }
    
    private fun displayAttachments(attachments: List<com.cocido.ramfapp.models.ReportAttachment>) {
        // Limpiar attachments existentes
        binding.layoutAttachmentsList.removeAllViews()
        
        attachments.forEach { attachment ->
            val attachmentView = layoutInflater.inflate(android.R.layout.simple_list_item_2, null) // R.layout.item_attachment
            val tvFilename = attachmentView.findViewById<android.widget.TextView>(android.R.id.text1) // R.id.tvFilename
            val tvSize = attachmentView.findViewById<android.widget.TextView>(android.R.id.text2) // R.id.tvSize
            val btnDownload = attachmentView.findViewById<android.widget.Button>(android.R.id.button1) // R.id.btnDownload
            
            tvFilename.text = attachment.filename
            tvSize.text = formatFileSize(attachment.size)
            
            btnDownload.setOnClickListener {
                downloadAttachment(attachment)
            }
            
            binding.layoutAttachmentsList.addView(attachmentView)
        }
    }
    
    private fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> "${size / (1024 * 1024)} MB"
        }
    }
    
    private fun setStatusColor(status: String?) {
        val color = when (status?.lowercase()) {
            "resuelto", "completado" -> getColor(android.R.color.holo_green_light) // R.color.success_green
            "en_progreso", "procesando" -> getColor(android.R.color.holo_orange_light) // R.color.warning_orange
            "cancelado", "rechazado" -> getColor(android.R.color.holo_red_light) // R.color.error_red
            else -> getColor(android.R.color.holo_blue_light) // R.color.info_blue
        }
        binding.tvStatus.setTextColor(color)
    }
    
    private fun setPriorityColor(priority: String?) {
        val color = when (priority?.lowercase()) {
            "alta", "urgente" -> getColor(android.R.color.holo_red_light) // R.color.error_red
            "media", "normal" -> getColor(android.R.color.holo_orange_light) // R.color.warning_orange
            "baja" -> getColor(android.R.color.holo_green_light) // R.color.success_green
            else -> getColor(android.R.color.darker_gray) // R.color.text_secondary_gray
        }
        binding.tvPriority.setTextColor(color)
    }
    
    private fun editReport() {
        if (report == null) return
        
        val intent = Intent(this, ReportEditActivity::class.java).apply {
            putExtra(ReportEditActivity.EXTRA_REPORT_ID, report!!.id)
            putExtra(ReportEditActivity.EXTRA_IS_EDIT_MODE, true)
        }
        startActivityForResult(intent, REQUEST_CODE_EDIT_REPORT)
    }
    
    private fun deleteReport() {
        if (report == null) return
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Eliminar Reporte")
            .setMessage("¿Estás seguro de que quieres eliminar este reporte? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                confirmDeleteReport()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun confirmDeleteReport() {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                val token = AuthManager.getAccessToken()
                if (token == null) {
                    showError("Sesión expirada")
                    return@launch
                }
                
                // TODO: Implementar servicio de reportes
                val response = null
                
                // TODO: Implementar lógica de respuesta
                showError("Servicio de reportes no implementado")
            } catch (e: Exception) {
                showError("Error de conexión: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun shareReport() {
        if (report == null) return
        
        val shareText = buildString {
            appendLine("📊 Reporte: ${report!!.title}")
            appendLine("🏢 Estación: ${report!!.stationName}")
            appendLine("📝 Descripción: ${report!!.description}")
            appendLine("👤 Autor: ${report!!.author?.firstName} ${report!!.author?.lastName}")
            appendLine("📅 Fecha: ${report!!.createdAt}")
            appendLine("📱 Desde RAMF - Red Agrometeorológica de Formosa")
        }
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "Reporte RAMF: ${report!!.title}")
        }
        
        startActivity(Intent.createChooser(intent, "Compartir reporte"))
    }
    
    private fun downloadAttachment(attachment: com.cocido.ramfapp.models.ReportAttachment) {
        // TODO: Implementar descarga de adjuntos
        showInfoMessage("Descargando ${attachment.filename}...")
    }
    
    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnEditReport.isEnabled = !loading
        binding.btnDeleteReport.isEnabled = !loading
        binding.btnShareReport.isEnabled = !loading
    }
    
    private fun showError(message: String) {
        showErrorMessage(message)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_CODE_EDIT_REPORT && resultCode == RESULT_OK) {
            // Refrescar datos después de editar
            loadReportDetail()
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}


