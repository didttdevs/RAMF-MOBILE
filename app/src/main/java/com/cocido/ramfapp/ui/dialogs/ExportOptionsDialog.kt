package com.cocido.ramfapp.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.cocido.ramfapp.databinding.DialogExportOptionsBinding

/**
 * Dialog para mostrar opciones de exportación de datos
 */
class ExportOptionsDialog : DialogFragment() {

    private lateinit var binding: DialogExportOptionsBinding
    private var onExportOptionSelected: ((ExportOption) -> Unit)? = null

    enum class ExportOption {
        CSV_DATA,
        CHART_IMAGE,
        SHARE_DATA,
        SHARE_CHART
    }

    companion object {
        fun newInstance(onExportOptionSelected: (ExportOption) -> Unit): ExportOptionsDialog {
            val dialog = ExportOptionsDialog()
            dialog.onExportOptionSelected = onExportOptionSelected
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogExportOptionsBinding.inflate(layoutInflater)
        
        setupUI()
        setupListeners()
        
        return Dialog(requireContext()).apply {
            setContentView(binding.root)
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun setupUI() {
        // Configurar opciones de exportación
        setupExportOptions()
    }

    private fun setupExportOptions() {
        val exportOptions = listOf(
            Triple("📊 Exportar Datos CSV", "Descargar datos en formato CSV", ExportOption.CSV_DATA),
            Triple("📈 Exportar Gráfico", "Guardar gráfico como imagen PNG", ExportOption.CHART_IMAGE),
            Triple("📤 Compartir Datos", "Compartir datos por WhatsApp, Email, etc.", ExportOption.SHARE_DATA),
            Triple("📱 Compartir Gráfico", "Compartir gráfico por WhatsApp, Email, etc.", ExportOption.SHARE_CHART)
        )

        binding.layoutOptions.removeAllViews()
        
        exportOptions.forEach { (title, description, option) ->
            val optionView = createOptionView(title, description, option)
            binding.layoutOptions.addView(optionView)
        }
    }

    private fun createOptionView(title: String, description: String, option: ExportOption): View {
        val optionView = LayoutInflater.from(requireContext())
            .inflate(android.R.layout.simple_list_item_2, binding.layoutOptions, false) // R.layout.item_export_option
        
        val tvTitle = optionView.findViewById<android.widget.TextView>(android.R.id.text1) // R.id.tvOptionTitle
        val tvDescription = optionView.findViewById<android.widget.TextView>(android.R.id.text2) // R.id.tvOptionDescription
        val ivIcon = optionView.findViewById<android.widget.ImageView>(android.R.id.icon) // R.id.ivOptionIcon
        
        tvTitle.text = title
        tvDescription.text = description
        
        // Icono según la opción
        val iconRes = when (option) {
            ExportOption.CSV_DATA -> android.R.drawable.ic_menu_save // R.drawable.ic_file_download
            ExportOption.CHART_IMAGE -> android.R.drawable.ic_menu_gallery // R.drawable.ic_image
            ExportOption.SHARE_DATA -> android.R.drawable.ic_menu_share // R.drawable.ic_share
            ExportOption.SHARE_CHART -> android.R.drawable.ic_menu_share // R.drawable.ic_share
        }
        ivIcon.setImageResource(iconRes)
        
        optionView.setOnClickListener {
            onExportOptionSelected?.invoke(option)
            dismiss()
        }
        
        return optionView
    }

    private fun setupListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }
}


