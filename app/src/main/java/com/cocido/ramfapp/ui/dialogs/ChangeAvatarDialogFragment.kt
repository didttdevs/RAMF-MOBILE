package com.cocido.ramfapp.ui.dialogs

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.cocido.ramfapp.R
import com.cocido.ramfapp.databinding.DialogChangeAvatarBinding
import com.cocido.ramfapp.utils.AuthManager
import com.cocido.ramfapp.viewmodels.ProfileViewModel
import com.cocido.ramfapp.viewmodels.ProfileViewModelFactory

/**
 * Dialog fragment para cambiar el avatar del usuario
 * Basado en la funcionalidad de la página web
 */
class ChangeAvatarDialogFragment : DialogFragment() {
    
    private var _binding: DialogChangeAvatarBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: ProfileViewModel
    private var selectedImageUri: Uri? = null
    
    // Image picker launchers
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { 
            selectedImageUri = it
            loadImagePreview(it)
        }
    }
    
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Handle camera result
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogChangeAvatarBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity(), ProfileViewModelFactory(requireContext()))[ProfileViewModel::class.java]
        setupListeners()
        loadCurrentAvatar()
        setupObservers()
    }
    
    private fun setupListeners() {
        binding.btnTakePhoto.setOnClickListener {
            // TODO: Implement camera capture
            Toast.makeText(requireContext(), "Funcionalidad de cámara próximamente", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnChooseFromGallery.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
        
        binding.btnRemoveAvatar.setOnClickListener {
            selectedImageUri = null
            binding.ivCurrentAvatar.setImageResource(R.drawable.ic_default_profile)
        }
        
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        
        binding.btnSaveAvatar.setOnClickListener {
            saveAvatar()
        }
    }
    
    private fun loadCurrentAvatar() {
        val user = AuthManager.getCurrentUser()
        user?.avatar?.let { avatarUrl ->
            Glide.with(this)
                .load(avatarUrl)
                .transform(CircleCrop())
                .placeholder(R.drawable.ic_default_profile)
                .error(R.drawable.ic_default_profile)
                .into(binding.ivCurrentAvatar)
        }
    }
    
    private fun loadImagePreview(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .transform(CircleCrop())
            .into(binding.ivCurrentAvatar)
    }
    
    private fun saveAvatar() {
        selectedImageUri?.let { uri ->
            viewModel.updateAvatar(uri)
        } ?: run {
            // Remove avatar
            viewModel.removeAvatar()
        }
    }
    
    private fun setupObservers() {
        viewModel.avatarUpdateState.observe(this) { state ->
            when (state) {
                is com.cocido.ramfapp.utils.Resource.Loading -> {
                    binding.btnSaveAvatar.isEnabled = false
                    binding.btnSaveAvatar.text = "Guardando..."
                }
                is com.cocido.ramfapp.utils.Resource.Success -> {
                    binding.btnSaveAvatar.isEnabled = true
                    binding.btnSaveAvatar.text = "Guardar"
                    Toast.makeText(requireContext(), "Avatar actualizado correctamente", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                is com.cocido.ramfapp.utils.Resource.Error -> {
                    binding.btnSaveAvatar.isEnabled = true
                    binding.btnSaveAvatar.text = "Guardar"
                    Toast.makeText(requireContext(), "Error: ${state.message}", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance(): ChangeAvatarDialogFragment {
            return ChangeAvatarDialogFragment()
        }
    }
}

