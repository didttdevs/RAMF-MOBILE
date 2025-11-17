package com.cocido.ramfapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.cocido.ramfapp.R
import com.cocido.ramfapp.databinding.ActivityReportsBinding
import com.cocido.ramfapp.models.Report
import com.cocido.ramfapp.models.ReportAuthor
import com.cocido.ramfapp.ui.adapters.ReportsAdapter
import com.cocido.ramfapp.ui.components.showInfoMessage

/**
 * Activity simplificado para mostrar reportes
 */
class ReportsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityReportsBinding
    private lateinit var reportsAdapter: ReportsAdapter
    private val reports = mutableListOf<Report>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        loadReports()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Reportes"
    }
    
    private fun setupRecyclerView() {
        reportsAdapter = ReportsAdapter { report ->
            openReportDetail(report)
        }
        
        binding.recyclerViewReports.apply {
            adapter = reportsAdapter
            layoutManager = LinearLayoutManager(this@ReportsActivity)
        }
    }
    
    private fun loadReports() {
        // Datos de ejemplo
        val sampleReports = listOf(
            Report(
                id = "1",
                title = "Reporte Estación Central",
                description = "Reporte de condiciones meteorológicas",
                stationName = "Estación Central",
                author = ReportAuthor(id = "1", firstName = "Sistema", lastName = "", email = ""),
                status = "Activo",
                priority = "Normal",
                createdAt = "2024-01-15T10:30:00Z",
                updatedAt = "2024-01-15T10:30:00Z"
            ),
            Report(
                id = "2", 
                title = "Reporte Estación Norte",
                description = "Análisis de precipitaciones",
                stationName = "Estación Norte",
                author = ReportAuthor(id = "1", firstName = "Sistema", lastName = "", email = ""),
                status = "Activo",
                priority = "Normal",
                createdAt = "2024-01-14T15:45:00Z",
                updatedAt = "2024-01-14T15:45:00Z"
            )
        )
        
        reports.clear()
        reports.addAll(sampleReports)
        reportsAdapter.updateReports(reports)
    }
    
    private fun openReportDetail(report: Report) {
        showInfoMessage("Abriendo reporte: ${report.title}")
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}