package com.cocido.ramfapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cocido.ramfapp.databinding.ItemReportBinding
import com.cocido.ramfapp.models.Report
import java.text.SimpleDateFormat
import java.util.*

class ReportsAdapter(
    private val onReportClick: (Report) -> Unit
) : RecyclerView.Adapter<ReportsAdapter.ReportViewHolder>() {

    private var reports: List<Report> = emptyList()

    fun updateReports(newReports: List<Report>) {
        reports = newReports
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reports[position])
    }

    override fun getItemCount(): Int = reports.size

    inner class ReportViewHolder(private val binding: ItemReportBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(report: Report) {
            binding.apply {
                tvTitle.text = report.title
                tvDescription.text = report.description
                tvDate.text = formatDate(report.createdAt)
                
                root.setOnClickListener {
                    onReportClick(report)
                }
            }
        }
        
        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                dateString
            }
        }
    }
}





