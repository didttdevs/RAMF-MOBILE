package com.cocido.ramfapp.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.cocido.ramfapp.databinding.DialogChangePasswordBinding
import com.cocido.ramfapp.viewmodels.ProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/**
 * Dialog para cambiar la contraseña del usuario
 * Incluye validaciones en tiempo real y manejo de errores
 */
class ChangePasswordDialogFragment : DialogFragment() {
    
    private var _binding: DialogChangePasswordBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: ProfileViewModel
    private var onPasswordChangedListener: (() -> Unit)? = null
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogChangePasswordBinding.inflate(layoutInflater)
        
        viewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
        
        setupUI()
        setupListeners()
        observeViewModel()
        
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cambiar Contraseña")
            .setView(binding.root)
            .setPositiveButton("Cambiar", null) // Se maneja en el listener
            .setNegativeButton("Cancelar") { _, _ -> dismiss() }
            .create()
    }
    
    override fun onStart() {
        super.onStart()
        
        // Configurar el botón positivo después de que el dialog esté creado
        val dialog = dialog as? androidx.appcompat.app.AlertDialog
        dialog?.getButton(DialogInterface.BUTTON_POSITIVE)?.setOnClickListener {
            changePassword()
        }
    }
    
    private fun setupUI() {
        // Configurar TextWatchers para validación en tiempo real
        setupTextWatchers()
    }
    
    private fun setupListeners() {
        // Los botones de toggle se manejan automáticamente por Material Design
    }
    
    private fun setupTextWatchers() {
        binding.apply {
            etCurrentPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    validateCurrentPassword()
                }
            })
            
            etNewPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    validateNewPassword()
                    validatePasswordMatch()
                }
            })
            
            etConfirmPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    validatePasswordMatch()
                }
            })
        }
    }
    
    private fun validateCurrentPassword(): Boolean {
        val currentPassword = binding.etCurrentPassword.text.toString()
        return when {
            currentPassword.isEmpty() -> {
                binding.tilCurrentPassword.error = "La contraseña actual es requerida"
                false
            }
            else -> {
                binding.tilCurrentPassword.error = null
                true
            }
        }
    }
    
    private fun validateNewPassword(): Boolean {
        val newPassword = binding.etNewPassword.text.toString()
        return when {
            newPassword.isEmpty() -> {
                binding.tilNewPassword.error = "La nueva contraseña es requerida"
                false
            }
            newPassword.length < 8 -> {
                binding.tilNewPassword.error = "La contraseña debe tener al menos 8 caracteres"
                false
            }
            !isValidPassword(newPassword) -> {
                binding.tilNewPassword.error = "Debe contener mayúscula, minúscula y número"
                false
            }
            else -> {
                binding.tilNewPassword.error = null
                true
            }
        }
    }
    
    private fun validatePasswordMatch(): Boolean {
        val newPassword = binding.etNewPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        
        return when {
            confirmPassword.isEmpty() -> {
                binding.tilConfirmPassword.error = "Confirma tu nueva contraseña"
                false
            }
            newPassword != confirmPassword -> {
                binding.tilConfirmPassword.error = "Las contraseñas no coinciden"
                false
            }
            else -> {
                binding.tilConfirmPassword.error = null
                true
            }
        }
    }
    
    private fun isValidPassword(password: String): Boolean {
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        
        return hasUpperCase && hasLowerCase && hasDigit
    }
    
    private fun changePassword() {
        val currentPassword = binding.etCurrentPassword.text.toString()
        val newPassword = binding.etNewPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        
        // Validar formulario
        val validation = viewModel.validatePasswordForm(currentPassword, newPassword, confirmPassword)
        
        if (!validation.isValid) {
            // Mostrar errores
            binding.tilCurrentPassword.error = validation.errors.find { it.contains("actual") }
            binding.tilNewPassword.error = validation.errors.find { it.contains("nueva") }
            binding.tilConfirmPassword.error = validation.errors.find { it.contains("coinciden") || it.contains("confirmación") }
            return
        }
        
        // Limpiar errores
        binding.tilCurrentPassword.error = null
        binding.tilNewPassword.error = null
        binding.tilConfirmPassword.error = null
        
        // Cambiar contraseña
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            
            try {
                val result = viewModel.changePassword(currentPassword, newPassword)
                
                if (result.isSuccess) {
                    onPasswordChangedListener?.invoke()
                    dismiss()
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                    showError(error)
                }
            } catch (e: Exception) {
                showError("Error al cambiar contraseña: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun observeViewModel() {
        viewModel.errorMessage.observe(this) { error ->
            if (error != null) {
                showError(error)
                viewModel.clearError()
            }
        }
    }
    
    private fun showError(message: String) {
        binding.tvError.apply {
            text = message
            visibility = View.VISIBLE
        }
    }
    
    fun setOnPasswordChangedListener(listener: () -> Unit) {
        onPasswordChangedListener = listener
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance(): ChangePasswordDialogFragment {
            return ChangePasswordDialogFragment()
        }
    }
}
