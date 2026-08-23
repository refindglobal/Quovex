package com.quovex.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Context for the AI action sheet — determines which actions are available.
 *
 * SELECTED_TEXT: User has selected/extracted text → content-level actions
 * PAGE: User is viewing a single page → page-level actions
 * PAGES: Multiple pages selected → multi-page summary actions
 * IMAGE: Image-based content (image PDF page, scanned image) → visual AI actions
 * CHAPTER: Full chapter context → chapter-level study actions
 *
 * Actions are filtered by context — never show all actions for every context.
 */
enum class AiActionContext {
    SELECTED_TEXT,
    PAGE,
    PAGES,
    IMAGE,
    CHAPTER
}

/** An actionable item in the AI action sheet */
data class AiAction(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val contexts: Set<AiActionContext>
)

/** All available AI actions with their valid contexts */
private val ALL_AI_ACTIONS = listOf(
    AiAction(
        id = "explain",
        label = "Explain",
        icon = Icons.Default.Lightbulb,
        contexts = setOf(AiActionContext.SELECTED_TEXT, AiActionContext.PAGE, AiActionContext.IMAGE)
    ),
    AiAction(
        id = "summarize",
        label = "Summarize",
        icon = Icons.Default.Summarize,
        contexts = setOf(
            AiActionContext.SELECTED_TEXT, AiActionContext.PAGE,
            AiActionContext.PAGES, AiActionContext.IMAGE
        )
    ),
    AiAction(
        id = "simplify",
        label = "Simplify",
        icon = Icons.Default.TextFields,
        contexts = setOf(AiActionContext.SELECTED_TEXT)
    ),
    AiAction(
        id = "solve",
        label = "Solve",
        icon = Icons.Default.AutoAwesome,
        contexts = setOf(AiActionContext.IMAGE)
    ),
    AiAction(
        id = "study_notes",
        label = "Create Study Notes",
        icon = Icons.Default.NoteAdd,
        contexts = setOf(AiActionContext.PAGES)
    ),
    AiAction(
        id = "add_to_notes",
        label = "Add to Notes",
        icon = Icons.Default.NoteAdd,
        contexts = setOf(AiActionContext.SELECTED_TEXT, AiActionContext.PAGE, AiActionContext.IMAGE)
    ),
    AiAction(
        id = "flashcards",
        label = "Make Flashcards",
        icon = Icons.Default.FlashOn,
        contexts = setOf(
            AiActionContext.SELECTED_TEXT, AiActionContext.PAGE,
            AiActionContext.PAGES, AiActionContext.IMAGE, AiActionContext.CHAPTER
        )
    ),
    AiAction(
        id = "quiz",
        label = "Make Quiz",
        icon = Icons.Default.Quiz,
        contexts = setOf(
            AiActionContext.SELECTED_TEXT, AiActionContext.PAGE,
            AiActionContext.PAGES, AiActionContext.CHAPTER
        )
    ),
    AiAction(
        id = "study_guide",
        label = "Create Study Guide",
        icon = Icons.Default.Book,
        contexts = setOf(AiActionContext.CHAPTER)
    ),
    AiAction(
        id = "ask_ai",
        label = "Ask Quovex AI",
        icon = Icons.Default.Chat,
        contexts = AiActionContext.entries.toSet()  // Available in ALL contexts
    )
)

/**
 * Context-aware AI action bottom sheet.
 *
 * Shows only the actions appropriate for the current [context].
 * Never shows all actions regardless of context.
 *
 * @param context The current content context (page, selected text, chapter, etc.)
 * @param contextLabel Human-readable label for what content is selected (e.g. "Chapter 3", "Page 7")
 * @param onActionSelected Callback when user taps an action — receives the action ID
 * @param onDismiss Called when sheet is dismissed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuovexAiActionSheet(
    context: AiActionContext,
    contextLabel: String = "",
    onActionSelected: (actionId: String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Filter actions to only those valid for this context
    val availableActions = ALL_AI_ACTIONS.filter { context in it.contexts }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Quovex AI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (contextLabel.isNotBlank()) {
                        Text(
                            text = contextLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Actions grid — minimal 2-column layout
            availableActions.chunked(2).forEach { rowActions ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowActions.forEach { action ->
                        AiActionChip(
                            action = action,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onActionSelected(action.id)
                                onDismiss()
                            }
                        )
                    }
                    // Fill empty cell if odd number of actions in last row
                    if (rowActions.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun AiActionChip(
    action: AiAction,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = action.icon,
            contentDescription = action.label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.height(22.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = action.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
