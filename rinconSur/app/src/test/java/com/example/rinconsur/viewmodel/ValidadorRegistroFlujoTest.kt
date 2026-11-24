package com.example.rinconsur.viewmodel

import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorRegistroFlujoTest {

    @Test
    fun registroValido_noRetornaErrores() {
        val form = FormFlujoState(
            acceso = "A1",
            fecha = "2026-11-18",
            ingresos = "100",
            compradores = "40",
            transitoJoven = "20",
            transitoAdulto = "25",
            transitoMayor = "15"
        )
        assertTrue(ValidadorRegistroFlujo.validar(form, "2026-11-19").isEmpty())
    }

    @Test
    fun fechaFutura_retornaErrorEnFecha() {
        val form = FormFlujoState(acceso = "A1", fecha = "2026-11-25", ingresos = "50")
        assertTrue(ValidadorRegistroFlujo.validar(form, "2026-11-19").containsKey("fecha"))
    }

    @Test
    fun ingresosCeroONegativos_retornaErrorEnIngresos() {
        val form = FormFlujoState(acceso = "A2", fecha = "2026-11-19", ingresos = "0")
        assertTrue(ValidadorRegistroFlujo.validar(form, "2026-11-19").containsKey("ingresos"))
    }

    @Test
    fun sumaMayorQueIngresos_retornaErrorEnTotal() {
        val form = FormFlujoState(
            acceso = "A3", fecha = "2026-11-19", ingresos = "20",
            compradores = "10", transitoJoven = "8", transitoAdulto = "5", transitoMayor = "2"
        )
        assertTrue(ValidadorRegistroFlujo.validar(form, "2026-11-19").containsKey("total"))
    }
}