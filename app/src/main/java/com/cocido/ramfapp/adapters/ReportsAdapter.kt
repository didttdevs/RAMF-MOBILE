package com.cocido.ramfapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cocido.ramfapp.R
import com.cocido.ramfapp.models.Report
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter para mostrar lista de reportes
 */
class ReportsAdapter(
    private val onReportClick: (Report) -> Unit
) : RecyclerView.Adapter<ReportsAdapter.ReportViewHolder>() {
    
    private val reports = mutableListOf<Report>()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reports[position])
    }
    
    override fun getItemCount(): Int = reports.size
    
    fun updateReports(newReports: List<Report>) {
        reports.clear()
        reports.addAll(newReports)
        notifyDataSetChanged()
    }
    
    fun addReports(newReports: List<Report>) {
        val startPosition = reports.size
        reports.addAll(newReports)
        notifyItemRangeInserted(startPosition, newReports.size)
    }
    
    fun clearReports() {
        reports.clear()
        notifyDataSetChanged()
    }
    
    inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvStation: TextView = itemView.findViewById(R.id.tvStation)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        
        fun bind(report: Report) {
            tvTitle.text = report.title
            tvDescription.text = report.description
            tvStation.text = report.stationName
            
            // Formatear fecha
            try {
                val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    .parse(report.createdAt)
                tvDate.text = dateFormat.format(date ?: Date())
            } catch (e: Exception) {
                tvDate.text = report.createdAt
            }
            
            // Autor
            val authorName = if (report.author != null) {
                "${report.author.firstName ?: ""} ${report.author.lastName ?: ""}".trim()
            } else {
                "Desconocido"
            }
            tvAuthor.text = authorName
            
            // Estado
            tvStatus.text = report.status ?: "Pendiente"
            setStatusColor(report.status)
            
            // Click listener
            itemView.setOnClickListener {
                onReportClick(report)
            }
        }
        
        private fun setStatusColor(status: String?) {
            val color = when (status?.lowercase()) {
                "resuelto", "completado" -> R.color.success_green
                "en_progreso", "procesando" -> R.color.warning_orange
                "cancelado", "rechazado" -> R.color.error_red
                else -> R.color.info_blue
            }
            tvStatus.setTextColor(itemView.context.getColor(color))
        }
    }
}









