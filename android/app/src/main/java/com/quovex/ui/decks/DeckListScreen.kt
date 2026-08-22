package com.quovex.ui.decks

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quovex.R
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.QuovexButton
import com.quovex.ui.components.QuovexButtonVariant
import com.quovex.ui.components.QuovexCard
import com.quovex.ui.components.QuovexChip
import com.quovex.ui.components.QuovexDialog
import com.quovex.ui.components.QuovexEmptyState
import com.quovex.ui.components.QuovexLinearProgressIndicator
import com.quovex.ui.components.QuovexTextField
import com.quovex.ui.components.QuovexTopAppBar

@Composable
fun DeckListScreen(
    viewModel: DeckListViewModel,
    onDeckClick: (Int) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val categories = listOf("All", "Physics", "Chemistry", "Maths", "Biology", "History")
    var showCreateDialog by androidx.compose.runtime.remember { mutableStateOf(false) }
    var newDeckTitle by androidx.compose.runtime.remember { mutableStateOf("") }
    var newDeckSubject by androidx.compose.runtime.remember { mutableStateOf("") }
    val colors = QuovexTheme.colors

    Scaffold(
        topBar = {
            QuovexTopAppBar(title = "Knowledge Base")
        },
        containerColor = colors.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = colors.primary,
                contentColor = colors.onPrimary,
                shape = QuovexTheme.shapes.large
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = QuovexTheme.spacing.base),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Create Deck")
                    Spacer(modifier = Modifier.size(QuovexTheme.spacing.sm))
                    Text("Create Deck", style = QuovexTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->

        if (showCreateDialog) {
            QuovexDialog(
                onDismissRequest = { showCreateDialog = false },
                title = "Create New Deck"
            ) {
                Column {
                    QuovexTextField(
                        value = newDeckTitle,
                        onValueChange = { newDeckTitle = it },
                        label = "Deck Title",
                        placeholder = "e.g. Thermodynamics & Heat Transfer"
                    )

                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.md))

                    QuovexTextField(
                        value = newDeckSubject,
                        onValueChange = { newDeckSubject = it },
                        label = "Subject",
                        placeholder = "e.g. Physics"
                    )

                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.xl))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        QuovexButton(
                            text = "Cancel",
                            onClick = { showCreateDialog = false },
                            variant = QuovexButtonVariant.Ghost,
                            modifier = Modifier.padding(end = QuovexTheme.spacing.sm),
                            height = 44.dp
                        )

                        QuovexButton(
                            text = "Create",
                            onClick = {
                                if (newDeckTitle.isNotBlank() && newDeckSubject.isNotBlank()) {
                                    viewModel.createDeck(newDeckTitle.trim(), newDeckSubject.trim())
                                    showCreateDialog = false
                                    newDeckTitle = ""
                                    newDeckSubject = ""
                                }
                            },
                            enabled = newDeckTitle.isNotBlank() && newDeckSubject.isNotBlank(),
                            variant = QuovexButtonVariant.Primary,
                            height = 44.dp
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Category Filter Pills
            LazyRow(
                contentPadding = PaddingValues(horizontal = QuovexTheme.spacing.lg, vertical = QuovexTheme.spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(QuovexTheme.spacing.sm)
            ) {
                items(categories) { cat ->
                    QuovexChip(
                        label = cat,
                        isSelected = state.selectedCategory == cat,
                        onClick = { viewModel.selectCategory(cat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.xs))

            if (state.filteredDecks.isEmpty()) {
                QuovexEmptyState(
                    title = "No Decks in ${state.selectedCategory}",
                    description = "Create a custom flashcard deck or generate one using our AI study tools.",
                    illustration = painterResource(id = R.drawable.ill_empty_deck),
                    actionText = "Create Deck",
                    onActionClick = { showCreateDialog = true }
                )
            } else {
                // Decks List from Room
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = QuovexTheme.spacing.lg, end = QuovexTheme.spacing.lg, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(QuovexTheme.spacing.base)
                ) {
                    items(state.filteredDecks) { deck ->
                        QuovexCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            elevation = QuovexTheme.elevation.card,
                            onClick = { onDeckClick(deck.id) }
                        ) {
                            Image(
                                painter = painterResource(id = deck.bgResId),
                                contentDescription = deck.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.35f),
                                                Color.Black.copy(alpha = 0.94f)
                                            )
                                        )
                                    )
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(QuovexTheme.spacing.base),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    QuovexChip(
                                        label = deck.subject.uppercase(),
                                        isSelected = false
                                    )
                                    Text(
                                        text = "+${deck.xpValue} XP",
                                        style = QuovexTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primary
                                    )
                                }

                                Column {
                                    Text(
                                        text = deck.title,
                                        style = QuovexTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.xxs))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${deck.dueCards} Due Today • ${deck.totalCards} Cards",
                                            style = QuovexTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            text = "${deck.masteryPercent}%",
                                            style = QuovexTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.xs))
                                    QuovexLinearProgressIndicator(
                                        progress = deck.masteryPercent / 100f,
                                        height = 4.dp,
                                        color = colors.primary,
                                        trackColor = colors.surfaceVariant
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
