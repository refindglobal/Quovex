package com.quovex.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.quovex.theme.QuovexTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuovexDialog(
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit
) {
    val colors = QuovexTheme.colors

    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier.fillMaxWidth(0.92f),
        properties = properties
    ) {
        Surface(
            shape = QuovexTheme.shapes.dialog,
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border),
            shadowElevation = QuovexTheme.elevation.dialog,
            tonalElevation = QuovexTheme.elevation.medium
        ) {
            Column(
                modifier = Modifier.padding(QuovexTheme.spacing.xl)
            ) {
                Text(
                    text = title,
                    style = QuovexTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(QuovexTheme.spacing.base))

                content()
            }
        }
    }
}

@Composable
fun QuovexConfirmDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    isDanger: Boolean = false,
    isLoading: Boolean = false
) {
    val colors = QuovexTheme.colors

    QuovexDialog(
        onDismissRequest = onDismissRequest,
        title = title
    ) {
        Column {
            Text(
                text = message,
                style = QuovexTheme.typography.bodyMedium,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.xl))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                QuovexTextButton(
                    text = dismissText,
                    onClick = onDismissRequest,
                    enabled = !isLoading,
                    modifier = Modifier.width(100.dp)
                )

                Spacer(modifier = Modifier.width(QuovexTheme.spacing.sm))

                QuovexButton(
                    text = confirmText,
                    onClick = onConfirm,
                    variant = if (isDanger) QuovexButtonVariant.Danger else QuovexButtonVariant.Primary,
                    isLoading = isLoading,
                    modifier = Modifier.width(120.dp),
                    height = 44.dp
                )
            }
        }
    }
}
