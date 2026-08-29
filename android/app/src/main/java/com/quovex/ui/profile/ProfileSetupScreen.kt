package com.quovex.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.R
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.QuovexButton
import com.quovex.ui.components.QuovexCard
import com.quovex.ui.components.QuovexChip
import com.quovex.ui.components.QuovexTextField

@Composable
fun ProfileSetupScreen(
    viewModel: ProfileSetupViewModel,
    onSetupComplete: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val colors = QuovexTheme.colors

    val avatarList = listOf(
        1 to R.drawable.avatar_1,
        2 to R.drawable.avatar_2,
        3 to R.drawable.avatar_3,
        4 to R.drawable.avatar_4,
        5 to R.drawable.avatar_5,
        6 to R.drawable.avatar_6,
        7 to R.drawable.avatar_7,
        8 to R.drawable.avatar_8,
        9 to R.drawable.avatar_9,
        10 to R.drawable.avatar_10,
        11 to R.drawable.avatar_11,
        12 to R.drawable.avatar_12
    )

    val gradeLevels = listOf("Class 10", "Class 11", "Class 12", "Dropper / Repeater", "College")
    val targetYears = listOf("2026", "2027", "2028")

    Scaffold(
        containerColor = colors.background,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .padding(QuovexTheme.spacing.base)
            ) {
                QuovexButton(
                    text = "Enter Command Center",
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.RocketLaunch,
                            contentDescription = null
                        )
                    },
                    onClick = { viewModel.completeProfile(onSetupComplete) },
                    isLoading = state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(QuovexTheme.spacing.base)
        ) {
            Text(
                text = "Claim Your Identity 👤",
                style = QuovexTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.xs))

            Text(
                text = "Choose your student avatar and handle to stand out on the leaderboard.",
                style = QuovexTheme.typography.bodyMedium,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.xl))

            // ── Avatar Grid ──────────────────────────────────────────────
            Text(
                text = "Select Avatar",
                style = QuovexTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.md))

            QuovexCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface,
                elevation = QuovexTheme.elevation.card
            ) {
                Column(modifier = Modifier.padding(QuovexTheme.spacing.base)) {
                    val rows = avatarList.chunked(4)
                    rows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowItems.forEach { (id, resId) ->
                                val isSelected = state.selectedAvatarId == id
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) colors.primaryContainer else colors.surfaceElevated)
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
                                            color = if (isSelected) colors.primary else colors.border,
                                            shape = CircleShape
                                        )
                                        .clickable { viewModel.selectAvatar(id) }
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = resId),
                                        contentDescription = "Avatar $id",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(colors.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = "Selected",
                                                tint = colors.background,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(QuovexTheme.spacing.sm))
                    }
                }
            }

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.xl))

            // ── Username / @handle ────────────────────────────────────────
            Text(
                text = "Student Handle",
                style = QuovexTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.xs))

            QuovexTextField(
                value = state.username,
                onValueChange = { viewModel.onUsernameChanged(it) },
                label = "Username / @handle",
                placeholder = "e.g. quantum_master",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = colors.textSecondary
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.xl))

            // ── Grade Level Selection ────────────────────────────────────
            Text(
                text = "Current Academic Grade",
                style = QuovexTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                gradeLevels.take(3).forEach { grade ->
                    val isSelected = state.gradeLevel == grade
                    QuovexChip(
                        label = grade,
                        isSelected = isSelected,
                        onClick = { viewModel.selectGrade(grade) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                gradeLevels.drop(3).forEach { grade ->
                    val isSelected = state.gradeLevel == grade
                    QuovexChip(
                        label = grade,
                        isSelected = isSelected,
                        onClick = { viewModel.selectGrade(grade) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.xl))

            // ── Target Exam Year ─────────────────────────────────────────
            Text(
                text = "Target Exam Year",
                style = QuovexTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                targetYears.forEach { year ->
                    val isSelected = state.targetYear == year
                    QuovexChip(
                        label = "Batch $year",
                        isSelected = isSelected,
                        onClick = { viewModel.selectYear(year) }
                    )
                }
            }

            // ── Error Message ─────────────────────────────────────────────
            AnimatedVisibility(visible = state.errorMessage != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.md))
                    Text(
                        text = state.errorMessage ?: "",
                        style = QuovexTheme.typography.bodySmall,
                        color = colors.error,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.xxl))
        }
    }
}
