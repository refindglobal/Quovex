package com.quovex.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.quovex.theme.QuovexTheme

@Composable
fun QuovexSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    count: Int? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val colors = QuovexTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = QuovexTheme.spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = QuovexTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            if (count != null) {
                Spacer(modifier = Modifier.width(QuovexTheme.spacing.sm))
                QuovexChip(
                    label = count.toString(),
                    isSelected = false
                )
            }
        }

        if (actionText != null && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = QuovexTheme.spacing.sm)
            ) {
                Text(
                    text = actionText,
                    style = QuovexTheme.typography.labelLarge,
                    color = colors.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
