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
            ImageDecoder.decodeBitmap(origen) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true
                val ancho = info.size.width
                val alto = info.size.height
                val mayor = maxOf(ancho, alto)
                if (mayor > LADO_MAXIMO * 2) {
                    val ratio = mayor / LADO_MAXIMO
                    decoder.setTargetSampleSize(ratio.coerceAtLeast(1))
                }
            }
        } else {
            context.contentResolver.openInputStream(uri)?.use { flujo ->
                BitmapFactory.decodeStream(flujo)
            }
        }
        val corregido = bitmap?.let { corregirOrientacionExif(context, uri, it) }
        corregido?.let { asegurarArgb8888(achicar(it)) }
    } catch (e: Throwable) {
        e.printStackTrace()
        null
    }

    private fun asegurarArgb8888(bitmap: Bitmap): Bitmap {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bitmap.config == Bitmap.Config.HARDWARE) {
            return bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: bitmap
        }
        return if (bitmap.config != Bitmap.Config.ARGB_8888) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: bitmap
        } else {
            bitmap
        }
    }

    fun corregirRotacionCamara(bitmap: Bitmap): Bitmap {
        val seguro = asegurarArgb8888(bitmap)
        return if (seguro.width > seguro.height) {
            val matrix = Matrix().apply { postRotate(90f) }
            Bitmap.createBitmap(seguro, 0, 0, seguro.width, seguro.height, matrix, true)
        } else {
            seguro
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
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return bitmap
    }

    fun aBytes(bitmap: Bitmap): ByteArray {
        val chico = achicar(asegurarArgb8888(bitmap))
        val salida = ByteArrayOutputStream()
        chico.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, salida)
        return salida.toByteArray()
    }

    fun aBitmap(bytes: ByteArray): Bitmap? = try {
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.let { asegurarArgb8888(it) }
    } catch (e: Throwable) {
        null
    }

    fun calcularDimensionesEscaladas(ancho: Int, alto: Int, maxLado: Int = LADO_MAXIMO): Pair<Int, Int> {
        if (ancho <= 0 || alto <= 0) return Pair(maxLado, maxLado)
        val ladoMayor = maxOf(ancho, alto)
        if (ladoMayor <= maxLado) return Pair(ancho, alto)
        val escala = maxLado.toFloat() / ladoMayor.toFloat()
        return Pair(
            (ancho * escala).toInt().coerceAtLeast(1),
            (alto * escala).toInt().coerceAtLeast(1)
        )
    }

    private fun achicar(bitmap: Bitmap): Bitmap {
        val (nuevoAncho, nuevoAlto) = calcularDimensionesEscaladas(bitmap.width, bitmap.height, LADO_MAXIMO)
        if (nuevoAncho == bitmap.width && nuevoAlto == bitmap.height) return bitmap
        return Bitmap.createScaledBitmap(bitmap, nuevoAncho, nuevoAlto, true)
    }
}