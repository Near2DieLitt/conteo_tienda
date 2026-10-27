package com.example.rinconsur.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import java.io.ByteArrayOutputStream

object ConvertidorImagen {

    private const val LADO_MAXIMO = 400
    private const val CALIDAD_JPEG = 80

    fun desdeUri(context: Context, uri: Uri): Bitmap? = try {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val origen = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(origen) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            context.contentResolver.openInputStream(uri).use { flujo ->
                BitmapFactory.decodeStream(flujo)
            }
        }
        bitmap?.let { corregirOrientacionExif(context, uri, it) }
    } catch (e: Exception) {
        null
    }

    fun corregirRotacionCamara(bitmap: Bitmap): Bitmap {
        return if (bitmap.width > bitmap.height) {
            val matrix = Matrix().apply { postRotate(90f) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    }

    private fun corregirOrientacionExif(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                val grados = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
                if (grados != 0f) {
                    val matrix = Matrix().apply { postRotate(grados) }
                    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }

    fun aBytes(bitmap: Bitmap): ByteArray{
        val chico = achicar(bitmap)
        val salida = ByteArrayOutputStream()
        chico.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, salida)
        return salida.toByteArray()
    }

    fun aBitmap(bytes: ByteArray): Bitmap? =
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    private fun achicar(bitmap: Bitmap): Bitmap{
        val ladoMayor = maxOf(bitmap.width, bitmap.height)
        if (ladoMayor <= LADO_MAXIMO) return bitmap
        val escala = LADO_MAXIMO.toFloat()
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * escala).toInt(),
            (bitmap.height * escala).toInt(),
            true
        )
    }
}
