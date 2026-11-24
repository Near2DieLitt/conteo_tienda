package com.example.rinconsur.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.rinconsur.R
import com.example.rinconsur.data.Fechas
import com.example.rinconsur.data.RinconSurRepository
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.data.VisitanteEnTiendaItem
import com.example.rinconsur.model.ResultadoBoleta
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(ExperimentalGetImage::class)
@Composable
fun EscanearBoletaScreen(
    correoUsuario: String,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val sesionPrefs = remember { SesionPreferencias(context) }
    val repositorio = remember { RinconSurRepository() }
    val scope = rememberCoroutineScope()

    val entradaActiva = remember(correoUsuario) { sesionPrefs.obtenerEntradaActiva(correoUsuario) }
    var idSesion by remember { mutableStateOf<Long?>(null) }
    var visitantesActivos by remember { mutableStateOf<List<VisitanteEnTiendaItem>>(emptyList()) }
    var idVisitanteSeleccionado by remember { mutableStateOf<Long?>(null) }

    suspend fun recargarVisitantes(sesId: Long?) {
        try {
            val lista = repositorio.listarVisitantesEnTienda(Fechas.hoy(), sesId)
            visitantesActivos = lista
            idVisitanteSeleccionado = idVisitanteSeleccionado?.takeIf { sel -> lista.any { it.id == sel } }
        } catch (_: Exception) { }
    }

    LaunchedEffect(correoUsuario, entradaActiva) {
        try {
            val idUsu = if (correoUsuario.isNotBlank()) {
                try { repositorio.buscarUsuarioPorCorreo(correoUsuario).id } catch (_: Exception) { null }
            } else null
            val sesionesHoy = repositorio.listarSesiones(fecha = Fechas.hoy(), incluirInactivas = false, idUsuario = idUsu)
            val sesId = if (!entradaActiva.isNullOrBlank()) {
                sesionesHoy.firstOrNull { it.codigo.equals(entradaActiva, ignoreCase = true) }?.id
            } else {
                sesionesHoy.firstOrNull()?.id
            }
            idSesion = sesId
            recargarVisitantes(sesId)
        } catch (_: Exception) { }
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    var codigoDetectado by remember { mutableStateOf<String?>(null) }
    var resultadoBoleta by remember { mutableStateOf<ResultadoBoleta?>(null) }
    var entradaManual by remember { mutableStateOf("") }
    var errorCamara by remember { mutableStateOf<String?>(null) }

    val escaneoBloqueado = remember { AtomicBoolean(false) }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
        BarcodeScanning.getClient(options)
    }
    var cameraProviderRef by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            cameraProviderRef?.unbindAll()
            scanner.close()
            analysisExecutor.shutdown()
        }
    }

    fun procesarCodigo(codigo: String) {
        val folioLimpio = codigo.trim()
        escaneoBloqueado.set(true)
        codigoDetectado = folioLimpio

        if (!folioLimpio.matches(Regex("^[0-9]{6,10}$"))) {
            resultadoBoleta = ResultadoBoleta.FormatoInvalido
            return
        }

        if (visitantesActivos.isEmpty()) {
            resultadoBoleta = ResultadoBoleta.SinVisitanteActivo
            return
        }
        val idVisitante = idVisitanteSeleccionado
        if (idVisitante == null) {
            resultadoBoleta = ResultadoBoleta.SinSeleccion
            return
        }

        scope.launch {
            try {
                repositorio.vincularBoleta(idVisitante, folioLimpio)
                if (!entradaActiva.isNullOrBlank()) {
                    try {
                        repositorio.registrarFlujo(
                            acceso = entradaActiva,
                            fecha = Fechas.hoy(),
                            ingresos = 0,
                            compradores = 1,
                            joven = 0,
                            adulto = 0,
                            mayor = 0
                        )
                    } catch (_: Exception) { }
                }
                resultadoBoleta = ResultadoBoleta.Exito(idVisitante, folioLimpio)
                idVisitanteSeleccionado = null
                recargarVisitantes(idSesion)
            } catch (e: Exception) {
                val msg = e.message.orEmpty()
                resultadoBoleta = when {
                    msg.contains("YA_PROCESADA", ignoreCase = true) || msg.contains("procesada", ignoreCase = true) ->
                        ResultadoBoleta.YaProcesada
                    msg.contains("FORMATO_INVALIDO", ignoreCase = true) || msg.contains("dígitos", ignoreCase = true) ->
                        ResultadoBoleta.FormatoInvalido
                    msg.contains("SIN_VISITANTE", ignoreCase = true) || msg.contains("ya no está", ignoreCase = true) ->
                        ResultadoBoleta.SinVisitanteActivo
                    else -> ResultadoBoleta.ErrorServidor(msg.ifBlank { "No se pudo procesar la boleta. Revisa tu conexión." })
                }
                recargarVisitantes(idSesion)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            cameraProviderRef = cameraProvider

                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
                                val mediaImage = imageProxy.image
                                if (mediaImage != null && !escaneoBloqueado.get()) {
                                    val image = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )
                                    scanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            val rawValue = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }?.rawValue
                                            if (!rawValue.isNullOrBlank() && escaneoBloqueado.compareAndSet(false, true)) {
                                                ContextCompat.getMainExecutor(ctx).execute {
                                                    procesarCodigo(rawValue)
                                                }
                                            }
                                        }
                                        .addOnCompleteListener { imageProxy.close() }
                                } else {
                                    imageProxy.close()
                                }
                            }

                            val selector = when {
                                cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> CameraSelector.DEFAULT_BACK_CAMERA
                                cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> CameraSelector.DEFAULT_FRONT_CAMERA
                                else -> null
                            }

                            if (selector == null) {
                                errorCamara = "No se detectó cámara disponible. Usa el ingreso manual abajo."
                            } else {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    selector,
                                    preview,
                                    imageAnalysis
                                )
                                errorCamara = null
                            }
                        } catch (exc: Exception) {
                            errorCamara = "No se pudo iniciar la cámara: ${exc.localizedMessage ?: "error desconocido"}"
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (errorCamara != null) {
                        Text(
                            text = errorCamara!!,
                            color = Color(0xFFFFB74D),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .width(280.dp)
                                .height(160.dp)
                                .border(2.dp, Color(0xFF0F5961), RoundedCornerShape(16.dp))
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Alinee la boleta dentro del recuadro",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Se requiere permiso de cámara para escanear boletas.",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                    Text("Conceder permiso")
                }
            }
        }

        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onVolver,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_volver),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Escanear Boleta",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (visitantesActivos.isNotEmpty()) {
                    Text(
                        "Seleccionar visitante que realiza la compra:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F5961)
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        visitantesActivos.forEach { vis ->
                            FilterChip(
                                selected = idVisitanteSeleccionado == vis.id,
                                onClick = { idVisitanteSeleccionado = vis.id },
                                label = { Text("${vis.codigo} (${vis.rangoEtario})", fontSize = 10.sp) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = entradaManual,
                        onValueChange = { entradaManual = it },
                        placeholder = { Text("Ingresar folio manualmente (6-10 dígitos)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (entradaManual.isNotBlank()) {
                                procesarCodigo(entradaManual)
                                entradaManual = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF0F5961), RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Send,
                            contentDescription = stringResource(R.string.cd_procesar),
                            tint = Color.White
                        )
                    }
                }
            }
        }

        resultadoBoleta?.let { res ->
            val (icono, tintColor, titulo, mensaje) = when (res) {
                is ResultadoBoleta.Exito -> Quadruple(
                    Icons.Filled.CheckCircle,
                    Color(0xFF1E7550),
                    "¡Boleta Procesada!",
                    "Boleta #${res.folio} vinculada exitosamente al visitante seleccionado."
                )
                is ResultadoBoleta.YaProcesada -> Quadruple(
                    Icons.Filled.Error,
                    Color.Red,
                    "Boleta Ya Registrada",
                    "Esta boleta ya fue procesada anteriormente."
                )
                is ResultadoBoleta.FormatoInvalido -> Quadruple(
                    Icons.Filled.Warning,
                    Color(0xFFB85D19),
                    "Estructura Inválida",
                    "La boleta debe ser un número de entre 6 y 10 dígitos."
                )
                is ResultadoBoleta.SinSeleccion -> Quadruple(
                    Icons.Filled.Warning,
                    Color(0xFFB85D19),
                    "Selecciona un visitante",
                    "Elige primero qué visitante realizó la compra y vuelve a escanear."
                )
                is ResultadoBoleta.ErrorServidor -> Quadruple(
                    Icons.Filled.Error,
                    Color.Red,
                    "No se pudo procesar",
                    res.mensaje
                )
                is ResultadoBoleta.SinVisitanteActivo -> Quadruple(
                    Icons.Filled.Warning,
                    Color(0xFFB85D19),
                    "Sin Visitantes en Tienda",
                    "No hay visitantes actualmente dentro de la tienda para vincular con la compra."
                )
            }

            AlertDialog(
                onDismissRequest = { },
                icon = {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = tintColor,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = { Text(text = titulo, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(text = mensaje, fontSize = 15.sp)
                        codigoDetectado?.let { cod ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Código/Folio: $cod",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            codigoDetectado = null
                            resultadoBoleta = null
                            escaneoBloqueado.set(false)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5961))
                    ) {
                        Text("Escanear otra")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onVolver) {
                        Text("Finalizar")
                    }
                }
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
