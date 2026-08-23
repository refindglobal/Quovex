package com.quovex.ui.ai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.domain.model.SubjectCatalog
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.QuovexCard
import com.quovex.ui.components.QuovexChip
import com.quovex.ui.components.QuovexMathText
import com.quovex.ui.components.QuovexTextField
import com.quovex.ui.components.QuovexTopAppBar

@Composable
fun AiChatScreen(
    viewModel: AiChatViewModel,
    onBackClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val colors = QuovexTheme.colors

    // Universal subject catalog — dynamic list from ViewModel/Catalog
    val subjects = if (state.availableSubjects.isNotEmpty()) {
        state.availableSubjects
    } else {
        SubjectCatalog.chatSelectorNames
    }

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    viewModel.onImageAttached(bitmap)
                }
            } catch (_: Exception) { }
        }
    }

    // Dynamic suggestions based on selected subject
    val suggestions = when (state.selectedSubject.lowercase()) {
        "physics" -> listOf("Explain Lenz's Law with examples", "Derive formula for Solenoid Inductance", "What is Carnot Engine efficiency?")
        "chemistry" -> listOf("Explain Aldol Condensation mechanism", "What is Markovnikov's Rule?", "How does Le Chatelier's Principle work?")
        "mathematics", "maths" -> listOf("Explain L'Hôpital's Rule with 0/0 example", "How to find Maxima and Minima?", "Integration by parts formula")
        "biology" -> listOf("What is DNA Replication?", "Explain Glycolysis steps", "Mendel's Laws of Inheritance")
        "accountancy" -> listOf("Explain rules of Debit and Credit", "What is Goodwill valuation?", "Format of Cash Flow Statement")
        "business studies" -> listOf("Principles of Scientific Management", "Explain Maslow's Need Hierarchy", "Steps in Planning Process")
        "economics" -> listOf("Law of Diminishing Marginal Utility", "Fiscal Deficit vs Revenue Deficit", "Determinants of Demand")
        "history" -> listOf("Causes of Revolt of 1857", "Key features of Harappan Civilization", "Non-Cooperation Movement timeline")
        "political science" -> listOf("Features of Indian Constitution", "Emergency provisions in India", "Role of Election Commission")
        "computer science" -> listOf("Explain recursion with base condition", "Difference between Stack and Queue", "SQL JOIN operations")
        else -> listOf("Summarize this concept", "Explain with real-world examples", "Create 3 practice questions on this")
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            QuovexTopAppBar(
                title = "AI Doubt Solver",
                onBackClick = onBackClick,
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = QuovexTheme.spacing.md)
                    ) {
                        QuovexChip(
                            label = "Quovex AI",
                            isSelected = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = "AI Powered",
                                    tint = colors.onPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )
                    }
                }
            )
        },
        containerColor = colors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Subject Filter Bar (Universal subjects)
            LazyRow(
                contentPadding = PaddingValues(horizontal = QuovexTheme.spacing.lg, vertical = QuovexTheme.spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(QuovexTheme.spacing.sm)
            ) {
                items(subjects) { subj ->
                    QuovexChip(
                        label = subj,
                        isSelected = state.selectedSubject.equals(subj, ignoreCase = true),
                        onClick = { viewModel.selectSubject(subj) }
                    )
                }
            }

            // Chat Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = QuovexTheme.spacing.lg, vertical = QuovexTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(QuovexTheme.spacing.md)
            ) {
                items(state.messages) { msg ->
                    if (msg.isUser) {
                        // User Bubble
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(0.82f)
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 18.dp,
                                            topEnd = 4.dp,
                                            bottomStart = 18.dp,
                                            bottomEnd = 18.dp
                                        )
                                    )
                                    .background(colors.primary)
                                    .padding(QuovexTheme.spacing.base),
                                horizontalAlignment = Alignment.End
                            ) {
                                // If image was attached, display it
                                msg.attachedImageBitmap?.let { bmp ->
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = "User attached image",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(160.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .padding(bottom = 8.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Text(
                                    text = msg.text,
                                    style = QuovexTheme.typography.bodyMedium,
                                    color = colors.onPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        // AI Bubble
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            QuovexCard(
                                modifier = Modifier.fillMaxWidth(0.92f),
                                backgroundColor = colors.surface,
                                borderColor = colors.border,
                                elevation = QuovexTheme.elevation.low
                            ) {
                                Column(modifier = Modifier.padding(QuovexTheme.spacing.base)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = QuovexTheme.spacing.sm)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AutoAwesome,
                                            contentDescription = "Quovex AI",
                                            tint = colors.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(QuovexTheme.spacing.xs))
                                        Text(
                                            text = "Quovex Tutor (${state.selectedSubject})",
                                            style = QuovexTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.primary
                                        )
                                    }

                                    QuovexMathText(
                                        text = msg.text,
                                        style = QuovexTheme.typography.bodyMedium,
                                        color = colors.textPrimary,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }
                    }
                }

                if (state.isTyping) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = QuovexTheme.spacing.sm)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = colors.primary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(QuovexTheme.spacing.sm))
                            Text(
                                text = "Quovex AI is analyzing your doubt...",
                                style = QuovexTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }

            // Quick Question Suggestions
            LazyRow(
                contentPadding = PaddingValues(horizontal = QuovexTheme.spacing.lg, vertical = QuovexTheme.spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(QuovexTheme.spacing.sm)
            ) {
                items(suggestions) { sugg ->
                    QuovexChip(
                        label = sugg,
                        isSelected = false,
                        onClick = { viewModel.sendMessage(sugg) }
                    )
                }
            }

            // Attached image preview banner if user picked an image
            state.attachedImage?.let { bmp ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = QuovexTheme.spacing.base, vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceVariant)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Attached preview",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Image attached for AI vision analysis",
                        style = QuovexTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.clearAttachedImage() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove attached image",
                            tint = colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.xxs))

            // Bottom Input Bar with image attachment button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = QuovexTheme.spacing.base, vertical = QuovexTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attach image button
                IconButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Attach problem image",
                        tint = if (state.attachedImage != null) colors.primary else colors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                QuovexTextField(
                    value = state.inputText,
                    onValueChange = { viewModel.onInputTextChanged(it) },
                    placeholder = if (state.attachedImage != null) "Ask about this image..." else "Ask a doubt in ${state.selectedSubject}...",
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { viewModel.sendMessage() })
                )

                Spacer(modifier = Modifier.width(QuovexTheme.spacing.sm))

                IconButton(
                    onClick = { viewModel.sendMessage() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(colors.primary, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = colors.onPrimary
                    )
                }
            }
        }
    }
}
