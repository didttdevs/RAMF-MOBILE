package com.cocido.ramfapp.ui.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cocido.ramfapp.R
import com.cocido.ramfapp.databinding.ActivityImageCropBinding
import java.io.File
import java.io.FileOutputStream

/**
 * Activity para recortar imágenes en forma circular
 * Implementación propia sin dependencias externas
 */
class ImageCropActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityImageCropBinding
    private var sourceUri: Uri? = null
    private var originalBitmap: Bitmap? = null
    private var croppedBitmap: Bitmap? = null
    
    companion object {
        const val EXTRA_SOURCE_URI = "source_uri"
        const val EXTRA_CROPPED_URI = "cropped_uri"
        
        fun startForResult(activity: Activity, sourceUri: Uri, requestCode: Int) {
            val intent = Intent(activity, ImageCropActivity::class.java).apply {
                putExtra(EXTRA_SOURCE_URI, sourceUri)
            }
            activity.startActivityForResult(intent, requestCode)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageCropBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        loadImage()
    }
    
    private fun setupUI() {
        // Configurar toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Recortar Imagen"
        }
        
        // Configurar listeners
        binding.btnCrop.setOnClickListener {
            cropImage()
        }
        
        binding.btnCancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }
    
    private fun loadImage() {
        sourceUri = intent.getParcelableExtra(EXTRA_SOURCE_URI)
        if (sourceUri == null) {
            Toast.makeText(this, "Error: No se encontró la imagen", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        try {
            val inputStream = contentResolver.openInputStream(sourceUri!!)
            originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (originalBitmap != null) {
                binding.cropImageView.setImageBitmap(originalBitmap)
            } else {
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
                finish()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al cargar la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun cropImage() {
        if (originalBitmap == null) return
        
        try {
            // Crear bitmap circular
            val size = minOf(originalBitmap!!.width, originalBitmap!!.height)
            val croppedBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(croppedBitmap)
            
            // Crear path circular
            val path = Path()
            val rect = RectF(0f, 0f, size.toFloat(), size.toFloat())
            path.addOval(rect, Path.Direction.CCW)
            
            // Aplicar clip path
            canvas.clipPath(path)
            
            // Dibujar imagen escalada
            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap!!, size, size, true)
            canvas.drawBitmap(scaledBitmap, 0f, 0f, Paint())
            
            // Guardar imagen recortada
            val croppedUri = saveCroppedImage(croppedBitmap)
            
            if (croppedUri != null) {
                val resultIntent = Intent().apply {
                    putExtra(EXTRA_CROPPED_URI, croppedUri)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Error al guardar la imagen recortada", Toast.LENGTH_SHORT).show()
            }
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error al recortar la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun saveCroppedImage(bitmap: Bitmap): Uri? {
        return try {
            val file = File(cacheDir, "cropped_avatar_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()
            
            Uri.fromFile(file)
        } catch (e: Exception) {
            null
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        setResult(RESULT_CANCELED)
        finish()
        return true
    }
}
