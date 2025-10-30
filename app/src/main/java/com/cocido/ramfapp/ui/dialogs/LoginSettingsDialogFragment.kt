package com.cocido.ramfapp.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import com.cocido.ramfapp.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.radiobutton.MaterialRadioButton

/**
 * Dialog fragment para configurar el diseño del login
 * Permite alternar entre layout v2 (con GIF) y v1 (fondo simple)
 */
class LoginSettingsDialogFragment : DialogFragment() {
    
    private var currentLayoutVersion: Boolean = true // true = v2, false = v1
    private var onLayoutSelected: ((Boolean) -> Unit)? = null
    
    companion object {
        /**
         * Crea una nueva instancia del diálogo
         * 
         * @param currentLayoutVersion true para v2 (GIF), false para v1 (simple)
         * @param onLayoutSelected Callback cuando se aplica un nuevo layout
         */
        fun newInstance(
            currentLayoutVersion: Boolean,
            onLayoutSelected: (Boolean) -> Unit
        ): LoginSettingsDialogFragment {
            return LoginSettingsDialogFragment().apply {
                this.currentLayoutVersion = currentLayoutVersion
                this.onLayoutSelected = onLayoutSelected
            }
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext()).apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_login_settings, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Configurar el tamaño del diálogo
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        setupViews(view)
    }
    
    private fun setupViews(view: View) {
        val cardV2 = view.findViewById<MaterialCardView>(R.id.cardLayoutV2)
        val cardV1 = view.findViewById<MaterialCardView>(R.id.cardLayoutV1)
        val radioV2 = view.findViewById<MaterialRadioButton>(R.id.radioLayoutV2)
        val radioV1 = view.findViewById<MaterialRadioButton>(R.id.radioLayoutV1)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancel)
        val btnApply = view.findViewById<MaterialButton>(R.id.btnApply)
        
        var selectedVersion = currentLayoutVersion
        
        // Marcar la opción actual
        radioV2.isChecked = currentLayoutVersion
        radioV1.isChecked = !currentLayoutVersion
        
        // Selección visual mejorada para la tarjeta seleccionada
        updateCardAppearance(cardV2, cardV1, radioV2.isChecked, radioV1.isChecked)
        
        // Función para seleccionar V2
        fun selectV2() {
            selectedVersion = true
            radioV2.isChecked = true
            radioV1.isChecked = false
            updateCardAppearance(cardV2, cardV1, true, false)
        }
        
        // Función para seleccionar V1
        fun selectV1() {
            selectedVersion = false
            radioV2.isChecked = false
            radioV1.isChecked = true
            updateCardAppearance(cardV2, cardV1, false, true)
        }
        
        // Click en tarjeta V2
        cardV2.setOnClickListener {
            selectV2()
        }
        
        // Click en RadioButton V2
        radioV2.setOnClickListener {
            selectV2()
        }
        
        // Click en tarjeta V1
        cardV1.setOnClickListener {
            selectV1()
        }
        
        // Click en RadioButton V1
        radioV1.setOnClickListener {
            selectV1()
        }
        
        // Click en botón Cancelar
        btnCancel.setOnClickListener {
            dismiss()
        }
        
        // Click en botón Aplicar
        btnApply.setOnClickListener {
            onLayoutSelected?.invoke(selectedVersion)
            dismiss()
        }
    }
    
    /**
     * Actualiza la apariencia de las tarjetas según la selección
     */
    private fun updateCardAppearance(
        cardV2: MaterialCardView,
        cardV1: MaterialCardView,
        v2Selected: Boolean,
        v1Selected: Boolean
    ) {
        if (v2Selected) {
            // V2 seleccionado - destacar
            cardV2.strokeWidth = 2
            cardV2.strokeColor = requireContext().getColor(R.color.formosa_blue_accent)
            cardV1.strokeWidth = 1
            cardV1.strokeColor = requireContext().getColor(R.color.text_secondary_gray)
        } else {
            // V1 seleccionado - destacar
            cardV1.strokeWidth = 2
            cardV1.strokeColor = requireContext().getColor(R.color.formosa_blue_accent)
            cardV2.strokeWidth = 1
            cardV2.strokeColor = requireContext().getColor(R.color.text_secondary_gray)
        }
    }
}

