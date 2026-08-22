package com.quovex.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quovex.theme.QuovexTheme

@Composable
fun QuovexChip(
    label: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val colors = QuovexTheme.colors

    val backgroundColor = when {
        !enabled -> colors.surfaceVariant.copy(alpha = 0.5f)
        isSelected -> colors.primary
        else -> colors.surfaceVariant
    }

    val contentColor = when {
        !enabled -> colors.textDisabled
        isSelected -> colors.onPrimary
        else -> colors.textPrimary
    }

    val borderColor = when {
        !enabled -> colors.borderLight
        isSelected -> colors.primary
        else -> colors.border
    }

    val chipModifier = if (onClick != null && enabled) {
        modifier
            .clickable(onClick = onClick)
            .semantics { role = Role.Checkbox }
    } else {
        modifier
    }

    Surface(
        modifier = chipModifier,
        shape = QuovexTheme.shapes.pill,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = QuovexTheme.spacing.md, vertical = QuovexTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(QuovexTheme.spacing.xs))
            } else if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(QuovexTheme.spacing.xs))
            }

            Text(
                text = label,
                style = QuovexTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Composable
fun QuovexFilterChip(
    label: String,
    isSelected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    QuovexChip(
        label = label,
        modifier = modifier,
        isSelected = isSelected,
        enabled = enabled,
        leadingIcon = if (leadingIcon != null) {
            {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (isSelected) QuovexTheme.colors.onPrimary else QuovexTheme.colors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null,
        onClick = { onSelectedChange(!isSelected) }
    )
}

@Composable
fun QuovexActionChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    QuovexChip(
        label = label,
        modifier = modifier,
        isSelected = false,
        enabled = enabled,
        leadingIcon = if (icon != null) {
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = QuovexTheme.colors.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null,
        onClick = onClick
    )
}
