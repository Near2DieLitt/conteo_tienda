package com.example.rinconsur.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorColaboradorTest {

    @Test
    fun nombre_exigeNombreYApellidoSoloConLetras() {
        assertEquals("Ingresa el nombre completo", ValidadorColaborador.validarNombre("  "))
        assertEquals("Ingresa nombre y apellido", ValidadorColaborador.validarNombre("Renata"))
        assertEquals("Solo letras y espacios", ValidadorColaborador.validarNombre("Jose123 Bustamante"))
        assertNull(ValidadorColaborador.validarNombre("Jose Bustamante"))
    }

    @Test
    fun correo_debeSerUnCorreoRealValido() {
        assertEquals("Ingresa el correo", ValidadorColaborador.validarCorreo(""))
        assertNotNull(ValidadorColaborador.validarCorreo("correo_invalido"))
        assertNull(ValidadorColaborador.validarCorreo("babubub4a@gmail.com"))
    }

    @Test
    fun rut_vacioOConDigitoIncorrecto_esRechazado() {
        assertEquals("Ingresa el RUT", ValidadorColaborador.validarRut(""))
        assertEquals("RUT inválido", ValidadorColaborador.validarRut("21550721-9"))
        assertNull(ValidadorColaborador.validarRut("21.550.721-1"))
    }

    @Test
    fun erroresColaborador_indicaSiHayAlgunError() {
        assertTrue(ErroresColaborador(rut = "RUT inválido").hayErrores)
        assertTrue(!ErroresColaborador().hayErrores)
    }
}