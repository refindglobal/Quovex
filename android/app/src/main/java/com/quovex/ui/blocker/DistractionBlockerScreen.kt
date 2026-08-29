package com.quovex.ui.blocker

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.quovex.domain.model.AppCategory
import com.quovex.domain.model.BlockedAppInfo
import com.quovex.domain.model.DistractionShieldState
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.QuovexButton
import com.quovex.ui.components.QuovexCard
import com.quovex.ui.components.QuovexChip
import com.quovex.ui.components.QuovexTextField
import com.quovex.ui.components.QuovexTopAppBar

@Composable
fun DistractionBlockerScreen(
    viewModel: DistractionBlockerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val shieldState by viewModel.shieldState.collectAsState()

    DistractionBlockerContent(
        shieldState = shieldState,
        onNavigateBack = onNavigateBack,
        onToggleShield = { viewModel.toggleShield(it) },
        onToggleApp = { pkg, isBlocked -> viewModel.toggleAppBlocked(pkg, isBlocked) },
        onToggleCategory = { cat, isBlocked -> viewModel.setCategoryBlocked(cat, isBlocked) },
        onRefreshApps = { viewModel.refreshInstalledApps() }
    )
}

@Composable
fun DistractionBlockerContent(
    shieldState: DistractionShieldState,
    onNavigateBack: () -> Unit,
    onToggleShield: (Boolean) -> Unit,
    onToggleApp: (packageName: String, isBlocked: Boolean) -> Unit,
    onToggleCategory: (category: AppCategory, isBlocked: Boolean) -> Unit,
    onRefreshApps: () -> Unit
) {
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

    Scaffold(
        topBar = {
            QuovexTopAppBar(
                title = "Distraction Blocker",
                subtitle = "Shield & App Restrictor",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = onRefreshApps,
                        modifier = Modifier.semantics { contentDescription = "Refresh Apps List" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = colors.textPrimary
                        )
                    }
                }
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = spacing.md, vertical = spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            // Master Shield Banner
            item {
                QuovexCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (shieldState.isShieldEnabled) colors.primary.copy(alpha = 0.5f) else colors.border
                ) {
                    Column(modifier = Modifier.padding(spacing.md)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (shieldState.isShieldEnabled)
                                                colors.primary.copy(alpha = 0.15f)
                                            else
                                                colors.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = if (shieldState.isShieldEnabled) colors.primary else colors.textTertiary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(spacing.md))
                                Column {
                                    Text(
                                        text = "Distraction Shield",
                                        style = QuovexTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = if (shieldState.isShieldEnabled)
                                            "${shieldState.blockedCount} apps currently blocked"
                                        else
                                            "Shield is disabled",
                                        style = QuovexTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            Switch(
                                checked = shieldState.isShieldEnabled,
                                onCheckedChange = onToggleShield,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colors.surface,
                                    checkedTrackColor = colors.primary,
                                    uncheckedThumbColor = colors.textTertiary,
                                    uncheckedTrackColor = colors.surfaceVariant
                                )
                            )
                        }

                        // Stats counters
                        Spacer(modifier = Modifier.height(spacing.md))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.surfaceVariant.copy(alpha = 0.6f))
                                    .padding(spacing.sm),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${shieldState.totalAttemptsResistedToday}",
                                        style = QuovexTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primary
                                    )
                                    Text(
                                        text = "Resisted Today",
                                        style = QuovexTheme.typography.labelSmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.surfaceVariant.copy(alpha = 0.6f))
                                    .padding(spacing.sm),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${shieldState.blockedCount}",
                                        style = QuovexTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Blocked Apps",
                                        style = QuovexTheme.typography.labelSmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Accessibility Service Warning / Status
            item {
                if (!shieldState.isAccessibilityServiceEnabled) {
                    QuovexCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = colors.warning.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = colors.warning,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(spacing.md))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Accessibility Permission Required",
                                    style = QuovexTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.warning
                                )
                                Text(
                                    text = "Why: Needed to detect and block distracting apps during focus sessions.\nWhere: Settings > Accessibility > Installed Apps > Quovex.\nIf Denied: App blocking will fail.",
                                    style = QuovexTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                                Spacer(modifier = Modifier.height(spacing.xs))
                                QuovexButton(
                                    text = "Enable Service",
                                    onClick = {
                                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                    },
                                    modifier = Modifier.height(34.dp)
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.primary.copy(alpha = 0.1f))
                            .padding(horizontal = spacing.md, vertical = spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(spacing.sm))
                        Text(
                            text = "Accessibility Shield Active & Running",
                            style = QuovexTheme.typography.labelMedium,
                            color = colors.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Search Bar
            item {
                QuovexTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Search installed applications...",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = colors.textTertiary
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    item {
                        QuovexChip(
                            label = "All (${shieldState.installedApps.size})",
                            isSelected = selectedCategoryFilter == null,
                            onClick = { selectedCategoryFilter = null }
                        )
                    }
                    items(AppCategory.values()) { category ->
                        val count = shieldState.installedApps.count { it.category == category }
                        if (count > 0) {
                            QuovexChip(
                                label = "${category.iconEmoji} ${category.title} ($count)",
                                isSelected = selectedCategoryFilter == category,
                                onClick = { selectedCategoryFilter = category }
                            )
                        }
                    }
                }
            }

            // Quick Batch Action if category selected
            if (selectedCategoryFilter != null) {
                item {
                    val category = selectedCategoryFilter!!
                    val categoryApps = shieldState.installedApps.filter { it.category == category }
                    val allBlocked = categoryApps.isNotEmpty() && categoryApps.all { it.isBlocked }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${category.iconEmoji} ${category.title}",
                            style = QuovexTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )

                        Text(
                            text = if (allBlocked) "Unblock All in Category" else "Block All in Category",
                            style = QuovexTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.primary,
                            modifier = Modifier
                                .clickable { onToggleCategory(category, !allBlocked) }
                                .padding(spacing.xs)
                        )
                    }
                }
            }

            // Apps List Items
            items(
                items = filteredApps,
                key = { it.packageName }
            ) { app ->
                AppBlockerItemRow(
                    app = app,
                    onToggle = { isBlocked -> onToggleApp(app.packageName, isBlocked) }
                )
            }

            if (filteredApps.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = spacing.xl),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No applications found matching \"$searchQuery\"" else "No applications discovered",
                            style = QuovexTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppBlockerItemRow(
    app: BlockedAppInfo,
    onToggle: (Boolean) -> Unit
) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing

    QuovexCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!app.isBlocked) },
        borderColor = if (app.isBlocked) colors.primary.copy(alpha = 0.3f) else colors.border
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (app.isBlocked)
                            colors.primary.copy(alpha = 0.15f)
                        else
                            colors.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = app.category.iconEmoji,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = app.appName,
                        style = QuovexTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    Text(
                        text = app.category.title,
                        style = QuovexTheme.typography.labelSmall,
                        color = colors.textTertiary
                    )

                    if (app.attemptsResistedCount > 0) {
                        Text(
                            text = "•",
                            style = QuovexTheme.typography.labelSmall,
                            color = colors.textTertiary
                        )
                        Text(
                            text = "${app.attemptsResistedCount} resisted",
                            style = QuovexTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = colors.primary
                        )
                    }
                }
            }

            Switch(
                checked = app.isBlocked,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.surface,
                    checkedTrackColor = colors.primary,
                    uncheckedThumbColor = colors.textTertiary,
                    uncheckedTrackColor = colors.surfaceVariant
                )
            )
        }
    }
}
