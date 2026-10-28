package com.example.rinconsur.data


import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object Fechas {
    val meses = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )
    private val dias = listOf(
        "Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"
    )

    private fun formato() = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun aTexto(cal: Calendar): String = formato().format(cal.time)

    fun deTexto(texto: String): Calendar =
        Calendar.getInstance().apply { time = formato().parse(texto)!! }

    fun hoy(): String = aTexto(Calendar.getInstance())

    fun ayer(): String = aTexto(Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) })

    fun fecha(anio: Int, mes: Int, dia: Int): String =
        String.format(Locale.US, "%04d-%02d-%02d", anio, mes + 1, dia)

    fun fechaLarga(texto: String): String {
        val c = deTexto(texto)
        return "${dias[c.get(Calendar.DAY_OF_WEEK) - 1]}, " +
                "${c.get(Calendar.DAY_OF_MONTH)} de ${meses[c.get(Calendar.MONTH)].lowercase()}"
    }

    fun inicioSemana(texto: String): String {
        val c = deTexto(texto)
        val desfase = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7
        c.add(Calendar.DAY_OF_MONTH, -desfase)
        return aTexto(c)
    }

    fun finSemana(texto: String): String {
        val c = deTexto(inicioSemana(texto))
        c.add(Calendar.DAY_OF_MONTH, 6)
        return aTexto(c)
    }
}