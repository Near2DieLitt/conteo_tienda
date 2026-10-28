package com.example.rinconsur.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
class SesionPreferencias (context: Context){
    private val prefs: SharedPreferences = context.getSharedPreferences("sesion", Context.MODE_PRIVATE)

    companion object{
        private const val CLAVE_ID_USUARIO = "id_usuario"
        private const val SIN_SESION = -1L
    }

    fun guardar(idUsuario: Long){
        prefs.edit{ putLong(CLAVE_ID_USUARIO, idUsuario) }
    }

    fun leer(): Long = prefs.getLong(CLAVE_ID_USUARIO, SIN_SESION)

    fun borrar(){
        prefs.edit{remove(CLAVE_ID_USUARIO)}
    }

    private fun claveEntrada(correo: String) = "entrada_activa_${correo.trim().lowercase()}"

    fun guardarEntradaActiva(correo: String, codigo: String) {
        prefs.edit { putString(claveEntrada(correo), codigo) }
    }

    fun obtenerEntradaActiva(correo: String): String? =
        prefs.getString(claveEntrada(correo), null)

    fun borrarEntradaActiva(correo: String) {
        prefs.edit { remove(claveEntrada(correo)) }
    }
}
