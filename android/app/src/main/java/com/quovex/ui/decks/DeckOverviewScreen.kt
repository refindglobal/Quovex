package com.quovex.ui.decks

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
import com.quovex.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quovex.domain.model.DeckStats
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.QuovexButton
import com.quovex.ui.components.QuovexButtonVariant
import com.quovex.ui.components.QuovexCard
import com.quovex.ui.components.QuovexErrorState
import com.quovex.ui.components.QuovexLinearProgressIndicator
import com.quovex.ui.components.QuovexLoading
import com.quovex.ui.components.QuovexTopAppBar

@Composable
fun DeckOverviewScreen(
    viewModel: DeckOverviewViewModel,
    onBackClick: () -> Unit,
    onStudyNowClick: (deckId: Int) -> Unit,
    onReviewAllClick: (deckId: Int) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing

    Scaffold(
        topBar = {
            QuovexTopAppBar(
                title = state.stats?.title ?: "Deck Overview",
                onBackClick = onBackClick
            )
        },
        containerColor = colors.background
    ) { padding ->

        when {
            state.isLoading -> {
                QuovexLoading(modifier = Modifier.padding(padding))
            }

            state.error != null -> {
                QuovexErrorState(
                    message = state.error ?: "An error occurred",
                    onRetry = { viewModel.loadStats() },
                    modifier = Modifier.padding(padding)
                )
            }

            state.isDeckEmpty -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ill_empty_decks_chest),
                        contentDescription = "Empty Decks",
                        modifier = Modifier.size(120.dp)
                    )
                    Spacer(Modifier.height(spacing.lg))
                    Text(
                        "No cards yet",
                        style = QuovexTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(Modifier.height(spacing.sm))
                    Text(
                        "Add flashcards to this deck to begin studying.",
                        style = QuovexTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            state.stats != null -> {
                DeckOverviewContent(
                    stats = state.stats!!,
                    isAllCaughtUp = state.isAllCaughtUp,
                    canStudyDue = state.canStudyDue,
                    canReviewAll = state.canReviewAll,
                    onStudyNowClick = { onStudyNowClick(state.stats!!.deckId) },
                    onReviewAllClick = { onReviewAllClick(state.stats!!.deckId) },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun DeckOverviewContent(
    stats: DeckStats,
    isAllCaughtUp: Boolean,
    canStudyDue: Boolean,
    canReviewAll: Boolean,
    onStudyNowClick: () -> Unit,
    onReviewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.lg, vertical = spacing.md)
    ) {
        // Subject chip
        Text(
            text = stats.subject.uppercase(),
            style = QuovexTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = colors.primary,
            letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
        )

        Spacer(Modifier.height(spacing.xs))

        // All caught up banner
        if (isAllCaughtUp) {
            QuovexCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing.sm),
                backgroundColor = colors.success.copy(alpha = 0.12f),
                borderColor = colors.success.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.base),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "All caught up",
                        tint = colors.success
                    )
                    Column {
                        Text(
                            "You're all caught up!",
                            style = QuovexTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.success
                        )
                        Text(
                            "No cards are due for review today.",
                            style = QuovexTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(spacing.lg))

        // Stats grid
        QuovexCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = QuovexTheme.elevation.card
        ) {
            Column(modifier = Modifier.padding(spacing.lg)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Deck Statistics",
                        style = QuovexTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textSecondary
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_deck_flashcards),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.height(spacing.lg))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(label = "Total", value = "${stats.totalCards}", icon = Icons.Filled.School)
                    StatItem(label = "Due Today", value = "${stats.dueCards}", icon = Icons.Filled.Timer,
                        valueColor = if (stats.dueCards > 0) colors.warning else colors.textPrimary)
                    StatItem(label = "Mastered", value = "${stats.masteredCards}", icon = Icons.Filled.CheckCircle,
                        valueColor = colors.success)
                }

                Spacer(Modifier.height(spacing.xl))

                // Mastery progress bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Mastery",
                        style = QuovexTheme.typography.labelMedium,
                        color = colors.textSecondary
                    )
                    Text(
                        "${stats.masteryPercent}%",
                        style = QuovexTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                        modifier = Modifier.semantics {
                            contentDescription = "${stats.masteryPercent} percent mastered"
                        }
                    )
                }
                Spacer(Modifier.height(spacing.xs))
                QuovexLinearProgressIndicator(
                    progress = stats.masteryPercent / 100f,
                    height = 8.dp,
                    color = colors.primary,
                    trackColor = colors.surfaceVariant
                )

                Spacer(Modifier.height(spacing.md))

                // Learning breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LearningBadge("Learning", stats.learningCards, colors.warning)
                    LearningBadge("Mastered", stats.masteredCards, colors.success)
                    LearningBadge("Due", stats.dueCards,
                        if (stats.dueCards > 0) colors.error else colors.textSecondary)
                }
            }
        }

        Spacer(Modifier.height(spacing.xl))

        // Primary CTA
        QuovexButton(
            text = if (canStudyDue) "Study Now — ${stats.dueCards} Due" else "No Cards Due",
            onClick = onStudyNowClick,
            variant = QuovexButtonVariant.Primary,
            enabled = canStudyDue,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = if (canStudyDue) "Study ${stats.dueCards} due cards" else "No cards due today" }
        )

        if (canReviewAll) {
            Spacer(Modifier.height(spacing.md))
            QuovexButton(
                text = "Review All ${stats.totalCards} Cards",
                onClick = onReviewAllClick,
                variant = QuovexButtonVariant.Secondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Review all ${stats.totalCards} cards regardless of due date" }
            )
        }

        Spacer(Modifier.height(spacing.xl))
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valueColor: androidx.compose.ui.graphics.Color = QuovexTheme.colors.textPrimary
) {
    val colors = QuovexTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, style = QuovexTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = valueColor)
        Text(label, style = QuovexTheme.typography.labelSmall, color = colors.textSecondary)
    }
}

@Composable
private fun LearningBadge(
    label: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "$count",
            style = QuovexTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(label, style = QuovexTheme.typography.labelSmall, color = QuovexTheme.colors.textSecondary)
    }
}
