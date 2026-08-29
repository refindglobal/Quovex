package com.quovex.ui.ai

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.R
import com.quovex.domain.model.DoubtFollowUpMessage
import com.quovex.theme.BrandEmerald
import com.quovex.theme.BrandEmeraldDim
import com.quovex.theme.QuovexTheme
import com.quovex.theme.SurfaceGlass
import com.quovex.theme.TextPrimary
import com.quovex.theme.TextSecondary
import com.quovex.theme.TextTertiary
import com.quovex.ui.components.QuovexButton
import com.quovex.ui.components.QuovexButtonVariant
import com.quovex.ui.components.QuovexCard
import com.quovex.ui.components.QuovexChip
import com.quovex.ui.components.QuovexErrorState
import com.quovex.ui.components.QuovexLoading
import com.quovex.ui.components.QuovexMathText
import com.quovex.ui.components.QuovexTextField
import com.quovex.ui.components.QuovexTopAppBar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImageDoubtScreen(
    viewModel: ImageDoubtViewModel,
    onBackClick: () -> Unit,
    onNavigateToNote: (materialId: Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing
    val context = LocalContext.current

    var areMistakesExpanded by remember { mutableStateOf(true) }

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
            } catch (_: Exception) {
                // Handled gracefully in viewmodel
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
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(BrandEmeraldDim),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_doubt_scanner_viewfinder),
                                contentDescription = "Doubt Scanner",
                                modifier = Modifier.size(54.dp)
                            )
                        }
                        Spacer(Modifier.height(spacing.base))
                        Text(
                            text = "Snap Any Academic Problem",
                            style = QuovexTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(Modifier.height(spacing.xs))
                        Text(
                            text = "Photograph a math equation, physics diagram, or chemistry problem to receive step-by-step reasoning, formulas, and common traps.",
                            style = QuovexTheme.typography.bodyMedium,
                            color = colors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 20.sp
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
                        .height(220.dp)
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
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Remove Image",
                            tint = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(spacing.md))

                Text("Subject Stream", style = QuovexTheme.typography.labelSmall, color = colors.textSecondary)
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
                    label = "Specific Doubt / Question (Optional)",
                    placeholder = "e.g. Why is torque zero at the center of mass?"
                )

                Spacer(Modifier.height(spacing.base))

                QuovexButton(
                    text = if (state.isSolving) "Analyzing & Solving..." else "Solve Problem with Quovex AI",
                    onClick = { viewModel.solveDoubt() },
                    enabled = !state.isSolving,
                    modifier = Modifier.fillMaxWidth()
                )

                if (state.isSolving) {
                    Spacer(Modifier.height(spacing.lg))
                    QuovexLoading(message = "Analyzing visual problem, identifying concepts & generating step-by-step proof...")
                }

                if (!state.error.isNullOrBlank()) {
                    Spacer(Modifier.height(spacing.md))
                    QuovexErrorState(message = state.error!!)
                }

                // ── 6-TIER STRUCTURED SOLUTION SECTION ───────────────────────
                val structured = state.structuredSolution
                if (structured != null) {
                    Spacer(Modifier.height(spacing.xl))

                    // Solution Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = BrandEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(spacing.xs))
                            Text(
                                text = "Step-by-Step AI Solution",
                                style = QuovexTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BrandEmeraldDim,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "QUOVEX AI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandEmerald,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(spacing.md))

                    // Tier 1 & 2: Problem Identification & Governing Concept
                    QuovexCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = colors.surface
                    ) {
                        Column(modifier = Modifier.padding(spacing.lg)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Lightbulb,
                                    contentDescription = null,
                                    tint = BrandEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(spacing.xs))
                                Text(
                                    text = "Core Concept & Governing Law",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = BrandEmerald
                                )
                            }
                            Spacer(Modifier.height(spacing.xs))
                            QuovexMathText(
                                text = structured.coreConcept,
                                style = QuovexTheme.typography.bodyMedium,
                                color = TextPrimary
                            )

                            if (structured.problemSummary.isNotBlank() && structured.problemSummary != structured.coreConcept) {
                                Spacer(Modifier.height(spacing.sm))
                                Text(
                                    text = "Problem Summary:",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(2.dp))
                                QuovexMathText(
                                    text = structured.problemSummary,
                                    style = QuovexTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // Tier 3: Step-by-Step Mathematical & Conceptual Reasoning
                    Spacer(Modifier.height(spacing.md))
                    Text(
                        text = "Detailed Derivation & Reasoning",
                        style = QuovexTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(spacing.xs))

                    structured.steps.forEachIndexed { index, stepText ->
                        QuovexCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            backgroundColor = colors.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(spacing.md),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(BrandEmeraldDim),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandEmerald
                                    )
                                }
                                Spacer(Modifier.width(spacing.md))
                                Column(modifier = Modifier.weight(1f)) {
                                    QuovexMathText(
                                        text = stepText,
                                        style = QuovexTheme.typography.bodyMedium,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Tier 4: Formulas Extracted
                    if (structured.formulas.isNotEmpty()) {
                        Spacer(Modifier.height(spacing.md))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Calculate,
                                contentDescription = null,
                                tint = Color(0xFF64B5F6),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(spacing.xs))
                            Text(
                                text = "Key Formulas & Theorems",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                        }
                        Spacer(Modifier.height(spacing.xs))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                            verticalArrangement = Arrangement.spacedBy(spacing.sm)
                        ) {
                            structured.formulas.forEach { formula ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF142436),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A5F))
                                ) {
                                    Text(
                                        text = formula.latex,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF90CAF9),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Tier 5: Final Answer & Conclusion
                    Spacer(Modifier.height(spacing.md))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        BrandEmerald.copy(alpha = 0.15f),
                                        Color(0xFF14241B)
                                    )
                                )
                            )
                            .border(1.dp, BrandEmerald.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(spacing.lg)
                    ) {
                        Column {
                            Text(
                                text = "🏁 Final Answer / Result",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = BrandEmerald
                            )
                            Spacer(Modifier.height(spacing.xs))
                            QuovexMathText(
                                text = structured.finalAnswer,
                                style = QuovexTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                    }

                    // Tier 6: Common Mistakes & Pitfalls (Collapsible)
                    if (structured.commonMistakes.isNotEmpty()) {
                        Spacer(Modifier.height(spacing.md))
                        QuovexCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { areMistakesExpanded = !areMistakesExpanded },
                            backgroundColor = Color(0xFF231A14)
                        ) {
                            Column(modifier = Modifier.padding(spacing.md)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.WarningAmber,
                                            contentDescription = null,
                                            tint = Color(0xFFFFB74D),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(spacing.xs))
                                        Text(
                                            text = "Common Student Mistakes & Exam Traps",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFFFFB74D)
                                        )
                                    }
                                    Icon(
                                        imageVector = if (areMistakesExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB74D)
                                    )
                                }

                                AnimatedVisibility(
                                    visible = areMistakesExpanded,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(top = spacing.sm),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        structured.commonMistakes.forEach { mistake ->
                                            Row(verticalAlignment = Alignment.Top) {
                                                Text("⚠️ ", fontSize = 12.sp)
                                                Text(
                                                    text = mistake,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFFFFE0B2),
                                                    lineHeight = 18.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── ACTION BUTTONS: SAVE AS MATERIAL & ADD FLASHCARDS ────
                    Spacer(Modifier.height(spacing.xl))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                    ) {
                        QuovexButton(
                            text = if (state.isSavingAsMaterial) "Saving..." else "Save to Materials",
                            onClick = { viewModel.saveSolutionAsMaterial(onNavigateToNote) },
                            enabled = !state.isSavingAsMaterial,
                            variant = QuovexButtonVariant.Secondary,
                            modifier = Modifier.weight(1f)
                        )
                        QuovexButton(
                            text = if (state.isCreatingFlashcards) "Synthesizing..." else "Create Flashcards",
                            onClick = { viewModel.createFlashcardDeck() },
                            enabled = !state.isCreatingFlashcards,
                            variant = QuovexButtonVariant.Primary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (!state.flashcardsCreatedMessage.isNullOrBlank()) {
                        Spacer(Modifier.height(spacing.sm))
                        Text(
                            text = "⚡ ${state.flashcardsCreatedMessage}",
                            style = QuovexTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BrandEmerald
                        )
                    }

                    // ── FOLLOW-UP MINI-CHAT THREAD (Module B3: L-034) ────────
                    Spacer(Modifier.height(spacing.xxl))
                    DoubtMiniChatSection(
                        messages = state.followUpMessages,
                        inputText = state.followUpInputText,
                        isSending = state.isSendingFollowUp,
                        onInputChanged = { viewModel.onFollowUpInputChanged(it) },
                        onSendMessage = { viewModel.sendFollowUpMessage() },
                        onChipClicked = { chipText -> viewModel.sendFollowUpMessage(chipText) }
                    )
                }

                Spacer(Modifier.height(spacing.xxl))
            }
        }
    }
}

@Composable
private fun DoubtMiniChatSection(
    messages: List<DoubtFollowUpMessage>,
    inputText: String,
    isSending: Boolean,
    onInputChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    onChipClicked: (String) -> Unit
) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing

    QuovexCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = colors.surface
    ) {
        Column(modifier = Modifier.padding(spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.QuestionAnswer,
                    contentDescription = null,
                    tint = BrandEmerald,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(spacing.xs))
                Text(
                    text = "Ask a Follow-Up Doubt",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
            }
            Text(
                text = "Clarify steps, explore alternative methods, or test edge cases with AI Tutor.",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(Modifier.height(spacing.md))

            // Suggested prompt chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                val chips = listOf(
                    "Can we solve this using an alternate method?",
                    "Explain Step 2 in more detail",
                    "What if initial velocity was not zero?",
                    "Why was this formula chosen?"
                )
                items(chips) { chipText ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceGlass,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF26332B)),
                        modifier = Modifier.clickable { onChipClicked(chipText) }
                    ) {
                        Text(
                            text = chipText,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Message thread
            if (messages.isNotEmpty()) {
                Spacer(Modifier.height(spacing.md))
                Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    messages.forEach { msg ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 14.dp,
                                            topEnd = 14.dp,
                                            bottomStart = if (msg.isUser) 14.dp else 2.dp,
                                            bottomEnd = if (msg.isUser) 2.dp else 14.dp
                                        )
                                    )
                                    .background(if (msg.isUser) BrandEmerald else SurfaceGlass)
                                    .border(
                                        1.dp,
                                        if (msg.isUser) BrandEmerald else Color(0xFF26332B),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = if (msg.isUser) "You" else "Quovex AI Tutor",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (msg.isUser) Color.Black.copy(alpha = 0.7f) else BrandEmerald
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    QuovexMathText(
                                        text = msg.text,
                                        style = QuovexTheme.typography.bodyMedium,
                                        color = if (msg.isUser) Color.Black else TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(spacing.md))

            // Input Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputChanged,
                    placeholder = { Text("Ask clarifying question...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandEmerald,
                        unfocusedBorderColor = Color(0xFF26332B),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Surface(
                    shape = CircleShape,
                    color = if (inputText.isNotBlank() && !isSending) BrandEmerald else BrandEmeraldDim,
                    modifier = Modifier
                        .size(44.dp)
                        .clickable(enabled = inputText.isNotBlank() && !isSending) {
                            onSendMessage()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = BrandEmerald,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Send,
                                contentDescription = "Send Follow-Up",
                                tint = if (inputText.isNotBlank()) Color.Black else TextTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
