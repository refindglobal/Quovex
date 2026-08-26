package com.quovex.ui.timer.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.domain.model.SoundscapeCategory
import com.quovex.domain.model.SoundscapePreset
import com.quovex.domain.model.SoundscapePresets
import com.quovex.domain.model.SoundscapeState
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.QuovexCard
import com.quovex.ui.components.QuovexChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundscapeSelectorSheet(
    soundscapeState: SoundscapeState,
    onDismiss: () -> Unit,
    onSelectPreset: (SoundscapePreset) -> Unit,
    onSetVolume: (Float) -> Unit,
    onToggleAutoPlay: (Boolean) -> Unit,
    onTogglePlay: () -> Unit
) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedCategory by remember { mutableStateOf(SoundscapeCategory.ALL) }

    val filteredPresets = remember(selectedCategory) {
        if (selectedCategory == SoundscapeCategory.ALL) {
            SoundscapePresets.ALL_PRESETS
        } else {
            SoundscapePresets.ALL_PRESETS.filter { it.category == selectedCategory || it.id == "none" }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        contentColor = colors.textPrimary,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.surfaceVariant)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.lg)
                .padding(bottom = spacing.xl)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🎧 Ambient Focus Soundscapes",
                        style = QuovexTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Zero-distraction acoustic focus masks & binaural beats",
                        style = QuovexTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(Modifier.height(spacing.md))

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(SoundscapeCategory.entries.toTypedArray()) { category ->
                    QuovexChip(
                        label = category.displayName,
                        isSelected = selectedCategory == category,
                        onClick = { selectedCategory = category }
                    )
                }
            }

            Spacer(Modifier.height(spacing.md))

            // Volume & Controls Card
            QuovexCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.md)
                ) {
                    // Volume Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VolumeDown,
                                contentDescription = "Volume",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(spacing.xs))
                            Text(
                                text = "Soundscape Volume",
                                style = QuovexTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                        }
                        Text(
                            text = "${(soundscapeState.volume * 100).toInt()}%",
                            style = QuovexTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                    }

                    Slider(
                        value = soundscapeState.volume,
                        onValueChange = onSetVolume,
                        valueRange = 0.0f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.primary,
                            activeTrackColor = colors.primary,
                            inactiveTrackColor = colors.surfaceVariant
                        )
                    )

                    Spacer(Modifier.height(spacing.xs))

                    // Auto Play with Timer Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-play with Focus Timer",
                                style = QuovexTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Automatically plays during focus, pauses on break/stop",
                                style = QuovexTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                        }
                        Switch(
                            checked = soundscapeState.isAutoPlayWithTimerEnabled,
                            onCheckedChange = onToggleAutoPlay,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.primary,
                                checkedTrackColor = colors.primary.copy(alpha = 0.3f),
                                uncheckedThumbColor = colors.textSecondary,
                                uncheckedTrackColor = colors.surfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(spacing.md))

            // Presets List
            Text(
                text = "SOUND PRESETS",
                style = QuovexTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = colors.textSecondary,
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(spacing.xs))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                items(filteredPresets, key = { it.id }) { preset ->
                    val isSelected = soundscapeState.selectedPreset.id == preset.id
                    val isPlayingThis = isSelected && soundscapeState.isPlaying

                    SoundscapePresetCard(
                        preset = preset,
                        isSelected = isSelected,
                        isPlaying = isPlayingThis,
                        onSelect = { onSelectPreset(preset) },
                        onTogglePlay = {
                            if (isSelected) {
                                onTogglePlay()
                            } else {
                                onSelectPreset(preset)
                                if (!soundscapeState.isPlaying) {
                                    onTogglePlay()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SoundscapePresetCard(
    preset: SoundscapePreset,
    isSelected: Boolean,
    isPlaying: Boolean,
    onSelect: () -> Unit,
    onTogglePlay: () -> Unit
) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing

    val borderColor = if (isSelected) colors.primary else colors.border
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) colors.surfaceVariant.copy(alpha = 0.5f) else colors.surface)
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect)
            .padding(spacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon / Emoji Box
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) colors.primary.copy(alpha = 0.15f) else colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = preset.iconEmoji,
                    fontSize = 20.sp
                )
            }

            Spacer(Modifier.width(spacing.md))

            // Details Column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    Text(
                        text = preset.title,
                        style = QuovexTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) colors.primary else colors.textPrimary
                    )
                    if (preset.tag.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = preset.tag,
                                style = QuovexTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(2.dp))

                Text(
                    text = preset.subtitle,
                    style = QuovexTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 1
                )
            }

            Spacer(Modifier.width(spacing.sm))

            // Equalizer Bars or Play Preview Button
            if (preset.id != "none") {
                if (isPlaying) {
                    EqualizerAnimation()
                    Spacer(Modifier.width(spacing.xs))
                }

                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) colors.primary else colors.surfaceVariant)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause preview" else "Play preview",
                        tint = if (isPlaying) colors.background else colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = colors.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun EqualizerAnimation() {
    val colors = QuovexTheme.colors
    val transition = rememberInfiniteTransition(label = "Equalizer")

    val bar1 by transition.animateFloat(
        initialValue = 4.dp.value,
        targetValue = 18.dp.value,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar1"
    )
    val bar2 by transition.animateFloat(
        initialValue = 16.dp.value,
        targetValue = 6.dp.value,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar2"
    )
    val bar3 by transition.animateFloat(
        initialValue = 8.dp.value,
        targetValue = 20.dp.value,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(22.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(bar1.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(colors.primary)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(bar2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(colors.primary)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(bar3.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(colors.primary)
        )
    }
}
