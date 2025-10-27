package com.cocido.ramfapp.ui.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.cocido.ramfapp.R
import com.cocido.ramfapp.databinding.ActivityImageCropBinding
import com.cocido.ramfapp.utils.ImageUtils
import java.io.File
import java.io.FileOutputStream

/**
 * Activity para recortar imágenes de avatar
 * Basado en la funcionalidad de la página web
 */
class ImageCropActivity : BaseActivity() {
    
    private lateinit var binding: ActivityImageCropBinding
    private var originalImageUri: Uri? = null
    private var croppedBitmap: Bitmap? = null
    
    override fun requiresAuthentication(): Boolean {
        return true
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageCropBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        loadImage()
        setupListeners()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Recortar Imagen"
        }
    }
    
    private fun loadImage() {
        val imageUriString = intent.getStringExtra("image_uri")
        if (imageUriString != null) {
            originalImageUri = Uri.parse(imageUriString)
            displayImage()
        } else {
            Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun displayImage() {
        originalImageUri?.let { uri ->
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                
                if (bitmap != null) {
                    binding.imageView.setImageBitmap(bitmap)
                    binding.imageView.visibility = View.VISIBLE
                } else {
                    showError("No se pudo cargar la imagen")
                }
            } catch (e: Exception) {
                showError("Error al procesar la imagen: ${e.message}")
            }
        }
    }
    
    private fun setupListeners() {
        binding.btnCrop.setOnClickListener {
            cropImage()
        }
        
        binding.btnCancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
    
    private fun cropImage() {
        try {
            // Por simplicidad, usamos la imagen completa
            // En una implementación real, aquí se implementaría el recorte
            originalImageUri?.let { uri ->
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                
                if (bitmap != null) {
                    // Crear una versión circular de la imagen
                    val circularBitmap = ImageUtils.createCircularBitmap(bitmap)
                    
                    // Guardar la imagen recortada
                    val croppedFile = saveCroppedImage(circularBitmap)
                    if (croppedFile != null) {
                        val resultIntent = Intent().apply {
                            putExtra("cropped_image_uri", Uri.fromFile(croppedFile))
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    } else {
                        showError("Error al guardar la imagen recortada")
                    }
                } else {
                    showError("Error al procesar la imagen")
                }
            }
        } catch (e: Exception) {
            showError("Error al recortar la imagen: ${e.message}")
        }
    }
    
    private fun saveCroppedImage(bitmap: Bitmap): File? {
        return try {
            val croppedFile = File(cacheDir, "cropped_avatar_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(croppedFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            croppedFile
        } catch (e: Exception) {
            null
        }
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        setResult(Activity.RESULT_CANCELED)
        finish()
        return true
    }
}