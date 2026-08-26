package com.quovex.ui.timer.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.domain.model.AppCategory
import com.quovex.domain.model.BlockedAppInfo
import com.quovex.domain.model.DistractionShieldState
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.QuovexButton
import com.quovex.ui.components.QuovexCard
import com.quovex.ui.components.QuovexChip
import com.quovex.ui.components.QuovexTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistractionShieldSheet(
    shieldState: DistractionShieldState,
    onDismiss: () -> Unit,
    onToggleApp: (packageName: String, isBlocked: Boolean) -> Unit,
    onToggleCategory: (category: AppCategory, isBlocked: Boolean) -> Unit,
    onToggleShield: (enabled: Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<AppCategory?>(null) }

    val filteredApps = remember(shieldState.installedApps, searchQuery, selectedCategoryFilter) {
        shieldState.installedApps.filter { app ->
            val matchesSearch = searchQuery.isBlank() ||
                app.appName.contains(searchQuery, ignoreCase = true) ||
                app.packageName.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategoryFilter == null || app.category == selectedCategoryFilter
            matchesSearch && matchesCategory
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        contentColor = colors.textPrimary,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = spacing.md)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.border)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = spacing.lg)
        ) {
            // Header & Global Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(colors.primaryGlow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(spacing.md))
                    Column {
                        Text(
                            text = "Distraction Shield",
                            style = QuovexTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${shieldState.blockedCount} apps blocked during focus",
                            style = QuovexTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }

                Switch(
                    checked = shieldState.isShieldEnabled,
                    onCheckedChange = onToggleShield,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.onPrimary,
                        checkedTrackColor = colors.primary,
                        uncheckedThumbColor = colors.textSecondary,
                        uncheckedTrackColor = colors.surfaceVariant
                    )
                )
            }

            Spacer(Modifier.height(spacing.md))

            // Accessibility Service Banner
            if (!shieldState.isAccessibilityServiceEnabled) {
                QuovexCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        },
                    backgroundColor = colors.surfaceVariant,
                    borderColor = colors.warning,
                    borderWidth = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = colors.warning,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(spacing.sm))
                            Column {
                                Text(
                                    text = "Accessibility Service Required",
                                    style = QuovexTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Tap to enable Quovex in Android Accessibility",
                                    style = QuovexTheme.typography.labelSmall,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        Text(
                            text = "Enable →",
                            style = QuovexTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                    }
                }
                Spacer(Modifier.height(spacing.md))
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = spacing.sm)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(spacing.xs))
                    Text(
                        text = "Accessibility Shield Active & Ready",
                        style = QuovexTheme.typography.labelSmall,
                        color = colors.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Quick Category Toggles Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                contentPadding = PaddingValues(bottom = spacing.sm)
            ) {
                item {
                    QuovexChip(
                        label = "All Apps",
                        isSelected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null }
                    )
                }
                items(AppCategory.values()) { category ->
                    val categoryApps = shieldState.installedApps.filter { it.category == category }
                    if (categoryApps.isNotEmpty()) {
                        val allBlocked = categoryApps.all { it.isBlocked }
                        QuovexChip(
                            label = "${category.iconEmoji} ${category.title} (${categoryApps.size})",
                            isSelected = selectedCategoryFilter == category,
                            onClick = {
                                if (selectedCategoryFilter == category) {
                                    // Batch toggle when already selected
                                    onToggleCategory(category, !allBlocked)
                                } else {
                                    selectedCategoryFilter = category
                                }
                            }
                        )
                    }
                }
            }

            // Search Bar
            QuovexTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search installed apps...",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(spacing.sm))

            // Installed Apps List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    AppRowItem(
                        app = app,
                        onToggle = { isBlocked -> onToggleApp(app.packageName, isBlocked) }
                    )
                }
            }

            Spacer(Modifier.height(spacing.md))

            QuovexButton(
                text = "Apply Distraction Shield (${shieldState.blockedCount} Blocked)",
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = spacing.lg)
            )
        }
    }
}

@Composable
private fun AppRowItem(
    app: BlockedAppInfo,
    onToggle: (Boolean) -> Unit
) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (app.isBlocked) colors.surfaceElevated else colors.surface)
            .border(1.dp, if (app.isBlocked) colors.primary.copy(alpha = 0.5f) else colors.border, RoundedCornerShape(12.dp))
            .padding(horizontal = spacing.md, vertical = spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(colors.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = app.category.iconEmoji,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.width(spacing.md))

            Column {
                Text(
                    text = app.appName,
                    style = QuovexTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    maxLines = 1
                )
                Text(
                    text = "${app.category.title} • ${app.packageName}",
                    style = QuovexTheme.typography.labelSmall,
                    color = colors.textSecondary,
                    maxLines = 1
                )
            }
        }

        Switch(
            checked = app.isBlocked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.onPrimary,
                checkedTrackColor = colors.primary,
                uncheckedThumbColor = colors.textSecondary,
                uncheckedTrackColor = colors.surfaceVariant
            ),
            modifier = Modifier.semantics {
                contentDescription = if (app.isBlocked) "Block ${app.appName}" else "Unblock ${app.appName}"
            }
        )
    }
}
