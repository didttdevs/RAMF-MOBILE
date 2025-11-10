package com.cocido.ramfapp.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.cocido.ramfapp.R
import com.cocido.ramfapp.databinding.DialogChangePasswordBinding
import com.cocido.ramfapp.models.ChangePasswordRequest
import com.cocido.ramfapp.utils.Resource
import com.cocido.ramfapp.viewmodels.ProfileViewModel
import com.cocido.ramfapp.viewmodels.ProfileViewModelFactory
import android.content.Context

/**
 * Dialog fragment para cambiar la contraseña del usuario
 * Basado en la funcionalidad de la página web
 */
class ChangePasswordDialogFragment : DialogFragment() {
    
    private var _binding: DialogChangePasswordBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: ProfileViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Configurar el tamaño del diálogo
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        viewModel = ViewModelProvider(requireActivity(), ProfileViewModelFactory(requireContext()))[ProfileViewModel::class.java]
        setupListeners()
        setupTextWatchers()
        setupObservers()
    }
    
    private fun setupListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        
        binding.btnUpdatePassword.setOnClickListener {
            changePassword()
        }
    }
    
    private fun setupTextWatchers() {
        binding.etCurrentPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.tilCurrentPassword.error = null
            }
        })
        
        binding.etNewPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.tilNewPassword.error = null
            }
        })
        
        binding.etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.tilConfirmPassword.error = null
            }
        })
    }
    
    private fun setupObservers() {
        viewModel.changePasswordState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    showLoading(true)
                }
                is Resource.Success -> {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Contraseña cambiada exitosamente", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                is Resource.Error -> {
                    showLoading(false)
                    // El mensaje del Exception ya viene formateado desde ProfileRepository
                    // con el mensaje específico del backend (Password not match, etc.)
                    val errorMessage = state.message ?: "Error al cambiar la contraseña"
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                }
                null -> {}
            }
        }
    }
    
    private fun changePassword() {
        if (!validateForm()) return
        
        val currentPassword = binding.etCurrentPassword.text.toString()
        val newPassword = binding.etNewPassword.text.toString()
        
        // Debug: Verificar que la contraseña no se está normalizando
        android.util.Log.d("ChangePassword", "Sending change password request")
        android.util.Log.d("ChangePassword", "Current password length: ${currentPassword.length}, has uppercase: ${currentPassword.any { it.isUpperCase() }}, preview: '${currentPassword.take(3)}***'")
        android.util.Log.d("ChangePassword", "New password length: ${newPassword.length}, has uppercase: ${newPassword.any { it.isUpperCase() }}, first char: '${newPassword.firstOrNull()}', preview: '${newPassword.take(3)}***'")
        android.util.Log.d("ChangePassword", "New password matches validation: ${isValidPassword(newPassword)}")
        android.util.Log.d("ChangePassword", "Passwords are different: ${currentPassword != newPassword}")
        
        val changePasswordRequest = ChangePasswordRequest(
            password = currentPassword,
            newPassword = newPassword
        )
        
        viewModel.changePassword(changePasswordRequest)
    }
    
    private fun validateForm(): Boolean {
        var isValid = true
        
        // Validar contraseña actual
        val currentPassword = binding.etCurrentPassword.text.toString()
        if (currentPassword.isEmpty()) {
            binding.tilCurrentPassword.error = "La contraseña actual es requerida"
            isValid = false
        } else if (currentPassword.length < 8) {
            binding.tilCurrentPassword.error = "La contraseña debe tener al menos 8 caracteres"
            isValid = false
        } else if (!isValidPassword(currentPassword)) {
            binding.tilCurrentPassword.error = "La contraseña debe contener mayúscula, minúscula y número"
            isValid = false
        }
        
        // Validar nueva contraseña
        val newPassword = binding.etNewPassword.text.toString()
        if (newPassword.isEmpty()) {
            binding.tilNewPassword.error = "La nueva contraseña es requerida"
            isValid = false
        } else if (newPassword.length < 8) {
            binding.tilNewPassword.error = "La contraseña debe tener al menos 8 caracteres"
            isValid = false
        } else if (!isValidPassword(newPassword)) {
            binding.tilNewPassword.error = "La contraseña debe contener al menos una letra mayúscula, una minúscula y un número"
            isValid = false
        }
        
        // Validar confirmación de contraseña
        val confirmPassword = binding.etConfirmPassword.text.toString()
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "La confirmación de contraseña es requerida"
            isValid = false
        } else if (newPassword != confirmPassword) {
            binding.tilConfirmPassword.error = "Las contraseñas no coinciden"
            isValid = false
        }
        
        return isValid
    }
    
    private fun isValidPassword(password: String): Boolean {
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        return hasUpperCase && hasLowerCase && hasDigit
    }
    
    private fun showLoading(show: Boolean) {
        binding.btnUpdatePassword.isEnabled = !show
        binding.btnCancel.isEnabled = !show
        binding.btnUpdatePassword.text = if (show) "Cambiando..." else "Actualizar Contraseña"
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