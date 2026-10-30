package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.R
import com.example.ui.components.GlassCard
import com.example.ui.components.ScanningOverlay
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AnalysisState
import com.example.ui.viewmodel.MainViewModel
import com.example.util.ImageUtils
import java.util.concurrent.Executors

@Composable
fun ScanCameraScreen(
    viewModel: MainViewModel,
    onClose: () -> Unit,
    onScanSuccess: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val analysisState by viewModel.analysisState.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Gallery Picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bitmap = ImageUtils.uriToScaledBitmap(context, uri)
            if (bitmap != null) {
                viewModel.analyzePhoto(bitmap)
            }
        }
    }

    var flashEnabled by remember { mutableStateOf(false) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    BackHandler {
        if (analysisState is AnalysisState.Analyzing) {
            viewModel.resetAnalysis()
        } else {
            onClose()
        }
    }

    LaunchedEffect(analysisState) {
        if (analysisState is AnalysisState.Success) {
            onScanSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B11))
    ) {
        // Camera Viewport or Fallback Container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp, top = 20.dp, start = 12.dp, end = 12.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFF0F1722))
        ) {
            if (hasCameraPermission) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            try {
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val capture = ImageCapture.Builder()
                                    .setFlashMode(
                                        if (flashEnabled) ImageCapture.FLASH_MODE_ON
                                        else ImageCapture.FLASH_MODE_OFF
                                    )
                                    .build()
                                imageCapture = capture

                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, capture)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Permission Denied or Simulator Fallback
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_healthy_bowl_1793371421669),
                        contentDescription = "Culinary Power Bowl",
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(24.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Camera Access Required",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Grant camera permission to scan dishes, or analyze a photo from your gallery.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Enable Camera", color = Color(0xFF042F2E), fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                // Demo photo scan for quick verification
                                val sampleBitmap = BitmapFactory.decodeResource(
                                    context.resources,
                                    R.drawable.img_hero_healthy_bowl_1793371421669
                                )
                                if (sampleBitmap != null) {
                                    viewModel.analyzePhoto(sampleBitmap)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Analyze Sample", color = TextPrimary)
                        }
                    }
                }
            }

            // Animated Scanning Overlay
            ScanningOverlay(
                isScanningActive = analysisState !is AnalysisState.Error
            )

            // Prompt pill at top of preview
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x99000000))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Place your food inside the frame",
                    color = TextPrimary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Top Control Bar: Close button & Flash button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0x88000000))
                    .border(1.dp, Color(0x33FFFFFF), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TextPrimary
                )
            }

            IconButton(
                onClick = { flashEnabled = !flashEnabled },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0x88000000))
                    .border(1.dp, Color(0x33FFFFFF), CircleShape)
            ) {
                Icon(
                    imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Toggle Flash",
                    tint = if (flashEnabled) CyanAccent else TextPrimary
                )
            }
        }

        // Bottom Bar: Gallery Button & Circular Capture Shutter
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 28.dp, start = 28.dp, end = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery button
                IconButton(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                        .border(1.dp, Color(0x44FFFFFF), CircleShape)
                        .testTag("camera_gallery_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Choose from gallery",
                        tint = TextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Shutter Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(3.5.dp, EmeraldPrimary, CircleShape)
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .testTag("camera_capture_button")
                        .clickable {
                            val capture = imageCapture
                            if (capture != null) {
                                val executor = Executors.newSingleThreadExecutor()
                                capture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        val bitmap = imageProxyToBitmap(image)
                                        image.close()
                                        if (bitmap != null) {
                                            viewModel.analyzePhoto(bitmap)
                                        }
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        exception.printStackTrace()
                                    }
                                })
                            } else {
                                // Fallback mock sample photo analysis if camera unattached
                                val sampleBitmap = BitmapFactory.decodeResource(
                                    context.resources,
                                    R.drawable.img_hero_healthy_bowl_1793371421669
                                )
                                if (sampleBitmap != null) {
                                    viewModel.analyzePhoto(sampleBitmap)
                                }
                            }
                        }
                )

                // Placeholder space for symmetry
                Spacer(modifier = Modifier.size(54.dp))
            }
        }

        // Animated AI Processing Dialog / Overlay
        AnimatedVisibility(
            visible = analysisState is AnalysisState.Analyzing,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            val analyzing = analysisState as? AnalysisState.Analyzing
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC060A10)),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(20.dp),
                    shape = RoundedCornerShape(28.dp),
                    backgroundColor = Color(0x2EFFFFFF),
                    borderColor = EmeraldPrimary.copy(alpha = 0.6f)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { analyzing?.progress ?: 0.5f },
                                modifier = Modifier.size(76.dp),
                                color = EmeraldPrimary,
                                strokeWidth = 5.dp,
                                trackColor = Color(0x33FFFFFF)
                            )
                            Text(
                                text = "AI",
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = analyzing?.stepText ?: "Analyzing your meal...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Estimating ingredients, calories and nutrition profile with Gemini",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Error State Modal
        AnimatedVisibility(
            visible = analysisState is AnalysisState.Error,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            val error = analysisState as? AnalysisState.Error
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC060A10)),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(20.dp),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "⚠️", fontSize = 36.sp)

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Couldn't analyze this image",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = error?.message ?: "Food wasn't clear enough to identify. Please retake photo with better lighting.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.resetAnalysis() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Try Again", color = Color(0xFF042F2E), fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    viewModel.resetAnalysis()
                                    galleryLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Choose Photo", color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
    val planeProxy = image.planes[0]
    val buffer = planeProxy.buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    return if (bitmap != null && image.imageInfo.rotationDegrees != 0) {
        ImageUtils.rotateBitmap(bitmap, image.imageInfo.rotationDegrees.toFloat())
    } else {
        bitmap
    }
}
