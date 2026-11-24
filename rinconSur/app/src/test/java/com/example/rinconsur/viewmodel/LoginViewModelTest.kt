package com.example.rinconsur.viewmodel

import android.app.Application
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel

    @Before
    fun preparar() {
        viewModel = LoginViewModel(Application())
    }

    @Test
    fun actualizarCorreo_conCorreoInvalido_muestraErrorEnVivoYLoQuitaAlCorregir() {
        viewModel.actualizarCorreo("sin_arroba")
        assertNotNull(viewModel.uiState.value.errorCorreo)

        viewModel.actualizarCorreo("rvaldes.4419@gmail.com")
        assertNull(viewModel.uiState.value.errorCorreo)
    }

    @Test
    fun alternarVisibilidadPassword_cambiaElEstadoDelOjo() {
        assertFalse(viewModel.uiState.value.passwordVisible)

        viewModel.alternarVisibilidadPassword()
        assertTrue(viewModel.uiState.value.passwordVisible)

        viewModel.alternarVisibilidadPassword()
        assertFalse(viewModel.uiState.value.passwordVisible)
    }

    @Test
    fun iniciarSesion_conCamposVacios_muestraErroresYNoLlamaAlServidor() {
        viewModel.iniciarSesion()

        val estado = viewModel.uiState.value
        assertEquals("Ingresa tu correo electrónico.", estado.errorCorreo)
        assertEquals("Ingresa tu contraseña.", estado.errorPassword)
        assertFalse(estado.cargando)
        assertNull(estado.usuarioAutenticado)
    }

    @Test
    fun validadorLogin_soloRevisaCorreoYContrasenaNoVacia() {
        assertEquals("Correo incorrecto.", ValidadorLogin.validarCorreo("correo@"))
        assertNull(ValidadorLogin.validarCorreo("daniela@rinconsur.cl"))
        assertEquals("Ingresa tu contraseña.", ValidadorLogin.validarContrasena("   "))
        assertNull(ValidadorLogin.validarContrasena("abc"))
    }
}