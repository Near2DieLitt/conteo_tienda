# Rincón Sur

Aplicación móvil Android para el conteo de visitantes en los accesos de una tienda retail y la separación entre quienes compran y quienes solo pasan por la tienda como atajo.

Proyecto de la asignatura DSY1105 – Desarrollo de Aplicaciones Móviles (Evaluación Parcial 2).

## Integrantes
- Matias Olavarria 
- Renata Valdés

**Docente:** Victor Rosendo

## Problema y solución

La tienda tiene cuatro accesos (A1 Principal, A2 Lateral, A3 Estacionamiento, A4 Conexión) y necesita saber cuántas personas entran y cuántas de ellas realmente compran.

La solución es un conteo manual desde la app más una verificación por boleta:

- El supervisor presiona **+ Visitante** cuando alguien entra (estado En_Tienda).
- Si alguien sale sin comprar, presiona **− Visitante** (el visitante activo más antiguo pasa a No_Compro).
- Al pagar, se escanea el código de barras de la boleta con la cámara y el visitante pasa a Compro.
- Con el **Cierre diario**, todos los que siguen En_Tienda pasan a No_Compro.

**Reglas de negocio:** cada boleta solo puede escanearse una vez ("Esta boleta ya fue procesada") y solo se aceptan folios numéricos de 6 a 10 dígitos.

## Funcionalidades
- **Inicio:** resumen del día, sesiones de hoy y próximas, visitantes en tienda, escaneo de boletas, cierre diario.
- **Sesiones:** calendario para programar sesiones por acceso y conteos por horario. No permite programar en fechas pasadas.
- **Analítica:** flujo por acceso y por periodo (personalizado, semana, mes), compradores, tránsito por rango etario y registros de flujo.
- **Equipo:** lista de personas con búsqueda y filtro por perfil. Solo el administrador agrega personas, cambia perfiles y habilita o deshabilita cuentas.
- **Perfil:** foto (cámara o galería), descripción editable, cargo, permisos y cierre de sesión.
- **Login y recuperación de contraseña**, con modo claro y oscuro.

## Roles y permisos

| Rol | Puede |
| --- | --- |
| Administrador | Ver todo, crear y editar sesiones, editar analítica, registrar personas y cambiar perfiles |
| Supervisor | Ver todo (solo lectura en Sesiones, Analítica y Equipo) |
| Observador | Ver todo (solo lectura en Sesiones, Analítica y Equipo) |

El administrador no puede crear otros administradores desde la app, no puede deshabilitarse a sí mismo y siempre debe quedar al menos uno activo.

Nada se borra de la base de datos: todo se deshabilita con una columna activo y el administrador puede volver a habilitarlo.

## Validaciones (en la capa de lógica / ViewModel)
- Correo con formato válido y dominio @rinconsur.cl para el personal.
- RUT chileno con dígito verificador (módulo 11), único.
- Nombre completo (al menos nombre y apellido) y correo único.
- Contraseñas guardadas con BCrypt (nunca en texto plano). Las cuentas nuevas reciben una clave temporal aleatoria (SecureRandom).
- Folio de boleta: solo dígitos, entre 6 y 10, y sin repetir.
- Sesiones solo para accesos A1–A4 y no en fechas pasadas.
- Cuentas deshabilitadas no pueden iniciar sesión.
- Cada acción de edición vuelve a comprobar en la base de datos que quien la hace es administrador activo.

## Recursos nativos
- **Cámara (CameraX + ML Kit):** escaneo del código de barras de la boleta y foto de perfil.
- **Galería:** elegir la foto de perfil.
- **Notificaciones:** aviso al recuperar la contraseña (permiso POST_NOTIFICATIONS).

## Arquitectura y tecnologías
- Kotlin + Jetpack Compose (Material 3)
- Patrón MVVM con StateFlow
- Navigation Compose
- SQLite (SQLiteOpenHelper), base de datos única rinconsur.db
- SharedPreferences para la entrada activa
- CameraX, ML Kit Barcode Scanning, BCrypt (at.favre.lib:bcrypt)
- Animaciones: barra de navegación inferior, cambio de pestañas, tarjetas y transiciones de contenido

```text
app/src/main/java/com/example/rinconsur/
├── data/          Base de datos, preferencias, fechas, conversión de imágenes
├── model/         Usuario, Colaborador, Sesión, Flujo, Visitante, enums
├── navigation/    Rutas y AppNavigation
├── permisos/      Utilidad de permisos
├── ui/            components, screens, styles, theme
└── viewmodel/     Lógica y validaciones de cada pantalla
```

## Cómo ejecutar
1. Clona el repositorio y ábrelo con Android Studio (versión reciente).
2. Espera a que Gradle sincronice las dependencias.
3. Ejecuta en un emulador o en un celular Android (la cámara funciona mejor en celular real).
4. Acepta los permisos de cámara y notificaciones cuando la app los pida.

Al actualizar la versión de la base de datos, esta se recrea y se pierden los datos de prueba. Para empezar de cero, desinstala la app del dispositivo.

## Usuarios de prueba

| Perfil | Correo | Contraseña |
| --- | --- | --- |
| Administrador | daniela@rinconsur.cl | Daniela123 |
| Supervisor | nicole@rinconsur.cl | Nicole123 |
| Supervisor | renata@rinconsur.cl | Renata123 |
| Observador | matias@rinconsur.cl | Matias123 |

### Para probar el escaneo de boletas

Genera un código de barras formato Code 128 con un número de 6 a 10 dígitos en Barcode Tec-IT y apunta la cámara a la pantalla.

## Planificación

Tablero Jira: https://duocuc-team-f0sc9kf8.atlassian.net/jira/software/projects/LT/boards/3?filter=&groupBy=none&atlOrigin=eyJpIjoiMDY5NGU2ZjhmMjM3NGJlZWI3YTc5ZGRhMmZiZDY2OWQiLCJwIjoiaiJ9