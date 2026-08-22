package com.quovex.ui.ai

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.QuovexButton
import com.quovex.ui.components.QuovexButtonVariant
import com.quovex.ui.components.QuovexCard
import com.quovex.ui.components.QuovexChip
import com.quovex.ui.components.QuovexTextArea
import com.quovex.ui.components.QuovexTopAppBar

@Composable
fun AiNoteSummarizerScreen(
    viewModel: AiNoteSummarizerViewModel,
    onBackClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val subjects = listOf("Physics", "Chemistry", "Maths", "Biology", "History")
    val colors = QuovexTheme.colors

    val sampleNotePhysics = """
        Electromagnetic induction is the process where a conductor placed in a changing magnetic field causes the production of a voltage across the conductor. 
        Faraday's law states that the induced electromotive force (EMF) in any closed circuit is equal to the negative rate of change of the magnetic flux through the circuit: ε = - dΦ/dt.
        Lenz's law specifies that the direction of the induced current opposes the initial change in magnetic flux that produced it, ensuring conservation of energy.
    """.trimIndent()

    Scaffold(
        topBar = {
            QuovexTopAppBar(
                title = "AI Note Parser",
                onBackClick = onBackClick
            )
        },
        containerColor = colors.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = QuovexTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(QuovexTheme.spacing.base),
            contentPadding = PaddingValues(vertical = QuovexTheme.spacing.base)
        ) {
            // Subject Selection Row
            item {
                Text(
                    text = "Select Target Subject",
                    style = QuovexTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(QuovexTheme.spacing.sm))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(QuovexTheme.spacing.sm)) {
                    items(subjects) { subj ->
                        QuovexChip(
                            label = subj,
                            isSelected = state.selectedSubject == subj,
                            onClick = { viewModel.selectSubject(subj) }
                        )
                    }
                }
            }

            // Text Input Box
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Paste Notes / OCR Text",
                        style = QuovexTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    QuovexChip(
                        label = "Load Sample 📋",
                        isSelected = false,
                        onClick = { viewModel.onInputTextChanged(sampleNotePhysics) }
                    )
                }

                Spacer(modifier = Modifier.height(QuovexTheme.spacing.sm))

                QuovexTextArea(
                    value = state.inputText,
                    onValueChange = { viewModel.onInputTextChanged(it) },
                    placeholder = "Paste raw lecture notes, book snippets, or OCR scan here...",
                    minLines = 5,
                    maxLines = 10
                )
            }

            // Summarize Action Button
            item {
                QuovexButton(
                    text = if (state.isProcessing) "Generating AI Notes..." else "Summarize with AI",
                    onClick = { viewModel.summarizeNote() },
                    enabled = !state.isProcessing && state.inputText.isNotBlank(),
                    isLoading = state.isProcessing,
                    variant = QuovexButtonVariant.Primary
                )
            }

            if (state.isProcessing) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = QuovexTheme.spacing.md),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = colors.primary,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(QuovexTheme.spacing.md))
                        Text(
                            text = "Extracting formulas, key points & flashcards...",
                            style = QuovexTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            // Summary Results Container
            if (state.summaryResult != null) {
                val result = state.summaryResult!!

                item {
                    QuovexCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = colors.surface,
                        elevation = QuovexTheme.elevation.card
                    ) {
                        Column(modifier = Modifier.padding(QuovexTheme.spacing.base)) {
                            // Tabs: [Summary] [Key Points] [Flashcards]
                            TabRow(
                                selectedTabIndex = state.selectedTab,
                                containerColor = Color.Transparent,
                                contentColor = colors.primary,
                                indicator = { tabPositions ->
                                    TabRowDefaults.SecondaryIndicator(
                                        modifier = Modifier.tabIndicatorOffset(tabPositions[state.selectedTab]),
                                        color = colors.primary
                                    )
                                },
                                divider = {}
                            ) {
                                Tab(
                                    selected = state.selectedTab == 0,
                                    onClick = { viewModel.selectTab(0) },
                                    text = { Text("Summary", fontWeight = FontWeight.Bold) }
                                )
                                Tab(
                                    selected = state.selectedTab == 1,
                                    onClick = { viewModel.selectTab(1) },
                                    text = { Text("Key Points", fontWeight = FontWeight.Bold) }
                                )
                                Tab(
                                    selected = state.selectedTab == 2,
                                    onClick = { viewModel.selectTab(2) },
                                    text = { Text("Flashcards (${result.flashcards.size})", fontWeight = FontWeight.Bold) }
                                )
                            }

                            Spacer(modifier = Modifier.height(QuovexTheme.spacing.base))

                            when (state.selectedTab) {
                                0 -> {
                                    Text(
                                        text = result.summary,
                                        style = QuovexTheme.typography.bodyMedium,
                                        color = colors.textPrimary,
                                        lineHeight = 22.sp
                                    )
                                }
                                1 -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(QuovexTheme.spacing.sm)) {
                                        result.keyPoints.forEach { point ->
                                            Row(verticalAlignment = Alignment.Top) {
                                                Text("• ", color = colors.primary, fontWeight = FontWeight.Bold)
                                                Text(
                                                    text = point,
                                                    style = QuovexTheme.typography.bodyMedium,
                                                    color = colors.textPrimary,
                                                    lineHeight = 20.sp
                                                )
                                            }
                                        }
                                    }
                                }
                                2 -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(QuovexTheme.spacing.sm)) {
                                        result.flashcards.forEachIndexed { _, card ->
                                            QuovexCard(
                                                modifier = Modifier.fillMaxWidth(),
                                                backgroundColor = colors.surfaceVariant
                                            ) {
                                                Column(modifier = Modifier.padding(QuovexTheme.spacing.md)) {
                                                    Text(
                                                        text = "Q: ${card.question}",
                                                        style = QuovexTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = colors.primary
                                                    )
                                                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.xs))
                                                    Text(
                                                        text = "A: ${card.answer}",
                                                        style = QuovexTheme.typography.bodySmall,
                                                        color = colors.textPrimary
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(QuovexTheme.spacing.xs))

                                        QuovexButton(
                                            text = "Save Flashcards to Deck",
                                            onClick = { viewModel.saveFlashcardsToDeck() },
                                            variant = QuovexButtonVariant.Primary,
                                            height = 44.dp
                                        )
                                    }
                                }
                            }

                            if (state.savedFlashcardsMessage != null) {
                                Spacer(modifier = Modifier.height(QuovexTheme.spacing.md))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Saved Success",
                                        tint = colors.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(QuovexTheme.spacing.xs))
                                    Text(
                                        text = state.savedFlashcardsMessage ?: "",
                                        style = QuovexTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
