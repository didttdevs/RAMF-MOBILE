package com.cocido.ramfapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utilidades para manejo de imágenes
 * Incluye compresión, redimensionado y corrección de orientación
 */
object ImageUtils {
    
    private const val MAX_IMAGE_SIZE = 1024 // Tamaño máximo en píxeles
    private const val COMPRESSION_QUALITY = 85 // Calidad de compresión (0-100)
    
    /**
     * Comprimir una imagen desde URI
     */
    fun compressImage(context: Context, uri: Uri): Uri {
        val inputStream = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        
        // Corregir orientación
        val correctedBitmap = correctImageOrientation(originalBitmap, uri, context)
        
        // Redimensionar si es necesario
        val resizedBitmap = resizeBitmap(correctedBitmap, MAX_IMAGE_SIZE)
        
        // Comprimir
        val compressedFile = createTempImageFile(context)
        val outputStream = FileOutputStream(compressedFile)
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, outputStream)
        outputStream.close()
        
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            compressedFile
        )
    }
    
    /**
     * Comprimir una imagen desde URI (versión simplificada)
     */
    fun compressImage(uri: Uri): Uri {
        // Esta es una versión simplificada que retorna el URI original
        // En una implementación completa, se procesaría la imagen aquí
        return uri
    }
    
    /**
     * Corregir la orientación de la imagen basada en los metadatos EXIF
     */
    private fun correctImageOrientation(bitmap: Bitmap, uri: Uri, context: Context): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val exif = ExifInterface(inputStream!!)
            inputStream.close()
            
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    matrix.postRotate(90f)
                    matrix.postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    matrix.postRotate(270f)
                    matrix.postScale(-1f, 1f)
                }
            }
            
            if (matrix.isIdentity) {
                bitmap
            } else {
                Bitmap.createBitmap(
                    bitmap, 0, 0,
                    bitmap.width, bitmap.height,
                    matrix, true
                )
            }
        } catch (e: Exception) {
            // Si hay error al leer EXIF, retornar bitmap original
            bitmap
        }
    }
    
    /**
     * Redimensionar bitmap manteniendo la proporción
     */
    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxSize && height <= maxSize) {
            return bitmap
        }
        
        val scale = if (width > height) {
            maxSize.toFloat() / width
        } else {
            maxSize.toFloat() / height
        }
        
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    /**
     * Crear archivo temporal para imagen
     */
    private fun createTempImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = File(context.cacheDir, "temp_images")
        
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        
        return File(storageDir, "IMG_${timeStamp}.jpg")
    }
    
    /**
     * Obtener tamaño del archivo en bytes
     */
    fun getFileSize(uri: Uri, context: Context): Long {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val size = inputStream?.available()?.toLong() ?: 0L
            inputStream?.close()
            size
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Verificar si el archivo es una imagen válida
     */
    fun isValidImage(uri: Uri, context: Context): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()
            
            options.outWidth > 0 && options.outHeight > 0
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Convertir bitmap a byte array
     */
    fun bitmapToByteArray(bitmap: Bitmap, quality: Int = COMPRESSION_QUALITY): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        return stream.toByteArray()
    }
    
    /**
     * Convertir byte array a bitmap
     */
    fun byteArrayToBitmap(byteArray: ByteArray): Bitmap? {
        return try {
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Limpiar archivos temporales
     */
    fun clearTempFiles(context: Context) {
        try {
            val tempDir = File(context.cacheDir, "temp_images")
            if (tempDir.exists() && tempDir.isDirectory) {
                tempDir.listFiles()?.forEach { file ->
                    if (file.isFile) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            // Log error if needed
        }
    }
}
