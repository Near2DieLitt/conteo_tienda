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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.automirrored.outlined.Send
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
import com.example.rinconsur.data.UsuarioDbHelper
import com.example.rinconsur.model.ResultadoBoleta
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.example.rinconsur.data.SesionPreferencias
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors


@OptIn(ExperimentalGetImage::class)
@Composable
fun EscanearBoletaScreen(
    correoUsuario: String,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val dbHelper = remember { UsuarioDbHelper(context) }
    val sesionPrefs = remember { SesionPreferencias(context) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    var codigoDetectado by remember { mutableStateOf<String?>(null) }
    var resultadoBoleta by remember { mutableStateOf<ResultadoBoleta?>(null) }
    var entradaManual by remember { mutableStateOf("") }

    fun procesarCodigo(codigo: String) {
        codigoDetectado = codigo
        val entradaActiva = sesionPrefs.obtenerEntradaActiva(correoUsuario)
        val idUsuario = dbHelper.obtenerUsuarioPorCorreo(correoUsuario)?.id?.toLong()
        val idSesion = entradaActiva?.let { dbHelper.obtenerIdSesionActiva(it, idUsuario) }
        resultadoBoleta = dbHelper.procesarBoleta(codigo, accesoActivo = entradaActiva, idSesion = idSesion)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val options = BarcodeScannerOptions.Builder()
                            .setBarcodeFormats(
                                Barcode.FORMAT_CODE_128,
                                Barcode.FORMAT_QR_CODE
                            )
                            .build()
                        val scanner = BarcodeScanning.getClient(options)
                        val analysisExecutor = Executors.newSingleThreadExecutor()

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage != null && codigoDetectado == null) {
                                val image = InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )
                                scanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        for (barcode in barcodes) {
                                            val rawValue = barcode.rawValue
                                            if (!rawValue.isNullOrBlank()) {
                                                procesarCodigo(rawValue)
                                                break
                                            }
                                        }
                                    }
                                    .addOnFailureListener {
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            } else {
                                imageProxy.close()
                            }
                        }

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalysis
                            )
                        } catch (exc: Exception) {
                            exc.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )


            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
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
                        contentDescription = stringResource(R.string.cd_p),
                        tint = Color.White
                    )
                }
            }
        }


        resultadoBoleta?.let { res ->
            val (icono, tintColor, titulo, mensaje) = when (res) {
                is ResultadoBoleta.Exito -> Quadruple(
                    Icons.Filled.CheckCircle,
                    Color(0xFF1E7550),
                    "¡Boleta Procesada!",
                    "Boleta #${res.folio} vinculada exitosamente. El visitante cambió su estado a 'Compró'."
                )
                is ResultadoBoleta.YaProcesada -> Quadruple(
                    Icons.Filled.Error,
                    Color.Red,
                    "Boleta Ya Registrada",
                    "Esta boleta ya fue procesada"
                )
                is ResultadoBoleta.FormatoInvalido -> Quadruple(
                    Icons.Filled.Warning,
                    Color(0xFFB85D19),
                    "Estructura Inválida",
                    "La boleta debe ser una cadena numérica con una longitud de entre 6 y 10 dígitos."
                )
                is ResultadoBoleta.SinVisitanteActivo -> Quadruple(
                    Icons.Filled.Warning,
                    Color(0xFFB85D19),
                    "Sin Visitantes en Tienda",
                    "No hay visitantes activos en la tienda con estado 'En_Tienda' para vincular con la compra."
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
                title = {
                    Text(text = titulo, fontWeight = FontWeight.Bold)
                },
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
