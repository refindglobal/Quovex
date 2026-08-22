package com.quovex.ui.flashcards

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.domain.model.FlashcardItem
import com.quovex.domain.model.StudySession
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.QuovexButton
import com.quovex.ui.components.QuovexButtonVariant
import com.quovex.ui.components.QuovexCard
import com.quovex.ui.components.QuovexEmptyState
import com.quovex.ui.components.QuovexErrorState
import com.quovex.ui.components.QuovexLinearProgressIndicator
import com.quovex.ui.components.QuovexLoading
import com.quovex.ui.components.QuovexTopAppBar

@Composable
fun FlashcardPlayerScreen(
    viewModel: FlashcardPlayerViewModel,
    onBackClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val colors = QuovexTheme.colors

    Scaffold(
        topBar = {
            QuovexTopAppBar(
                title = state.deckTitle.ifBlank { "Flashcard Review" },
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
                    message = state.error ?: "Something went wrong",
                    onRetry = { viewModel.clearError() },
                    modifier = Modifier.padding(padding)
                )
            }

            state.isDeckComplete -> {
                DeckCompleteScreen(
                    session = state.session,
                    onRestartClick = { viewModel.restartSession() },
                    onBackClick = onBackClick,
                    modifier = Modifier.padding(padding)
                )
            }

            state.isNoDueCards -> {
                QuovexEmptyState(
                    title = "No Cards Due",
                    description = if (state.studyMode == StudyMode.DUE_ONLY)
                        "All cards in this deck are up to date. Use 'Review All' to practice anyway."
                    else
                        "This deck has no flashcards yet.",
                    actionText = "Go Back",
                    onActionClick = onBackClick,
                    modifier = Modifier.padding(padding)
                )
            }

            state.currentCard != null -> {
                StudyContent(
                    state = state,
                    onFlipClick = { viewModel.flipCard() },
                    onReviewClick = { quality -> viewModel.submitReviewRating(quality) },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

// ── Study active content ──────────────────────────────────────────────────────

@Composable
private fun StudyContent(
    state: FlashcardPlayerUiState,
    onFlipClick: () -> Unit,
    onReviewClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing
    val card = state.currentCard ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top: progress
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.progressText,
                    style = QuovexTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textSecondary,
                    modifier = Modifier.semantics {
                        contentDescription = "Card ${state.currentCardIndex + 1} of ${state.cards.size}"
                    }
                )
                val modeLabel = if (state.studyMode == StudyMode.REVIEW_ALL) "All Cards" else "Due Only"
                Text(
                    text = modeLabel,
                    style = QuovexTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
            }

            Spacer(Modifier.height(spacing.sm))

            QuovexLinearProgressIndicator(
                progress = state.progress,
                height = 6.dp,
                color = colors.primary,
                trackColor = colors.surfaceVariant
            )

            Spacer(Modifier.height(spacing.xl))

            // Flashcard with fixed 3D flip
            FlashCard(
                card = card,
                isFlipped = state.isFlipped,
                onFlipClick = onFlipClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            )
        }

        // Bottom: controls
        ReviewControls(
            isFlipped = state.isFlipped,
            isSubmitting = state.isSubmittingReview,
            onFlipClick = onFlipClick,
            onReviewClick = onReviewClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = spacing.xl)
        )
    }
}

// ── 3D Flashcard ─────────────────────────────────────────────────────────────

/**
 * Renders a two-sided flashcard with a smooth 3D Y-axis flip.
 *
 * Implementation uses two separate [Box] composables (front / back), each
 * maintaining its own rotation. The front is visible for rotation 0°→90°,
 * the back is visible for rotation 90°→180°. This avoids the visual jump
 * that occurs when a single Box tries to show both sides around the midpoint.
 *
 * cameraDistance is scaled by pixel density to prevent perspective distortion.
 */
@Composable
private fun FlashCard(
    card: FlashcardItem,
    isFlipped: Boolean,
    onFlipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = QuovexTheme.colors

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 450),
        label = "CardFlip"
    )

    // Front face: visible when rotation is 0° → 90°
    val frontAlpha = if (rotation <= 90f) 1f else 0f
    // Back face: visible when rotation is 90° → 180°
    val backAlpha = if (rotation > 90f) 1f else 0f

    Box(modifier = modifier) {
        // Front face (Question)
        if (frontAlpha > 0f) {
            QuovexCard(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 14f * density
                    }
                    .semantics {
                        contentDescription = "Question card: ${card.frontContent}. Tap to reveal answer."
                        role = Role.Button
                    },
                elevation = QuovexTheme.elevation.card,
                backgroundColor = colors.surfaceVariant,
                borderColor = colors.border,
                onClick = onFlipClick
            ) {
                CardFace(
                    label = "QUESTION",
                    content = card.frontContent,
                    isAnswer = false,
                    showFlipHint = true
                )
            }
        }

        // Back face (Answer) — pre-rotated by 180° so it reads correctly after flip
        if (backAlpha > 0f) {
            QuovexCard(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = rotation - 180f
                        cameraDistance = 14f * density
                    }
                    .semantics {
                        contentDescription = "Answer card: ${card.backContent}. Select a rating below."
                        role = Role.Button
                    },
                elevation = QuovexTheme.elevation.card,
                backgroundColor = colors.surface,
                borderColor = colors.primary.copy(alpha = 0.5f),
                onClick = onFlipClick
            ) {
                CardFace(
                    label = "ANSWER",
                    content = card.backContent,
                    isAnswer = true,
                    showFlipHint = false
                )
            }
        }
    }
}

@Composable
private fun CardFace(
    label: String,
    content: String,
    isAnswer: Boolean,
    showFlipHint: Boolean
) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.xl),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = QuovexTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = if (isAnswer) colors.primary else colors.textSecondary
            )

            Spacer(Modifier.height(spacing.lg))

            Text(
                text = content,
                style = QuovexTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = colors.textPrimary,
                lineHeight = 30.sp
            )

            if (showFlipHint) {
                Spacer(Modifier.height(spacing.xl))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Flip,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.size(spacing.xs))
                    Text(
                        text = "Tap to reveal answer",
                        style = QuovexTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}

// ── Review controls ───────────────────────────────────────────────────────────

@Composable
private fun ReviewControls(
    isFlipped: Boolean,
    isSubmitting: Boolean,
    onFlipClick: () -> Unit,
    onReviewClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = QuovexTheme.spacing
    val colors = QuovexTheme.colors

    Column(modifier = modifier) {
        if (!isFlipped) {
            // Before flip: show accessible "Reveal Answer" button
            QuovexButton(
                text = "Reveal Answer",
                onClick = onFlipClick,
                variant = QuovexButtonVariant.Primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Reveal the answer for this card" }
            )
        } else {
            Text(
                text = "How well did you recall this?",
                style = QuovexTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = spacing.md),
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Again (Quality = 0) — failed recall, resets to 1 day
                QuovexButton(
                    text = "Again\nReset",
                    onClick = { onReviewClick(0) },
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = "Again — failed to recall. Resets scheduling." },
                    variant = QuovexButtonVariant.Danger,
                    enabled = !isSubmitting,
                    height = 56.dp,
                    shape = QuovexTheme.shapes.medium,
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
                )

                // Hard (Quality = 3) — difficult recall
                QuovexButton(
                    text = "Hard\n~1d",
                    onClick = { onReviewClick(3) },
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = "Hard — recalled with difficulty. Schedules approximately 1 day." },
                    variant = QuovexButtonVariant.Secondary,
                    enabled = !isSubmitting,
                    height = 56.dp,
                    shape = QuovexTheme.shapes.medium,
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
                )

                // Good (Quality = 4) — correct with hesitation
                QuovexButton(
                    text = "Good\n~3d",
                    onClick = { onReviewClick(4) },
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = "Good — recalled correctly. Schedules approximately 3 days." },
                    variant = QuovexButtonVariant.Primary,
                    enabled = !isSubmitting,
                    height = 56.dp,
                    shape = QuovexTheme.shapes.medium,
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
                )

                // Easy (Quality = 5) — instant recall
                QuovexButton(
                    text = "Easy\n~7d",
                    onClick = { onReviewClick(5) },
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = "Easy — instant recall. Schedules approximately 7 days." },
                    variant = QuovexButtonVariant.Outline,
                    enabled = !isSubmitting,
                    height = 56.dp,
                    shape = QuovexTheme.shapes.medium,
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ── Deck Complete screen ──────────────────────────────────────────────────────

@Composable
private fun DeckCompleteScreen(
    session: StudySession,
    onRestartClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.EmojiEvents,
            contentDescription = "Deck complete trophy",
            tint = colors.warning,
            modifier = Modifier.size(80.dp)
        )

        Spacer(Modifier.height(spacing.lg))

        Text(
            "Deck Complete!",
            style = QuovexTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )

        Spacer(Modifier.height(spacing.sm))

        Text(
            "You cleared all scheduled cards.",
            style = QuovexTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(spacing.xl))

        // Session summary card
        QuovexCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = QuovexTheme.elevation.card
        ) {
            Column(
                modifier = Modifier.padding(spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Session Summary",
                    style = QuovexTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )
                Spacer(Modifier.height(spacing.lg))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SessionStat("Reviewed", "${session.reviewedCount}", colors.textPrimary)
                    SessionStat("Again", "${session.againCount}", colors.error)
                    SessionStat("Hard", "${session.hardCount}", colors.warning)
                    SessionStat("Good", "${session.goodCount}", colors.primary)
                    SessionStat("Easy", "${session.easyCount}", colors.success)
                }

                if (session.reviewedCount > 0) {
                    Spacer(Modifier.height(spacing.lg))
                    Text(
                        "Accuracy: ${session.accuracyPercent}%",
                        style = QuovexTheme.typography.labelMedium,
                        color = colors.textSecondary
                    )
                    Spacer(Modifier.height(spacing.xs))
                    QuovexLinearProgressIndicator(
                        progress = session.accuracyPercent / 100f,
                        height = 6.dp,
                        color = colors.success,
                        trackColor = colors.surfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(spacing.xxl))

        QuovexButton(
            text = "Study Again",
            onClick = onRestartClick,
            variant = QuovexButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(spacing.md))

        QuovexButton(
            text = "Back to Deck",
            onClick = onBackClick,
            variant = QuovexButtonVariant.Secondary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SessionStat(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = QuovexTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = QuovexTheme.typography.labelSmall, color = QuovexTheme.colors.textSecondary)
    }
}
