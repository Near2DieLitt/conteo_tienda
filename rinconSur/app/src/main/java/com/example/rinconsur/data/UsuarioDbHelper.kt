package com.example.rinconsur.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.rinconsur.model.Colaborador
import com.example.rinconsur.model.ConteoSesion
import com.example.rinconsur.model.Perfiles
import com.example.rinconsur.model.RangoEtario
import com.example.rinconsur.model.RegistroFlujo
import com.example.rinconsur.model.ResultadoBoleta
import com.example.rinconsur.model.ResumenFlujo
import com.example.rinconsur.model.ResumenVisitantes
import com.example.rinconsur.model.RolUsuario
import com.example.rinconsur.model.SesionAcceso
import com.example.rinconsur.model.Usuario
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UsuarioDbHelper (context: Context):
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD){
    companion object{
        private const val NOMBRE_BD = "rinconsur.db"
        private const val VERSION_BD = 7

        private const val TABLA_USUARIOS = "usuarios"
        private const val USU_ID = "id_usuario"
        private const val USU_NOMBRE = "nombre"
        private const val USU_CORREO = "correo"
        private const val USU_CONTRASENA = "password_hash"
        private const val USU_ROL = "rol"
        private const val USU_AVATAR = "avatar"
        private const val USU_DESCRIPCION = "descripcion"
        private const val USU_RUT = "rut"
        private const val USU_PERFIL = "perfil"
        private const val USU_ACTIVO = "activo"

        private const val TABLA_SESIONES = "sesiones"
        private const val TABLA_CONTEOS = "conteos"

        private const val TABLA_FLUJO = "registros_flujo"

        private const val TABLA_VISITANTES = "visitantes"
        private const val TABLA_BOLETAS = "boletas"
    }

    override fun onConfigure(db: SQLiteDatabase){
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase){
        val crearTablaUsuarios = """
            CREATE TABLE $TABLA_USUARIOS(
                $USU_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $USU_NOMBRE VARCHAR(100) NOT NULL,
                $USU_CORREO VARCHAR(150) UNIQUE NOT NULL,
                $USU_CONTRASENA VARCHAR(255) NOT NULL,
                $USU_ROL VARCHAR(50) NOT NULL,
                $USU_AVATAR BLOB,
                $USU_DESCRIPCION VARCHAR(255),
                $USU_RUT VARCHAR(12) UNIQUE NOT NULL,
                $USU_PERFIL VARCHAR(10) NOT NULL,
                $USU_ACTIVO INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent()
        db.execSQL(crearTablaUsuarios)

        val crearTablaSesiones = """
            CREATE TABLE $TABLA_SESIONES(
                id_sesion INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo VARCHAR(50) NOT NULL,
                nombre VARCHAR(100) NOT NULL,
                fecha VARCHAR(20) NOT NULL,
                activo INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent()
        db.execSQL(crearTablaSesiones)

        val crearTablaConteos = """
            CREATE TABLE $TABLA_CONTEOS(
                id_conteo INTEGER PRIMARY KEY AUTOINCREMENT,
                id_sesion INTEGER NOT NULL,
                nombre VARCHAR(100) NOT NULL,
                horario VARCHAR(50) NOT NULL,
                completado INTEGER NOT NULL DEFAULT 0,
                activo INTEGER NOT NULL DEFAULT 1,
                FOREIGN KEY(id_sesion) REFERENCES $TABLA_SESIONES(id_sesion)
            )
        """.trimIndent()
        db.execSQL(crearTablaConteos)

        val crearTablaFlujo = """
            CREATE TABLE $TABLA_FLUJO(
                id_registro INTEGER PRIMARY KEY AUTOINCREMENT,
                acceso VARCHAR(50) NOT NULL,
                fecha VARCHAR(20) NOT NULL,
                ingresos INTEGER NOT NULL,
                compradores INTEGER NOT NULL,
                transito_joven INTEGER NOT NULL,
                transito_adulto INTEGER NOT NULL,
                transito_mayor INTEGER NOT NULL,
                activo INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent()
        db.execSQL(crearTablaFlujo)

        val crearTablaVisitantes = """
            CREATE TABLE $TABLA_VISITANTES(
                id_visitante INTEGER PRIMARY KEY AUTOINCREMENT,
                fecha VARCHAR(20) NOT NULL,
                hora VARCHAR(20) NOT NULL,
                estado VARCHAR(50) NOT NULL,
                boleta VARCHAR(50)
            )
        """.trimIndent()
        db.execSQL(crearTablaVisitantes)

        val crearTablaBoletas = """
            CREATE TABLE $TABLA_BOLETAS(
                id_boleta INTEGER PRIMARY KEY AUTOINCREMENT,
                folio VARCHAR(50) UNIQUE NOT NULL,
                fecha VARCHAR(20) NOT NULL,
                id_visitante INTEGER NOT NULL,
                FOREIGN KEY(id_visitante) REFERENCES $TABLA_VISITANTES(id_visitante)
            )
        """.trimIndent()
        db.execSQL(crearTablaBoletas)

        sembrarUsuario(db, "Daniela Gonzales", "danielle@rinconsur.cl", "20123456-5", "Danielle123",
            Perfiles.ADMINISTRADOR)
        sembrarUsuario(db, "Nicole Soto", "nicole@rinconsur.cl", "19876543-0", "Nicole123", Perfiles.SUPERVISOR_PISO)
        sembrarUsuario(db, "Renata Valdés", "renata@rinconsur.cl", "21045678-3", "Renata123", Perfiles.SUPERVISOR_CAJA)
        sembrarUsuario(db, "Matías Olavarría", "matias@rinconsur.cl", "20456789-1", "Matias123", Perfiles.OBSERVADOR)
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int){
        db.execSQL("DROP TABLE IF EXISTS $TABLA_BOLETAS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_VISITANTES")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_FLUJO")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_CONTEOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_SESIONES")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_USUARIOS")
        onCreate(db)
    }

    private fun sembrarUsuario(
        db: SQLiteDatabase, nombre: String, correo: String, rut: String,
        clave: String, perfil: com.example.rinconsur.model.PerfilAcceso
    ) {
        val hash = BCrypt.withDefaults().hashToString(12, clave.toCharArray())
        val valores = ContentValues().apply {
            put(USU_NOMBRE, nombre)
            put(USU_CORREO, correo)
            put(USU_CONTRASENA, hash)
            put(USU_ROL, perfil.rol.name)
            put(USU_RUT, rut)
            put(USU_PERFIL, perfil.codigo)
            put(USU_ACTIVO, 1)
        }
        db.insert(TABLA_USUARIOS, null, valores)
    }

    fun obtenerUsuarioPorCorreo(correo: String): Usuario? {
        val db = this.readableDatabase
        var usuario: Usuario? = null

        val cursor: Cursor = db.rawQuery(
            "SELECT $USU_NOMBRE, $USU_CORREO, $USU_CONTRASENA, $USU_ROL, $USU_AVATAR, $USU_DESCRIPCION, " +
                    "$USU_ID, $USU_RUT, $USU_PERFIL, $USU_ACTIVO " +
                    "FROM $TABLA_USUARIOS WHERE LOWER(TRIM($USU_CORREO)) = LOWER(TRIM(?))",
            arrayOf(correo)
        )

        if (cursor.moveToFirst()){
            val rolString = cursor.getString(3)
            val rolEnum = try { RolUsuario.valueOf(rolString) } catch (e: Exception){
                RolUsuario.OBSERVADOR
            }

            usuario = Usuario(
                nombre = cursor.getString(0),
                correo = cursor.getString(1),
                passwordHash = cursor.getString(2),
                rol = rolEnum,
                avatar = cursor.getBlob(4),
                descripcion = cursor.getString(5),
                id = cursor.getInt(6),
                rut = cursor.getString(7),
                perfil = cursor.getString(8),
                activo = cursor.getInt(9) == 1
            )
        }
        cursor.close()

        return usuario
    }

    fun actualizarAvatar(correo: String, avatar: ByteArray){
        val db = this.writableDatabase
        val valores = ContentValues().apply { put(USU_AVATAR, avatar) }
        db.update(
            TABLA_USUARIOS,
            valores,
            "LOWER(TRIM($USU_CORREO)) = LOWER(TRIM(?))",
            arrayOf(correo)
        )
    }

    fun actualizarDescripcion(correo: String, descripcion: String){
        val db = this.writableDatabase
        val valores = ContentValues().apply {
            put(USU_DESCRIPCION, descripcion)
        }
        db.update(
            TABLA_USUARIOS,
            valores,
            "LOWER(TRIM($USU_CORREO)) = LOWER(TRIM(?))",
            arrayOf(correo)
        )
    }

    fun verificarCorreoExistente(correo: String): Boolean{
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT 1 FROM $TABLA_USUARIOS WHERE LOWER(TRIM($USU_CORREO)) = LOWER(TRIM(?)) AND $USU_ACTIVO = 1", arrayOf(correo))
        val existe = cursor.moveToFirst()
        cursor.close()
        return existe
    }

    fun actualizarContrasena(correo: String, nuevaContrasenaHash: String): Boolean{
        val db = this.writableDatabase
        val valores = ContentValues().apply { put(USU_CONTRASENA, nuevaContrasenaHash) }
        val filasAfectadas = db.update(TABLA_USUARIOS, valores, "$USU_CORREO = ?", arrayOf(correo))
        return filasAfectadas > 0
    }

    // ---------- Equipo (colaboradores) ----------

    private fun colaboradorDe(c: Cursor): Colaborador {
        val perfil = Perfiles.deCodigo(c.getString(4))
        return Colaborador(
            id = c.getInt(0),
            nombre = c.getString(1),
            correo = c.getString(2),
            rut = c.getString(3),
            perfil = perfil,
            rol = perfil.rol,
            activo = c.getInt(5) == 1
        )
    }

    private val columnasColaborador = "$USU_ID, $USU_NOMBRE, $USU_CORREO, $USU_RUT, $USU_PERFIL, $USU_ACTIVO"

    fun obtenerColaboradores(): List<Colaborador> {
        val lista = mutableListOf<Colaborador>()
        val c = readableDatabase.rawQuery(
            "SELECT $columnasColaborador FROM $TABLA_USUARIOS ORDER BY $USU_ACTIVO DESC, $USU_PERFIL, $USU_NOMBRE",
            null
        )
        while (c.moveToNext()) lista.add(colaboradorDe(c))
        c.close()
        return lista
    }

    fun obtenerColaboradorPorCorreo(correo: String): Colaborador? {
        val c = readableDatabase.rawQuery(
            "SELECT $columnasColaborador FROM $TABLA_USUARIOS WHERE LOWER(TRIM($USU_CORREO)) = LOWER(TRIM(?))",
            arrayOf(correo)
        )
        val r = if (c.moveToFirst()) colaboradorDe(c) else null
        c.close()
        return r
    }

    fun obtenerColaboradorPorId(id: Int): Colaborador? {
        val c = readableDatabase.rawQuery(
            "SELECT $columnasColaborador FROM $TABLA_USUARIOS WHERE $USU_ID = ?",
            arrayOf(id.toString())
        )
        val r = if (c.moveToFirst()) colaboradorDe(c) else null
        c.close()
        return r
    }

    /** Verdad en BD: el correo existe, está activo y es ADMIN. */
    fun esAdminActivo(correo: String): Boolean {
        val c = readableDatabase.rawQuery(
            "SELECT 1 FROM $TABLA_USUARIOS WHERE LOWER(TRIM($USU_CORREO)) = LOWER(TRIM(?)) AND $USU_ROL = ? AND $USU_ACTIVO = 1",
            arrayOf(correo, RolUsuario.ADMIN.name)
        )
        val r = c.moveToFirst()
        c.close()
        return r
    }

    fun contarAdminsActivos(): Int {
        val c = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM $TABLA_USUARIOS WHERE $USU_ROL = ? AND $USU_ACTIVO = 1",
            arrayOf(RolUsuario.ADMIN.name)
        )
        val n = if (c.moveToFirst()) c.getInt(0) else 0
        c.close()
        return n
    }

    fun existeRut(rut: String): Boolean {
        val c = readableDatabase.rawQuery("SELECT 1 FROM $TABLA_USUARIOS WHERE $USU_RUT = ?", arrayOf(rut))
        val r = c.moveToFirst()
        c.close()
        return r
    }

    /** Correo ocupado por cualquier persona, activa o no (el UNIQUE aplica a todas). */
    fun existeCorreoRegistrado(correo: String): Boolean {
        val c = readableDatabase.rawQuery(
            "SELECT 1 FROM $TABLA_USUARIOS WHERE LOWER(TRIM($USU_CORREO)) = LOWER(TRIM(?))",
            arrayOf(correo)
        )
        val r = c.moveToFirst()
        c.close()
        return r
    }

    fun insertarColaborador(
        nombre: String, correo: String, rut: String, hash: String,
        perfil: com.example.rinconsur.model.PerfilAcceso
    ): Long {
        val valores = ContentValues().apply {
            put(USU_NOMBRE, nombre)
            put(USU_CORREO, correo)
            put(USU_CONTRASENA, hash)
            put(USU_ROL, perfil.rol.name)
            put(USU_RUT, rut)
            put(USU_PERFIL, perfil.codigo)
            put(USU_ACTIVO, 1)
        }
        return writableDatabase.insert(TABLA_USUARIOS, null, valores)
    }

    fun cambiarActivoColaborador(id: Int, activo: Boolean): Boolean {
        val valores = ContentValues().apply { put(USU_ACTIVO, if (activo) 1 else 0) }
        return writableDatabase.update(TABLA_USUARIOS, valores, "$USU_ID = ?", arrayOf(id.toString())) > 0
    }

    fun cambiarPerfilColaborador(id: Int, perfil: com.example.rinconsur.model.PerfilAcceso): Boolean {
        val valores = ContentValues().apply {
            put(USU_PERFIL, perfil.codigo)
            put(USU_ROL, perfil.rol.name)
        }
        return writableDatabase.update(TABLA_USUARIOS, valores, "$USU_ID = ?", arrayOf(id.toString())) > 0
    }

    fun insertarSesion(codigo: String, nombre: String, fecha: String): Long {
        val valores = ContentValues().apply {
            put("codigo", codigo)
            put("nombre", nombre)
            put("fecha", fecha)
            put("activo", 1)
        }
        return writableDatabase.insert(TABLA_SESIONES, null, valores)
    }

    fun cambiarActivoSesion(idSesion: Long, activo: Boolean) {
        val valores = ContentValues().apply { put("activo", if (activo) 1 else 0) }
        writableDatabase.update(TABLA_SESIONES, valores, "id_sesion = ?", arrayOf(idSesion.toString()))
    }

    fun finalizarSesion(idSesion: Long) {
        val db = writableDatabase
        val valoresSesion = ContentValues().apply { put("activo", 0) }
        db.update(TABLA_SESIONES, valoresSesion, "id_sesion = ?", arrayOf(idSesion.toString()))

        val valoresConteos = ContentValues().apply { put("completado", 1) }
        db.update(TABLA_CONTEOS, valoresConteos, "id_sesion = ?", arrayOf(idSesion.toString()))
    }

    fun finalizarSesionPorCodigo(codigo: String, fecha: String = Fechas.hoy()) {
        val db = writableDatabase
        val cursor = db.rawQuery(
            "SELECT id_sesion FROM $TABLA_SESIONES WHERE fecha = ? AND UPPER(codigo) = UPPER(?) AND activo = 1",
            arrayOf(fecha, codigo.trim())
        )
        val idsFinalizar = mutableListOf<Long>()
        while (cursor.moveToNext()) {
            idsFinalizar.add(cursor.getLong(0))
        }
        cursor.close()

        idsFinalizar.forEach { id ->
            finalizarSesion(id)
        }
    }

    fun insertarConteo(idSesion: Long, nombre: String, horario: String) {
        val valores = ContentValues().apply {
            put("id_sesion", idSesion)
            put("nombre", nombre)
            put("horario", horario)
            put("completado", 0)
            put("activo", 1)
        }
        writableDatabase.insert(TABLA_CONTEOS, null, valores)
    }

    fun cambiarActivoConteo(idConteo: Long, activo: Boolean) {
        val valores = ContentValues().apply { put("activo", if (activo) 1 else 0) }
        writableDatabase.update(TABLA_CONTEOS, valores, "id_conteo = ?", arrayOf(idConteo.toString()))
    }

    fun cambiarEstadoConteo(idConteo: Long, completado: Boolean) {
        val valores = ContentValues().apply { put("completado", if (completado) 1 else 0) }
        writableDatabase.update(TABLA_CONTEOS, valores, "id_conteo = ?", arrayOf(idConteo.toString()))
    }

    fun obtenerSesiones(desde: String, hasta: String, incluirInactivas: Boolean): List<SesionAcceso> {
        val db = readableDatabase
        val lista = mutableListOf<SesionAcceso>()
        val filtroActivo = if (incluirInactivas) "" else " AND activo = 1"

        db.rawQuery(
            "SELECT id_sesion, codigo, nombre, fecha, activo FROM $TABLA_SESIONES " +
                    "WHERE fecha BETWEEN ? AND ?$filtroActivo ORDER BY fecha DESC, id_sesion ASC",
            arrayOf(desde, hasta)
        ).use { c ->
            while (c.moveToNext()) {
                val id = c.getLong(0)
                lista.add(
                    SesionAcceso(
                        id = id,
                        codigo = c.getString(1),
                        nombre = c.getString(2),
                        fecha = c.getString(3),
                        activo = c.getInt(4) == 1,
                        conteos = obtenerConteos(db, id, incluirInactivas)
                    )
                )
            }
        }
        return lista
    }

    private fun obtenerConteos(db: SQLiteDatabase, idSesion: Long, incluirInactivas: Boolean): List<ConteoSesion> {
        val lista = mutableListOf<ConteoSesion>()
        val filtroActivo = if (incluirInactivas) "" else " AND activo = 1"

        db.rawQuery(
            "SELECT id_conteo, nombre, horario, completado, activo FROM $TABLA_CONTEOS " +
                    "WHERE id_sesion = ?$filtroActivo ORDER BY id_conteo ASC",
            arrayOf(idSesion.toString())
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    ConteoSesion(
                        id = c.getLong(0),
                        nombre = c.getString(1),
                        horario = c.getString(2),
                        completado = c.getInt(3) == 1,
                        activo = c.getInt(4) == 1
                    )
                )
            }
        }
        return lista
    }

    fun contarSesionesCompletadas(): Int {
        val db = readableDatabase
        val countInactivas = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLA_SESIONES WHERE activo = 0",
            null
        ).use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }

        val todas = obtenerSesiones("0000-01-01", "2099-12-31", incluirInactivas = true)
        val countFinConteos = todas.count { (it.conteos.isNotEmpty() && it.avance == 100) || !it.activo }

        return maxOf(countInactivas, countFinConteos)
    }

    fun contarSesiones(desde: String, hasta: String, acceso: String?): Int {
        val filtroAcceso = if (acceso != null) " AND codigo = ?" else ""
        val args = if (acceso != null) arrayOf(desde, hasta, acceso) else arrayOf(desde, hasta)
        val total = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM $TABLA_SESIONES WHERE activo = 1 AND fecha BETWEEN ? AND ?$filtroAcceso",
            args
        ).use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }
        return total
    }

    fun obtenerHorariosConteos(desde: String, hasta: String, acceso: String?): List<String> {
        val filtroAcceso = if (acceso != null) " AND s.codigo = ?" else ""
        val args = if (acceso != null) arrayOf(desde, hasta, acceso) else arrayOf(desde, hasta)
        val lista = mutableListOf<String>()
        readableDatabase.rawQuery(
            "SELECT c.horario FROM $TABLA_CONTEOS c " +
                    "INNER JOIN $TABLA_SESIONES s ON s.id_sesion = c.id_sesion " +
                    "WHERE c.activo = 1 AND s.activo = 1 AND s.fecha BETWEEN ? AND ?$filtroAcceso",
            args
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(c.getString(0))
            }
        }
        return lista
    }
    private fun valoresFlujo(r: RegistroFlujo) = ContentValues().apply {
        put("acceso", r.acceso)
        put("fecha", r.fecha)
        put("ingresos", r.ingresos)
        put("compradores", r.compradores)
        put("transito_joven", r.transitoJoven)
        put("transito_adulto", r.transitoAdulto)
        put("transito_mayor", r.transitoMayor)
        put("activo", if (r.activo) 1 else 0)
    }

    fun insertarRegistroFlujo(registro: RegistroFlujo): Long =
        writableDatabase.insert(TABLA_FLUJO, null, valoresFlujo(registro))

    fun insertarRegistrosFlujo(lista: List<RegistroFlujo>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            lista.forEach { db.insert(TABLA_FLUJO, null, valoresFlujo(it)) }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun cambiarActivoRegistroFlujo(idRegistro: Long, activo: Boolean) {
        val valores = ContentValues().apply { put("activo", if (activo) 1 else 0) }
        writableDatabase.update(TABLA_FLUJO, valores, "id_registro = ?", arrayOf(idRegistro.toString()))
    }

    fun cantidadRegistrosFlujo(): Int {
        val total = readableDatabase.rawQuery("SELECT COUNT(*) FROM $TABLA_FLUJO", null)
            .use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }
        return total
    }

    fun obtenerResumenFlujo(desde: String, hasta: String, acceso: String?): ResumenFlujo {
        val filtroAcceso = if (acceso != null) " AND acceso = ?" else ""
        val args = if (acceso != null) arrayOf(desde, hasta, acceso) else arrayOf(desde, hasta)
        val resumen = readableDatabase.rawQuery(
            "SELECT COALESCE(SUM(ingresos),0), COALESCE(SUM(compradores),0), " +
                    "COALESCE(SUM(transito_joven),0), COALESCE(SUM(transito_adulto),0), " +
                    "COALESCE(SUM(transito_mayor),0) " +
                    "FROM $TABLA_FLUJO WHERE activo = 1 AND fecha BETWEEN ? AND ?$filtroAcceso",
            args
        ).use { c ->
            if (c.moveToFirst()) {
                ResumenFlujo(c.getInt(0), c.getInt(1), c.getInt(2), c.getInt(3), c.getInt(4))
            } else {
                ResumenFlujo()
            }
        }
        return resumen
    }

    fun obtenerFlujoPorAcceso(desde: String, hasta: String): Map<String, ResumenFlujo> {
        val mapa = mutableMapOf<String, ResumenFlujo>()
        readableDatabase.rawQuery(
            "SELECT acceso, SUM(ingresos), SUM(compradores), SUM(transito_joven), " +
                    "SUM(transito_adulto), SUM(transito_mayor) " +
                    "FROM $TABLA_FLUJO WHERE activo = 1 AND fecha BETWEEN ? AND ? GROUP BY acceso",
            arrayOf(desde, hasta)
        ).use { c ->
            while (c.moveToNext()) {
                mapa[c.getString(0)] = ResumenFlujo(
                    c.getInt(1), c.getInt(2), c.getInt(3), c.getInt(4), c.getInt(5)
                )
            }
        }
        return mapa
    }

    fun obtenerRegistrosFlujo(
        desde: String,
        hasta: String,
        acceso: String?,
        incluirInactivos: Boolean,
        limite: Int
    ): List<RegistroFlujo> {
        val filtroAcceso = if (acceso != null) " AND acceso = ?" else ""
        val filtroActivo = if (incluirInactivos) "" else " AND activo = 1"
        val args = if (acceso != null) arrayOf(desde, hasta, acceso) else arrayOf(desde, hasta)
        val lista = mutableListOf<RegistroFlujo>()

        readableDatabase.rawQuery(
            "SELECT id_registro, acceso, fecha, ingresos, compradores, transito_joven, " +
                    "transito_adulto, transito_mayor, activo FROM $TABLA_FLUJO " +
                    "WHERE fecha BETWEEN ? AND ?$filtroAcceso$filtroActivo " +
                    "ORDER BY fecha DESC, id_registro DESC LIMIT $limite",
            args
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    RegistroFlujo(
                        id = c.getLong(0),
                        acceso = c.getString(1),
                        fecha = c.getString(2),
                        ingresos = c.getInt(3),
                        compradores = c.getInt(4),
                        transitoJoven = c.getInt(5),
                        transitoAdulto = c.getInt(6),
                        transitoMayor = c.getInt(7),
                        activo = c.getInt(8) == 1
                    )
                )
            }
        }
        return lista
    }
    fun registrarEntradaVisitante(fecha: String = Fechas.hoy()): Long {
        val hora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val valores = ContentValues().apply {
            put("fecha", fecha)
            put("hora", hora)
            put("estado", "En_Tienda")
        }
        return writableDatabase.insert(TABLA_VISITANTES, null, valores)
    }

    fun registrarSalidaVisitanteSinCompra(fecha: String = Fechas.hoy()): Boolean {
        val db = writableDatabase
        var idVisitante: Long? = null

        val cursor = db.rawQuery(
            "SELECT id_visitante FROM $TABLA_VISITANTES " +
                    "WHERE fecha = ? AND estado = 'En_Tienda' " +
                    "ORDER BY id_visitante ASC LIMIT 1",
            arrayOf(fecha)
        )
        if (cursor.moveToFirst()) {
            idVisitante = cursor.getLong(0)
        }
        cursor.close()

        return if (idVisitante != null) {
            val valores = ContentValues().apply {
                put("estado", "No_Compro")
            }
            val filas = db.update(
                TABLA_VISITANTES,
                valores,
                "id_visitante = ?",
                arrayOf(idVisitante.toString())
            )
            filas > 0
        } else {
            false
        }
    }

    fun validarEstructuraBoleta(folio: String): Boolean {
        val folioLimpio = folio.trim()
        return folioLimpio.matches(Regex("^[0-9]{6,10}$"))
    }

    fun esBoletaRegistrada(folio: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT 1 FROM $TABLA_BOLETAS WHERE LOWER(TRIM(folio)) = LOWER(TRIM(?))",
            arrayOf(folio)
        )
        val existe = cursor.moveToFirst()
        cursor.close()
        return existe
    }

    fun procesarBoleta(
        folio: String,
        accesoActivo: String? = null,
        fecha: String = Fechas.hoy()
    ): ResultadoBoleta {
        val folioLimpio = folio.trim()

        if (!validarEstructuraBoleta(folioLimpio)) {
            return ResultadoBoleta.FormatoInvalido
        }

        if (esBoletaRegistrada(folioLimpio)) {
            return ResultadoBoleta.YaProcesada
        }

        val db = writableDatabase
        var idVisitante: Long? = null

        val cursor = db.rawQuery(
            "SELECT id_visitante FROM $TABLA_VISITANTES " +
                    "WHERE fecha = ? AND estado = 'En_Tienda' " +
                    "ORDER BY id_visitante ASC LIMIT 1",
            arrayOf(fecha)
        )
        if (cursor.moveToFirst()) {
            idVisitante = cursor.getLong(0)
        }
        cursor.close()

        if (idVisitante == null) {
            return ResultadoBoleta.SinVisitanteActivo
        }

        db.beginTransaction()
        try {
            val valoresBoleta = ContentValues().apply {
                put("folio", folioLimpio)
                put("fecha", fecha)
                put("id_visitante", idVisitante)
            }
            db.insert(TABLA_BOLETAS, null, valoresBoleta)

            val valoresVisitante = ContentValues().apply {
                put("estado", "Compro")
                put("boleta", folioLimpio)
            }
            db.update(
                TABLA_VISITANTES,
                valoresVisitante,
                "id_visitante = ?",
                arrayOf(idVisitante.toString())
            )

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        if (!accesoActivo.isNullOrBlank()) {
            registrarOActualizarFlujo(
                acceso = accesoActivo,
                fecha = fecha,
                deltaIngresos = 0,
                deltaCompradores = 1
            )
        }

        return ResultadoBoleta.Exito(idVisitante, folioLimpio)
    }

    fun obtenerSesionesActivasEntrada(fecha: String = Fechas.hoy()): List<SesionAcceso> {
        val db = readableDatabase
        val lista = mutableListOf<SesionAcceso>()
        db.rawQuery(
            "SELECT id_sesion, codigo, nombre, fecha, activo FROM $TABLA_SESIONES " +
                    "WHERE fecha = ? AND activo = 1 AND UPPER(codigo) IN ('A1', 'A2', 'A3', 'A4') " +
                    "ORDER BY id_sesion DESC",
            arrayOf(fecha)
        ).use { c ->
            while (c.moveToNext()) {
                val id = c.getLong(0)
                lista.add(
                    SesionAcceso(
                        id = id,
                        codigo = c.getString(1),
                        nombre = c.getString(2),
                        fecha = c.getString(3),
                        activo = c.getInt(4) == 1,
                        conteos = emptyList()
                    )
                )
            }
        }
        return lista
    }

    fun registrarOActualizarFlujo(
        acceso: String,
        fecha: String = Fechas.hoy(),
        deltaIngresos: Int = 0,
        deltaCompradores: Int = 0,
        rangoEtario: RangoEtario = RangoEtario.ADULTO
    ) {
        val db = writableDatabase
        val cursor = db.rawQuery(
            "SELECT id_registro, ingresos, compradores, transito_joven, transito_adulto, transito_mayor " +
                    "FROM $TABLA_FLUJO WHERE fecha = ? AND acceso = ? AND activo = 1 LIMIT 1",
            arrayOf(fecha, acceso)
        )

        if (cursor.moveToFirst()) {
            val id = cursor.getLong(0)
            val ingresosAct = cursor.getInt(1) + deltaIngresos
            val compradoresAct = cursor.getInt(2) + deltaCompradores
            var jovenAct = cursor.getInt(3)
            var adultoAct = cursor.getInt(4)
            var mayorAct = cursor.getInt(5)

            if (deltaIngresos > 0) {
                when (rangoEtario) {
                    RangoEtario.JOVEN -> jovenAct += deltaIngresos
                    RangoEtario.ADULTO -> adultoAct += deltaIngresos
                    RangoEtario.PERSONA_MAYOR, RangoEtario.NO_DETERMINADO, RangoEtario.NINO -> mayorAct += deltaIngresos
                }
            }

            val valores = ContentValues().apply {
                put("ingresos", ingresosAct.coerceAtLeast(0))
                put("compradores", compradoresAct.coerceAtLeast(0))
                put("transito_joven", jovenAct.coerceAtLeast(0))
                put("transito_adulto", adultoAct.coerceAtLeast(0))
                put("transito_mayor", mayorAct.coerceAtLeast(0))
            }
            db.update(TABLA_FLUJO, valores, "id_registro = ?", arrayOf(id.toString()))
        } else {
            var joven = 0
            var adulto = 0
            var mayor = 0

            if (deltaIngresos > 0) {
                when (rangoEtario) {
                    RangoEtario.JOVEN -> joven = deltaIngresos
                    RangoEtario.ADULTO -> adulto = deltaIngresos
                    RangoEtario.PERSONA_MAYOR, RangoEtario.NO_DETERMINADO, RangoEtario.NINO -> mayor = deltaIngresos
                }
            }

            val valores = ContentValues().apply {
                put("acceso", acceso)
                put("fecha", fecha)
                put("ingresos", deltaIngresos.coerceAtLeast(1))
                put("compradores", deltaCompradores.coerceAtLeast(0))
                put("transito_joven", joven)
                put("transito_adulto", adulto)
                put("transito_mayor", mayor)
                put("activo", 1)
            }
            db.insert(TABLA_FLUJO, null, valores)
        }
        cursor.close()
    }

    fun obtenerResumenVisitantes(fecha: String = Fechas.hoy()): ResumenVisitantes {
        val db = readableDatabase
        var enTienda = 0
        var compro = 0
        var noCompro = 0

        val cursor = db.rawQuery(
            "SELECT estado, COUNT(*) FROM $TABLA_VISITANTES WHERE fecha = ? GROUP BY estado",
            arrayOf(fecha)
        )
        while (cursor.moveToNext()) {
            val estado = cursor.getString(0)
            val cantidad = cursor.getInt(1)
            when (estado) {
                "En_Tienda" -> enTienda = cantidad
                "Compro" -> compro = cantidad
                "No_Compro" -> noCompro = cantidad
            }
        }
        cursor.close()

        val total = enTienda + compro + noCompro
        return ResumenVisitantes(enTienda, compro, noCompro, total)
    }

    fun ejecutarCierreDiario(fecha: String = Fechas.hoy()): Int {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put("estado", "No_Compro")
        }
        return db.update(
            TABLA_VISITANTES,
            valores,
            "fecha = ? AND estado = 'En_Tienda'",
            arrayOf(fecha)
        )
    }
}