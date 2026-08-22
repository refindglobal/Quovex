package com.quovex.ui.ai

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
fun ImageDoubtScreen(
    viewModel: ImageDoubtViewModel,
    onBackClick: () -> Unit,
    onNavigateToNote: (noteId: Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing
    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.onImageCaptured(bitmap)
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
                viewModel.onImageCaptured(bitmap)
            } catch (e: Exception) {
                // Handled in viewmodel
            }
        }
    }

    Scaffold(
        topBar = {
            QuovexTopAppBar(
                title = "Photo Doubt Solver",
                onBackClick = onBackClick
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
            if (state.capturedBitmap == null) {
                // ── NO IMAGE CAPTURED STATE ──────────────────────────────────
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
                            Icons.Filled.Psychology,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(Modifier.height(spacing.base))
                        Text(
                            text = "Snap Any Problem",
                            style = QuovexTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(Modifier.height(spacing.xs))
                        Text(
                            text = "Photograph a math equation, physics diagram, or chemistry reaction to get step-by-step visual solutions powered by AI Vision.",
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
            } else {
                // ── IMAGE CAPTURED STATE ─────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceVariant)
                ) {
                    Image(
                        bitmap = state.capturedBitmap!!.asImageBitmap(),
                        contentDescription = "Problem Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    IconButton(
                        onClick = { viewModel.clearImage() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(spacing.xs)
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Remove Image",
                            tint = colors.error
                        )
                    }
                }

                Spacer(Modifier.height(spacing.md))

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

                Spacer(Modifier.height(spacing.sm))

                QuovexTextField(
                    value = state.questionText,
                    onValueChange = { viewModel.onQuestionTextChanged(it) },
                    label = "Optional Question or Specific Doubt",
                    placeholder = "e.g. How do I solve for tension T in step 2?"
                )

                Spacer(Modifier.height(spacing.base))

                QuovexButton(
                    text = if (state.isSolving) "Analyzing & Solving..." else "Solve Problem with AI",
                    onClick = { viewModel.solveDoubt() },
                    enabled = !state.isSolving,
                    modifier = Modifier.fillMaxWidth()
                )

                if (state.isSolving) {
                    Spacer(Modifier.height(spacing.lg))
                    QuovexLoading(message = "Analyzing visual problem & formatting solution...")
                }

                if (!state.error.isNullOrBlank()) {
                    Spacer(Modifier.height(spacing.md))
                    QuovexErrorState(message = state.error!!)
                }

                // ── SOLUTION RENDERING ───────────────────────────────────────
                if (!state.solutionText.isNullOrBlank()) {
                    Spacer(Modifier.height(spacing.xl))
                    QuovexCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = colors.surface
                    ) {
                        Column(modifier = Modifier.padding(spacing.lg)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(spacing.xs))
                                    Text(
                                        text = "Step-by-Step Solution",
                                        style = QuovexTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primary
                                    )
                                }
                                if (state.solutionProvider != null) {
                                    QuovexChip(label = state.solutionProvider!!.uppercase(), isSelected = false)
                                }
                            }

                            Spacer(Modifier.height(spacing.md))

                            Text(
                                text = state.solutionText!!,
                                style = QuovexTheme.typography.bodyMedium,
                                color = colors.textPrimary,
                                lineHeight = QuovexTheme.typography.bodyMedium.lineHeight
                            )

                            Spacer(Modifier.height(spacing.xl))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                            ) {
                                QuovexButton(
                                    text = if (state.isSavingAsNote) "Saving..." else "Save as Note",
                                    onClick = { viewModel.saveSolutionAsNote(onNavigateToNote) },
                                    enabled = !state.isSavingAsNote,
                                    variant = QuovexButtonVariant.Secondary,
                                    modifier = Modifier.weight(1f)
                                )
                                QuovexButton(
                                    text = if (state.isCreatingFlashcards) "Creating..." else "Add Flashcards",
                                    onClick = { viewModel.createFlashcardDeck() },
                                    enabled = !state.isCreatingFlashcards,
                                    variant = QuovexButtonVariant.Primary,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (!state.flashcardsCreatedMessage.isNullOrBlank()) {
                                Spacer(Modifier.height(spacing.sm))
                                Text(
                                    text = "✅ ${state.flashcardsCreatedMessage}",
                                    style = QuovexTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(spacing.xxl))
            }
        }
    }
}
