package com.cocido.ramfapp.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import com.cocido.ramfapp.R
import com.cocido.ramfapp.databinding.DialogChangePasswordBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Dialog profesional para cambio de contraseña
 * Implementa validaciones y UX optimizada
 */
class ChangePasswordDialog : BottomSheetDialogFragment() {

    private var _binding: DialogChangePasswordBinding? = null
    private val binding get() = _binding!!

    private var onPasswordChanged: ((oldPassword: String, newPassword: String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupValidation()
        setupButtons()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        
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

    private fun setupValidation() {
        // Validación de contraseña actual
        binding.etOldPassword.doOnTextChanged { _, _, _, _ ->
            binding.tilOldPassword.error = null
        }

        // Validación de nueva contraseña
        binding.etNewPassword.doOnTextChanged { text, _, _, _ ->
            binding.tilNewPassword.error = null
            
            // Validar fortaleza
            if (text != null && text.length >= 8) {
                validatePasswordStrength(text.toString())
            }
        }

        // Validación de confirmación
        binding.etConfirmPassword.doOnTextChanged { _, _, _, _ ->
            binding.tilConfirmPassword.error = null
        }
    }

    private fun validatePasswordStrength(password: String) {
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        val strength = when {
            password.length >= 12 && hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar -> "Fuerte"
            password.length >= 8 && hasUpperCase && hasLowerCase && hasDigit -> "Media"
            else -> "Débil"
        }

        val color = when (strength) {
            "Fuerte" -> R.color.success_green
            "Media" -> R.color.warning_orange
            else -> R.color.error_red
        }

        binding.tvPasswordStrength.text = "Fortaleza: $strength"
        binding.tvPasswordStrength.setTextColor(resources.getColor(color, null))
        binding.tvPasswordStrength.visibility = View.VISIBLE
    }

    private fun setupButtons() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnChange.setOnClickListener {
            if (validateFields()) {
                val oldPassword = binding.etOldPassword.text.toString()
                val newPassword = binding.etNewPassword.text.toString()
                
                onPasswordChanged?.invoke(oldPassword, newPassword)
                dismiss()
            }
        }
    }

    private fun validateFields(): Boolean {
        val oldPassword = binding.etOldPassword.text.toString()
        val newPassword = binding.etNewPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        var isValid = true

        // Validar contraseña actual
        if (oldPassword.isEmpty()) {
            binding.tilOldPassword.error = "Ingresa tu contraseña actual"
            isValid = false
        }

        // Validar nueva contraseña
        if (newPassword.isEmpty()) {
            binding.tilNewPassword.error = "Ingresa una nueva contraseña"
            isValid = false
        } else if (newPassword.length < 8) {
            binding.tilNewPassword.error = "La contraseña debe tener al menos 8 caracteres"
            isValid = false
        } else if (newPassword == oldPassword) {
            binding.tilNewPassword.error = "La nueva contraseña debe ser diferente"
            isValid = false
        }

        // Validar confirmación
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "Confirma tu nueva contraseña"
            isValid = false
        } else if (confirmPassword != newPassword) {
            binding.tilConfirmPassword.error = "Las contraseñas no coinciden"
            isValid = false
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getTheme(): Int = R.style.ThemeOverlay_App_BottomSheetDialog

    companion object {
        fun newInstance(
            onPasswordChanged: (oldPassword: String, newPassword: String) -> Unit
        ): ChangePasswordDialog {
            return ChangePasswordDialog().apply {
                this.onPasswordChanged = onPasswordChanged
            }
        }
    }
}

