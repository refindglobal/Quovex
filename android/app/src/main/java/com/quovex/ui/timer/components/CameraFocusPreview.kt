package com.quovex.ui.timer.components

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.quovex.data.camera.CameraFocusAnalyzer
import com.quovex.domain.model.AttentivenessState
import com.quovex.domain.model.FocusFrameResult
import com.quovex.domain.model.FocusTrackingState
import com.quovex.theme.QuovexTheme
import java.util.concurrent.Executors

@Composable
fun CameraFocusPreview(
    focusState: FocusTrackingState,
    onPermissionResult: (Boolean) -> Unit,
    onFrameAnalyzed: (FocusFrameResult) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing

    var isPreviewVisible by remember { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onPermissionResult(isGranted)
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        onPermissionResult(hasPermission)

        if (!hasPermission && focusState.isEnabled) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Attentiveness Status & Score Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(
                    when (focusState.attentivenessState) {
                        AttentivenessState.ATTENTIVE -> colors.primaryGlow
                        AttentivenessState.LOOKING_AWAY -> colors.warning.copy(alpha = 0.15f)
                        AttentivenessState.DROWSY_EYES_CLOSED -> colors.error.copy(alpha = 0.15f)
                        else -> colors.surfaceElevated
                    }
                )
                .border(
                    width = 1.dp,
                    color = when (focusState.attentivenessState) {
                        AttentivenessState.ATTENTIVE -> colors.primary
                        AttentivenessState.LOOKING_AWAY -> colors.warning
                        AttentivenessState.DROWSY_EYES_CLOSED -> colors.error
                        else -> colors.border
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = spacing.md, vertical = spacing.xs)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                Text(
                    text = focusState.attentivenessState.emoji,
                    fontSize = 14.sp
                )
                Text(
                    text = "${focusState.attentivenessState.label} • ${focusState.focusScore}% Score",
                    style = QuovexTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = when (focusState.attentivenessState) {
                        AttentivenessState.ATTENTIVE -> colors.primary
                        AttentivenessState.LOOKING_AWAY -> colors.warning
                        AttentivenessState.DROWSY_EYES_CLOSED -> colors.error
                        else -> colors.textSecondary
                    }
                )

                IconButton(
                    onClick = { isPreviewVisible = !isPreviewVisible },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = if (isPreviewVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle mini preview",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Warning Alert Banner if active
        AnimatedVisibility(
            visible = focusState.isWarningActive && focusState.warningMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            focusState.warningMessage?.let { msg ->
                Row(
                    modifier = Modifier
                        .padding(top = spacing.xs)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.warning.copy(alpha = 0.18f))
                        .border(1.dp, colors.warning, RoundedCornerShape(8.dp))
                        .padding(horizontal = spacing.sm, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = colors.warning,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = msg,
                        style = QuovexTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.warning
                    )
                }
            }
        }

        Spacer(Modifier.height(spacing.xs))

        // Mini Camera Preview (Expandable / Minimizable)
        AnimatedVisibility(
            visible = isPreviewVisible && focusState.hasCameraPermission && focusState.isCameraActive,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 106.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.5.dp, colors.primary, RoundedCornerShape(12.dp))
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setTargetResolution(Size(480, 640))
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also {
                                    it.setAnalyzer(cameraExecutor, CameraFocusAnalyzer(onFrameAnalyzed))
                                }

                            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalysis
                                )
                            } catch (e: Exception) {
                                // Camera binding fallback
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    }
                )

                // Privacy lock overlay badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                        .padding(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "On-device processing",
                        tint = colors.primary,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}
