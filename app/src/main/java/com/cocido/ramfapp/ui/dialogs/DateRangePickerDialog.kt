package com.cocido.ramfapp.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.Pair
import com.cocido.ramfapp.R
import com.cocido.ramfapp.databinding.DialogDateRangePickerBinding
import com.cocido.ramfapp.utils.DateRangeUtils
import com.cocido.ramfapp.utils.DateRangeUtils.DateRange
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.*

/**
 * Dialog profesional para selección de rangos de fechas
 * Implementa Material Design 3 con presets y selección personalizada
 * 
 * Uso:
 * ```
 * DateRangePickerDialog.newInstance(currentRange) { selectedRange ->
 *     // Manejar rango seleccionado
 * }.show(supportFragmentManager, "date_range_picker")
 * ```
 */
class DateRangePickerDialog : BottomSheetDialogFragment() {

    private var _binding: DialogDateRangePickerBinding? = null
    private val binding get() = _binding!!

    private var currentRange: DateRange = DateRange.getDefault()
    private var onRangeSelected: ((DateRange) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDateRangePickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPresetChips()
        setupCustomDateButton()
        setupActionButtons()
        updateRangeDisplay()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        
        // Expandir el bottom sheet completamente
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }
        
        return dialog
    }

    /**
     * Configura los chips de presets de rangos de fechas
     */
    private fun setupPresetChips() {
        binding.chipGroup.removeAllViews()

        // Crear chips para cada preset (excepto CUSTOM que se maneja aparte)
        DateRangeUtils.DateRangePreset.values()
            .filter { it != DateRangeUtils.DateRangePreset.CUSTOM }
            .forEach { preset ->
                val chip = createPresetChip(preset)
                binding.chipGroup.addView(chip)
            }

        // Seleccionar el chip correspondiente al rango actual
        updateSelectedChip()
    }

    /**
     * Crea un chip para un preset
     */
    private fun createPresetChip(preset: DateRangeUtils.DateRangePreset): Chip {
        return Chip(requireContext()).apply {
            text = preset.displayName
            isCheckable = true
            isChecked = currentRange.preset == preset

            setOnClickListener {
                selectPreset(preset)
            }

            // Estilo Material Design 3
            chipBackgroundColor = context.getColorStateList(R.color.chip_background_selector)
            setTextColor(context.getColorStateList(R.color.chip_text_selector))
        }
    }

    /**
     * Selecciona un preset y actualiza el rango
     */
    private fun selectPreset(preset: DateRangeUtils.DateRangePreset) {
        currentRange = DateRange.fromPreset(preset)
        updateRangeDisplay()
        updateSelectedChip()
    }

    /**
     * Actualiza el chip seleccionado visualmente
     */
    private fun updateSelectedChip() {
        for (i in 0 until binding.chipGroup.childCount) {
            val chip = binding.chipGroup.getChildAt(i) as? Chip
            chip?.isChecked = false
        }

        // Marcar el chip correspondiente al preset actual
        val presets = DateRangeUtils.DateRangePreset.values()
            .filter { it != DateRangeUtils.DateRangePreset.CUSTOM }
        
        val index = presets.indexOf(currentRange.preset)
        if (index >= 0 && index < binding.chipGroup.childCount) {
            (binding.chipGroup.getChildAt(index) as? Chip)?.isChecked = true
        }
    }

    /**
     * Configura el botón para selección personalizada
     */
    private fun setupCustomDateButton() {
        binding.btnCustomRange.setOnClickListener {
            showMaterialDateRangePicker()
        }
    }

    /**
     * Muestra el Material Date Range Picker
     */
    private fun showMaterialDateRangePicker() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Seleccionar rango de fechas")
            .setSelection(
                Pair(
                    currentRange.startDate.time,
                    currentRange.endDate.time
                )
            )
            .setTheme(R.style.ThemeOverlay_App_DatePicker)
            .build()

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = Date(selection.first)
            val endDate = Date(selection.second)

            // Validar que el rango sea válido
            if (!DateRangeUtils.isDateInValidRange(startDate) || 
                !DateRangeUtils.isDateInValidRange(endDate)) {
                showInvalidDateError()
                return@addOnPositiveButtonClickListener
            }

            // Usar inicio y fin del día para incluir todo el rango
            currentRange = DateRange(
                DateRangeUtils.getStartOfDay(startDate),
                DateRangeUtils.getEndOfDay(endDate),
                DateRangeUtils.DateRangePreset.CUSTOM
            )

            updateRangeDisplay()
            updateSelectedChip()
        }

        dateRangePicker.show(parentFragmentManager, "date_range_picker")
    }

    /**
     * Actualiza la visualización del rango seleccionado
     */
    private fun updateRangeDisplay() {
        binding.tvSelectedRange.text = currentRange.toDisplayString()

        // Mostrar duración
        val days = currentRange.getDurationInDays()
        binding.tvRangeDuration.text = when {
            days == 0 -> "Mismo día"
            days == 1 -> "1 día"
            days < 7 -> "$days días"
            days < 30 -> "${days / 7} semanas"
            days < 365 -> "${days / 30} meses"
            else -> "${days / 365} años"
        }

        // Advertencia si el rango es muy grande
        if (currentRange.isLongerThan(90)) {
            binding.tvRangeWarning.visibility = View.VISIBLE
            binding.tvRangeWarning.text = 
                "⚠️ Rango extenso: La carga puede ser lenta"
        } else {
            binding.tvRangeWarning.visibility = View.GONE
        }
    }

    /**
     * Configura los botones de acción
     */
    private fun setupActionButtons() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnApply.setOnClickListener {
            if (currentRange.isValid()) {
                onRangeSelected?.invoke(currentRange)
                dismiss()
            } else {
                showInvalidDateError()
            }
        }
    }

    /**
     * Muestra error de fecha inválida
     */
    private fun showInvalidDateError() {
        binding.tvRangeWarning.visibility = View.VISIBLE
        binding.tvRangeWarning.text = "❌ Rango de fechas inválido"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getTheme(): Int = R.style.ThemeOverlay_App_BottomSheetDialog

    companion object {
        /**
         * Crea una nueva instancia del dialog
         * 
         * @param currentRange Rango actual seleccionado
         * @param onRangeSelected Callback cuando se selecciona un nuevo rango
         */
        fun newInstance(
            currentRange: DateRange = DateRange.getDefault(),
            onRangeSelected: (DateRange) -> Unit
        ): DateRangePickerDialog {
            return DateRangePickerDialog().apply {
                this.currentRange = currentRange
                this.onRangeSelected = onRangeSelected
            }
        }
    }
}

