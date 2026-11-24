package com.example.rinconsur.model

import android.util.Log
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RequisitosClave(
    val largoMinimo: Boolean = false,
    val tieneMayuscula: Boolean = false,
    val tieneMinuscula: Boolean = false,
    val tieneNumero: Boolean = false,
    val tieneEspecial: Boolean = false
) {
    val cumpleTodo: Boolean
        get() = largoMinimo && tieneMayuscula && tieneMinuscula && tieneNumero && tieneEspecial

    val cumplidos: Int
        get() = listOf(largoMinimo, tieneMayuscula, tieneMinuscula, tieneNumero, tieneEspecial).count { it }
}

object ValidadorClave {
    const val CARACTERES_ESPECIALES = "!@#\$%&*._-+"

    fun evaluar(clave: String): RequisitosClave = RequisitosClave(
        largoMinimo = clave.length >= 8,
        tieneMayuscula = clave.any { it.isUpperCase() },
        tieneMinuscula = clave.any { it.isLowerCase() },
        tieneNumero = clave.any { it.isDigit() },
        tieneEspecial = clave.any { it in CARACTERES_ESPECIALES || (!it.isLetterOrDigit() && !it.isWhitespace()) }
    )

    fun validar(clave: String): String? {
        val r = evaluar(clave)
        return when {
            clave.isBlank() -> "Ingresa una contraseña"
            !r.largoMinimo -> "La clave debe tener al menos 8 caracteres"
            !r.tieneMayuscula -> "Debe incluir al menos una letra mayúscula"
            !r.tieneMinuscula -> "Debe incluir al menos una letra minúscula"
            !r.tieneNumero -> "Debe incluir al menos un número"
            !r.tieneEspecial -> "Debe incluir al menos un carácter especial (!@#\$%&*)"
            else -> null
        }
    }

    fun generarClaveTemporalSegura(): String {
        val r = SecureRandom()
        val mayus = "ABCDEFGHJKMNPQRSTUVWXYZ"
        val minus = "abcdefghjkmnpqrstuvwxyz"
        val nums = "23456789"
        val esp = "!@#\$%&*"
        val todos = mayus + minus + nums + esp

        val chars = mutableListOf(
            mayus[r.nextInt(mayus.length)],
            minus[r.nextInt(minus.length)],
            nums[r.nextInt(nums.length)],
            esp[r.nextInt(esp.length)]
        )
        repeat(6) { chars.add(todos[r.nextInt(todos.length)]) }
        chars.shuffle(r)
        return chars.joinToString("")
    }
}

data class CorreoSimuladoItem(
    val para: String,
    val asunto: String,
    val mensaje: String,
    val codigoDestacado: String,
    val hora: String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
)

object BandejaCorreoSimulado {
    private val _bandeja = mutableListOf<CorreoSimuladoItem>()
    val bandeja: List<CorreoSimuladoItem> get() = _bandeja.toList()

    fun enviar(para: String, asunto: String, mensaje: String, codigoDestacado: String) {
        val correo = CorreoSimuladoItem(
            para = para.trim().lowercase(),
            asunto = asunto,
            mensaje = mensaje,
            codigoDestacado = codigoDestacado
        )
        _bandeja.removeAll { it.para == correo.para && it.asunto == asunto }
        _bandeja.add(0, correo)
        Log.i("CORREO_SIMULADO", "==========================================")
        Log.i("CORREO_SIMULADO", "PARA: ${correo.para}")
        Log.i("CORREO_SIMULADO", "ASUNTO: $asunto")
        Log.i("CORREO_SIMULADO", "CLAVE / CÓDIGO: $codigoDestacado")
        Log.i("CORREO_SIMULADO", "MENSAJE: $mensaje")
        Log.i("CORREO_SIMULADO", "==========================================")
    }

    fun ultimoPara(correo: String): CorreoSimuladoItem? =
        _bandeja.firstOrNull { it.para.equals(correo.trim(), ignoreCase = true) }
}