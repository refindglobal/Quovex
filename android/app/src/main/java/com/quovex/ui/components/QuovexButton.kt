package com.quovex.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quovex.theme.QuovexTheme

enum class QuovexButtonVariant {
    Primary,
    Secondary,
    Outline,
    Ghost,
    Danger
}

@Composable
fun QuovexButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: QuovexButtonVariant = QuovexButtonVariant.Primary,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    shape: Shape = QuovexTheme.shapes.large,
    height: Dp = 52.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
) {
    val colors = QuovexTheme.colors
    val isInteractive = enabled && !isLoading

    val content: @Composable () -> Unit = {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = when (variant) {
                        QuovexButtonVariant.Primary -> colors.onPrimary
                        QuovexButtonVariant.Danger -> colors.onError
                        else -> colors.primary
                    },
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(QuovexTheme.spacing.sm))
            } else if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(QuovexTheme.spacing.sm))
            }

            Text(
                text = text,
                style = QuovexTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            if (!isLoading && trailingIcon != null) {
                Spacer(modifier = Modifier.width(QuovexTheme.spacing.sm))
                trailingIcon()
            }
        }
    }

    when (variant) {
        QuovexButtonVariant.Primary -> {
            Button(
                onClick = { if (isInteractive) onClick() },
                enabled = isInteractive,
                modifier = modifier
                    .fillMaxWidth()
                    .height(height)
                    .semantics { role = Role.Button },
                shape = shape,
                contentPadding = contentPadding,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                    disabledContainerColor = colors.primary.copy(alpha = 0.35f),
                    disabledContentColor = colors.onPrimary.copy(alpha = 0.5f)
                )
            ) {
                content()
            }
        }

        QuovexButtonVariant.Secondary, QuovexButtonVariant.Outline -> {
            OutlinedButton(
                onClick = { if (isInteractive) onClick() },
                enabled = isInteractive,
                modifier = modifier
                    .fillMaxWidth()
                    .height(height)
                    .semantics { role = Role.Button },
                shape = shape,
                contentPadding = contentPadding,
                border = BorderStroke(
                    1.5.dp,
                    if (isInteractive) colors.border else colors.border.copy(alpha = 0.4f)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = colors.textPrimary,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = colors.textDisabled
                )
            ) {
                content()
            }
        }

        QuovexButtonVariant.Ghost -> {
            TextButton(
                onClick = { if (isInteractive) onClick() },
                enabled = isInteractive,
                modifier = modifier
                    .height(height)
                    .semantics { role = Role.Button },
                shape = shape,
                contentPadding = contentPadding,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colors.primary,
                    disabledContentColor = colors.textDisabled
                )
            ) {
                content()
            }
        }

        QuovexButtonVariant.Danger -> {
            Button(
                onClick = { if (isInteractive) onClick() },
                enabled = isInteractive,
                modifier = modifier
                    .fillMaxWidth()
                    .height(height)
                    .semantics { role = Role.Button },
                shape = shape,
                contentPadding = contentPadding,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.error,
                    contentColor = colors.onError,
                    disabledContainerColor = colors.error.copy(alpha = 0.35f),
                    disabledContentColor = colors.onError.copy(alpha = 0.5f)
                )
            ) {
                content()
            }
        }
    }
}

@Composable
fun QuovexPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    QuovexButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        variant = QuovexButtonVariant.Primary,
        enabled = enabled,
        isLoading = isLoading,
        leadingIcon = leadingIcon
    )
}

@Composable
fun QuovexSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    QuovexButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        variant = QuovexButtonVariant.Secondary,
        enabled = enabled,
        isLoading = isLoading,
        leadingIcon = leadingIcon
    )
}

@Composable
fun QuovexTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    QuovexButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        variant = QuovexButtonVariant.Ghost,
        enabled = enabled,
        isLoading = isLoading
    )
}

@Composable
fun QuovexIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = QuovexTheme.colors.textPrimary,
    containerColor: Color = Color.Transparent
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(48.dp)
            .semantics {
                this.contentDescription = contentDescription
                role = Role.Button
            },
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = containerColor,
            contentColor = tint,
            disabledContentColor = QuovexTheme.colors.textDisabled
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) tint else QuovexTheme.colors.textDisabled,
            modifier = Modifier.size(24.dp)
        )
    }
}
