package com.example.rinconsur.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RinconSurRepositoryTest {

    private lateinit var fakeUsuarios: FakeUsuarioRemotoApi
    private lateinit var repo: RinconSurRepository

    @Before
    fun preparar() {
        fakeUsuarios = FakeUsuarioRemotoApi()
        repo = RinconSurRepository(
            usuarioApi = fakeUsuarios,
            sesionApi = FakeSesionRemotaApi(),
            visitanteApi = FakeVisitanteRemotoApi(),
            analiticaApi = FakeAnaliticaRemotaApi()
        )
    }

    @Test
    fun login_devuelveUsuarioYConvierteRolCorrectamente() = runBlocking {
        val dto = repo.login("  rvaldes.4419@gmail.com ", "Daniela123!")
        assertEquals(1L, dto.id)
        assertEquals("ADMIN", dto.rol)
        assertEquals("Daniela Gonzales", dto.aUsuario().nombre)
    }

    @Test
    fun crearColaborador_enviaDatosNormalizados() = runBlocking {
        val msg = repo.crearColaborador("  Renata Valdés ", "RENATA@GMAIL.COM", "21045678-3", "P-04", "OBSERVADOR")
        assertTrue(msg.contains("renata@gmail.com"))
        assertEquals(3, repo.listarUsuarios().size)

        val resp = repo.generarClaveTemporal(4L, 1L)
        assertFalse(resp.containsKey("clave"))
        assertTrue(resp["mensaje"].toString().contains("correo"))
    }

    @Test
    fun sesionesVisitantesYAnalitica_operanContraApiRemota() = runBlocking {
        val sesion = repo.crearSesion("A2", "Acceso Lateral", "2026-11-19", 4L)
        assertEquals(2L, sesion.id)

        val resumen = repo.obtenerResumenVisitantes("2026-11-19", 1L)
        assertEquals(4, resumen.total)
        assertEquals(1, repo.listarVisitantesEnTienda("2026-11-19", 1L).size)

        val flujos = repo.listarFlujo("2026-11-01", "2026-11-19")
        assertEquals(100, flujos.first().ingresos)
    }

    @Test
    fun sinConexion_lanzaMensajeAmigable() {
        fakeUsuarios.sinConexion = true
        val ex = assertThrows(Exception::class.java) {
            runBlocking { repo.listarUsuarios() }
        }
        assertTrue(ex.message!!.contains("No hay conexión con el servidor"))
    }
}
