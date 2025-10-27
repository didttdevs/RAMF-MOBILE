package com.cocido.ramfapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cocido.ramfapp.databinding.ActivityReportEditBinding
import com.cocido.ramfapp.models.CreateReportRequest
import com.cocido.ramfapp.models.UpdateReportRequest
import com.cocido.ramfapp.network.RetrofitClient
import com.cocido.ramfapp.utils.AuthManager
import kotlinx.coroutines.launch

/**
 * Activity para crear y editar reportes
 */
class ReportEditActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityReportEditBinding
    private var reportId: String? = null
    private var isEditMode = false
    private var stationName: String? = null
    
    companion object {
        const val EXTRA_REPORT_ID = "report_id"
        const val EXTRA_IS_EDIT_MODE = "is_edit_mode"
        const val EXTRA_STATION_NAME = "station_name"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Obtener parámetros del intent
        reportId = intent.getStringExtra(EXTRA_REPORT_ID)
        isEditMode = intent.getBooleanExtra(EXTRA_IS_EDIT_MODE, false)
        stationName = intent.getStringExtra(EXTRA_STATION_NAME)
        
        setupToolbar()
        setupListeners()
        setupForm()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = if (isEditMode) "Editar Reporte" else "Nuevo Reporte"
        }
    }
    
    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            if (isEditMode) {
                updateReport()
            } else {
                createReport()
            }
        }
        
        binding.btnCancel.setOnClickListener {
            finish()
        }
    }
    
    private fun setupForm() {
        // Si es modo edición, cargar datos existentes
        if (isEditMode && reportId != null) {
            loadReportData()
        }
        
        // Si hay estación predefinida, establecerla
        if (stationName != null) {
            binding.etStation.setText(stationName)
            binding.etStation.isEnabled = false // No permitir cambiar la estación
        }
        
        // Configurar validaciones en tiempo real
        setupValidations()
    }
    
    private fun setupValidations() {
        binding.etTitle.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                validateTitle()
            }
        })
        
        binding.etDescription.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                validateDescription()
            }
        })
        
        binding.etStation.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                validateStation()
            }
        })
    }
    
    private fun validateTitle(): Boolean {
        val title = binding.etTitle.text.toString().trim()
        return if (title.isEmpty()) {
            binding.tilTitle.error = "El título es requerido"
            false
        } else if (title.length < 5) {
            binding.tilTitle.error = "El título debe tener al menos 5 caracteres"
            false
        } else {
            binding.tilTitle.error = null
            true
        }
    }
    
    private fun validateDescription(): Boolean {
        val description = binding.etDescription.text.toString().trim()
        return if (description.isEmpty()) {
            binding.tilDescription.error = "La descripción es requerida"
            false
        } else if (description.length < 10) {
            binding.tilDescription.error = "La descripción debe tener al menos 10 caracteres"
            false
        } else {
            binding.tilDescription.error = null
            true
        }
    }
    
    private fun validateStation(): Boolean {
        val station = binding.etStation.text.toString().trim()
        return if (station.isEmpty()) {
            binding.tilStation.error = "La estación es requerida"
            false
        } else {
            binding.tilStation.error = null
            true
        }
    }
    
    private fun validateForm(): Boolean {
        val titleValid = validateTitle()
        val descriptionValid = validateDescription()
        val stationValid = validateStation()
        
        return titleValid && descriptionValid && stationValid
    }
    
    private fun loadReportData() {
        // TODO: Implementar carga de datos del reporte para edición
        // Por ahora, solo mostrar un mensaje
        Toast.makeText(this, "Cargando datos del reporte...", Toast.LENGTH_SHORT).show()
    }
    
    private fun createReport() {
        if (!validateForm()) {
            return
        }
        
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                val token = AuthManager.getAccessToken()
                if (token == null) {
                    showError("Sesión expirada")
                    return@launch
                }
                
                val createRequest = CreateReportRequest(
                    title = binding.etTitle.text.toString().trim(),
                    description = binding.etDescription.text.toString().trim(),
                    stationName = binding.etStation.text.toString().trim(),
                    priority = binding.spPriority.selectedItem.toString().lowercase()
                )
                
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
    
    private fun updateReport() {
        if (!validateForm() || reportId == null) {
            return
        }
        
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                val token = AuthManager.getAccessToken()
                if (token == null) {
                    showError("Sesión expirada")
                    return@launch
                }
                
                val updateRequest = UpdateReportRequest(
                    title = binding.etTitle.text.toString().trim(),
                    description = binding.etDescription.text.toString().trim(),
                    status = null // No cambiar estado en edición
                )
                
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
    
    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !loading
        binding.btnCancel.isEnabled = !loading
        binding.etTitle.isEnabled = !loading
        binding.etDescription.isEnabled = !loading
        binding.etStation.isEnabled = !loading
        binding.spPriority.isEnabled = !loading
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
