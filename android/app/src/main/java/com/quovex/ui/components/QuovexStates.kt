package com.quovex.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quovex.R
import com.quovex.theme.QuovexTheme

@Composable
fun QuovexLoading(
    modifier: Modifier = Modifier,
    message: String = "Loading...",
    fullScreen: Boolean = true
) {
    val colors = QuovexTheme.colors

    Box(
        modifier = if (fullScreen) modifier.fillMaxSize() else modifier.fillMaxWidth().padding(QuovexTheme.spacing.xl),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = colors.primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(40.dp)
            )
            if (message.isNotBlank()) {
                Spacer(modifier = Modifier.height(QuovexTheme.spacing.md))
                Text(
                    text = message,
                    style = QuovexTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun QuovexErrorState(
    message: String,
    modifier: Modifier = Modifier,
    title: String = "Something went wrong",
    onRetry: (() -> Unit)? = null,
    icon: ImageVector = Icons.Filled.ErrorOutline,
    fullScreen: Boolean = true
) {
    val colors = QuovexTheme.colors

    Box(
        modifier = if (fullScreen) modifier.fillMaxSize().padding(QuovexTheme.spacing.xl) else modifier.fillMaxWidth().padding(QuovexTheme.spacing.lg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Error",
                tint = colors.error,
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.base))

            Text(
                text = title,
                style = QuovexTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.xs))

            Text(
                text = message,
                style = QuovexTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )

            if (onRetry != null) {
                Spacer(modifier = Modifier.height(QuovexTheme.spacing.lg))
                QuovexSecondaryButton(
                    text = "Retry",
                    onClick = onRetry,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(0.6f)
                )
            }
        }
    }
}

@Composable
fun QuovexEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    illustration: Painter? = null,
    icon: ImageVector = Icons.Filled.HourglassEmpty,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    fullScreen: Boolean = true
) {
    val colors = QuovexTheme.colors

    Box(
        modifier = if (fullScreen) modifier.fillMaxSize().padding(QuovexTheme.spacing.xl) else modifier.fillMaxWidth().padding(QuovexTheme.spacing.lg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (illustration != null) {
                Image(
                    painter = illustration,
                    contentDescription = null,
                    modifier = Modifier.size(140.dp)
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.textTertiary,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.base))

            Text(
                text = title,
                style = QuovexTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.xs))

            Text(
                text = description,
                style = QuovexTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )

            if (actionText != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(QuovexTheme.spacing.lg))
                QuovexPrimaryButton(
                    text = actionText,
                    onClick = onActionClick,
                    modifier = Modifier.fillMaxWidth(0.7f)
                )
            }
        }
    }
}
