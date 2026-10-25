package com.example.rinconsur.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.rinconsur.model.RolUsuario
import com.example.rinconsur.model.Usuario

class UsuarioDbHelper (context: Context):
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD){

    companion object{
        private const val NOMBRE_BD = "rinconsur.db"
        private const val VERSION_BD = 2

        private const val TABLA_USUARIOS = "usuarios"
        private const val USU_ID = "id_usuario"
        private const val USU_NOMBRE = "nombre"
        private const val USU_CORREO = "correo"
        private const val USU_CONTRASENA = "password_hash"
        private const val USU_ROL = "rol"
        private const val USU_AVATAR = "avatar"
        private const val USU_DESCRIPCION = "descripcion"
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

        val hashObs = BCrypt.withDefaults().hashToString(12, "Observador123".toCharArray())
        val hashSup = BCrypt.withDefaults().hashToString(12, "Supervisor123".toCharArray())

        insertarUsuarioPrueba(db,"Observador rincón sur", "observador@rinconsur.cl", hashObs, RolUsuario.OBSERVADOR.name)
        insertarUsuarioPrueba(db,"Supervisor rincón sur",  "supervisor@rinconsur.cl", hashSup, RolUsuario.SUPERVISOR.name)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int){
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
                    "FROM $TABLA_USUARIOS WHERE $USU_CORREO = ?",
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
            "$USU_CORREO = ? ",
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
            "$USU_CORREO = ?",
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

}