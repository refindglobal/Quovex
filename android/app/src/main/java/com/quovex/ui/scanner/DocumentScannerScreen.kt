package com.quovex.ui.scanner

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.QuovexButton
import com.quovex.ui.components.QuovexButtonVariant
import com.quovex.ui.components.QuovexCard
import com.quovex.ui.components.QuovexChip
import com.quovex.ui.components.QuovexErrorState
import com.quovex.ui.components.QuovexLoading
import com.quovex.ui.components.QuovexTextField
import com.quovex.ui.components.QuovexTopAppBar

@Composable
fun DocumentScannerScreen(
    viewModel: DocumentScannerViewModel,
    onBackClick: () -> Unit,
    onNoteSaved: (noteId: Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing
    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.processCapturedImage(bitmap)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                viewModel.processCapturedImage(bitmap)
            } catch (e: Exception) {
                // Handled in viewmodel
            }
        }
    }

    Scaffold(
        topBar = {
            QuovexTopAppBar(
                title = "Document Scanner",
                onBackClick = onBackClick,
                actions = {
                    if (state.extractedText.isNotBlank()) {
                        IconButton(onClick = { cameraLauncher.launch() }) {
                            Icon(
                                Icons.Filled.Refresh,
                                contentDescription = "Retake",
                                tint = colors.textSecondary
                            )
                        }
                    }
                }
            )
        },
        containerColor = colors.background
    ) { padding ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = spacing.lg, vertical = spacing.md)
        ) {
            if (state.extractedText.isBlank() && !state.isScanning) {
                // ── INITIAL CAPTURE STATE ────────────────────────────────────
                QuovexCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.xxl),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.DocumentScanner,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(Modifier.height(spacing.base))
                        Text(
                            text = "Scan Physical Notes & Books",
                            style = QuovexTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(Modifier.height(spacing.xs))
                        Text(
                            text = "Use on-device OCR to instantly convert handwritten notes, textbooks, and problem sets into editable digital notes.",
                            style = QuovexTheme.typography.bodyMedium,
                            color = colors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(spacing.xl))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.base)
                        ) {
                            QuovexButton(
                                text = "Take Photo",
                                onClick = { cameraLauncher.launch() },
                                variant = QuovexButtonVariant.Primary,
                                modifier = Modifier.weight(1f)
                            )
                            QuovexButton(
                                text = "From Gallery",
                                onClick = { galleryLauncher.launch("image/*") },
                                variant = QuovexButtonVariant.Secondary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            } else if (state.isScanning) {
                // ── SCANNING / OCR IN PROGRESS ───────────────────────────────
                QuovexLoading(
                    message = "Running on-device OCR scan...",
                    modifier = Modifier.padding(vertical = spacing.xxl)
                )
            } else {
                // ── EXTRACTED TEXT REVIEW & SAVE ─────────────────────────────
                if (!state.error.isNullOrBlank()) {
                    QuovexErrorState(message = state.error!!)
                    Spacer(Modifier.height(spacing.md))
                }

                QuovexTextField(
                    value = state.noteTitle,
                    onValueChange = { viewModel.onTitleChanged(it) },
                    label = "Note Title"
                )

                Spacer(Modifier.height(spacing.sm))

                Text("Subject", style = QuovexTheme.typography.labelSmall, color = colors.textSecondary)
                Spacer(Modifier.height(spacing.xxs))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    items(state.availableSubjects) { subj ->
                        QuovexChip(
                            label = subj,
                            isSelected = state.selectedSubject == subj,
                            onClick = { viewModel.selectSubject(subj) }
                        )
                    }
                }

                Spacer(Modifier.height(spacing.md))

                QuovexTextField(
                    value = state.extractedText,
                    onValueChange = { viewModel.onTextChanged(it) },
                    label = "Extracted OCR Text (Editable)",
                    singleLine = false,
                    minLines = 8
                )

                Spacer(Modifier.height(spacing.xl))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.base)
                ) {
                    QuovexButton(
                        text = if (state.isSaving) "Saving..." else "Save as Note",
                        onClick = { viewModel.saveAsNote(onNoteSaved) },
                        enabled = !state.isSaving && !state.isSummarizing,
                        variant = QuovexButtonVariant.Secondary,
                        modifier = Modifier.weight(1f)
                    )
                    QuovexButton(
                        text = if (state.isSummarizing) "Summarizing..." else "AI Summarize",
                        onClick = { viewModel.summarizeAndSave(onNoteSaved) },
                        enabled = !state.isSaving && !state.isSummarizing,
                        variant = QuovexButtonVariant.Primary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(spacing.lg))
            }
        }
    }
}
