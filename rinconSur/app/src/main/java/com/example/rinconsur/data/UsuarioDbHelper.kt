package com.example.rinconsur.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.rinconsur.model.ConteoSesion
import com.example.rinconsur.model.RegistroFlujo
import com.example.rinconsur.model.ResumenFlujo
import com.example.rinconsur.model.RolUsuario
import com.example.rinconsur.model.SesionAcceso
import com.example.rinconsur.model.Usuario
class UsuarioDbHelper (context: Context):
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD){

    companion object{
        private const val NOMBRE_BD = "rinconsur.db"
        private const val VERSION_BD = 5

        private const val TABLA_USUARIOS = "usuarios"
        private const val USU_ID = "id_usuario"
        private const val USU_NOMBRE = "nombre"
        private const val USU_CORREO = "correo"
        private const val USU_CONTRASENA = "password_hash"
        private const val USU_ROL = "rol"
        private const val USU_AVATAR = "avatar"
        private const val USU_DESCRIPCION = "descripcion"

        private const val TABLA_SESIONES = "sesiones"
        private const val TABLA_CONTEOS = "conteos"

        private const val TABLA_FLUJO = "registros_flujo"
    }

    override fun onConfigure(db: SQLiteDatabase){
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase){
        val crearTablaUsuarios = """
            CREATE TABLE $TABLA_USUARIOS(
                $USU_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $USU_NOMBRE TEXT NOT NULL,
                $USU_CORREO TEXT UNIQUE NOT NULL,
                $USU_CONTRASENA TEXT NOT NULL,
                $USU_ROL TEXT NOT NULL,
                $USU_AVATAR BLOB,
                $USU_DESCRIPCION TEXT
            )
        """.trimIndent()
        db.execSQL(crearTablaUsuarios)

        val crearTablaSesiones = """
            CREATE TABLE $TABLA_SESIONES(
                id_sesion INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo TEXT NOT NULL,
                nombre TEXT NOT NULL,
                fecha TEXT NOT NULL,
                activo INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent()
        db.execSQL(crearTablaSesiones)

        val crearTablaConteos = """
            CREATE TABLE $TABLA_CONTEOS(
                id_conteo INTEGER PRIMARY KEY AUTOINCREMENT,
                id_sesion INTEGER NOT NULL,
                nombre TEXT NOT NULL,
                horario TEXT NOT NULL,
                completado INTEGER NOT NULL DEFAULT 0,
                activo INTEGER NOT NULL DEFAULT 1,
                FOREIGN KEY(id_sesion) REFERENCES $TABLA_SESIONES(id_sesion)
            )
        """.trimIndent()
        db.execSQL(crearTablaConteos)

        val crearTablaFlujo = """
            CREATE TABLE $TABLA_FLUJO(
                id_registro INTEGER PRIMARY KEY AUTOINCREMENT,
                acceso TEXT NOT NULL,
                fecha TEXT NOT NULL,
                ingresos INTEGER NOT NULL,
                compradores INTEGER NOT NULL,
                transito_joven INTEGER NOT NULL,
                transito_adulto INTEGER NOT NULL,
                transito_mayor INTEGER NOT NULL,
                activo INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent()
        db.execSQL(crearTablaFlujo)

        val hashObs = BCrypt.withDefaults().hashToString(12, "Observador123".toCharArray())
        val hashSup = BCrypt.withDefaults().hashToString(12, "Supervisor123".toCharArray())

        insertarUsuarioPrueba(db,"Observador rincón sur", "observador@rinconsur.cl", hashObs, RolUsuario.OBSERVADOR.name)
        insertarUsuarioPrueba(db,"Supervisor rincón sur",  "supervisor@rinconsur.cl", hashSup, RolUsuario.SUPERVISOR.name)
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int){db.execSQL("DROP TABLE IF EXISTS $TABLA_FLUJO")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_CONTEOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_SESIONES")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_USUARIOS")
        onCreate(db)
    }

    private fun insertarUsuarioPrueba(db: SQLiteDatabase, nombre: String, correo: String, hash: String, rol: String){
        val valores = ContentValues().apply {
            put(USU_NOMBRE, nombre)
            put(USU_CORREO, correo)
            put(USU_CONTRASENA, hash)
            put(USU_ROL, rol)
        }
        db.insert(TABLA_USUARIOS, null, valores)
    }

    fun obtenerUsuarioPorCorreo(correo: String): Usuario? {
        val db = this.readableDatabase
        var usuario: Usuario? = null

        val cursor: Cursor = db.rawQuery(
            "SELECT $USU_NOMBRE, $USU_CORREO, $USU_CONTRASENA, $USU_ROL, $USU_AVATAR, $USU_DESCRIPCION " +
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
                descripcion = cursor.getString(5)
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
        val cursor = db.rawQuery("SELECT 1 FROM $TABLA_USUARIOS WHERE $USU_CORREO = ?", arrayOf(correo))
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
}