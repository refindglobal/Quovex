package com.quovex.ui.community

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quovex.R
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.QuovexButton
import com.quovex.ui.components.QuovexButtonVariant
import com.quovex.ui.components.QuovexCard
import com.quovex.ui.components.QuovexChip
import com.quovex.ui.components.QuovexEmptyState
import com.quovex.ui.components.QuovexLoading
import com.quovex.ui.components.QuovexTopAppBar

@Composable
fun CommunityScreen(
    viewModel: CommunityViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val filters = listOf("All", "JEE Advanced", "NEET / JEE", "NEET UG", "Maths", "Physics")
    val colors = QuovexTheme.colors

    val filteredRooms = if (state.selectedFilter == "All") {
        state.rooms
    } else {
        state.rooms.filter { it.targetExam.contains(state.selectedFilter, ignoreCase = true) || it.subject.contains(state.selectedFilter, ignoreCase = true) }
    }

    val totalActiveStudents = state.rooms.sumOf { it.activeMembers }

    Scaffold(
        topBar = {
            QuovexTopAppBar(
                title = "Live Study Rooms",
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = QuovexTheme.spacing.base)
                    ) {
                        QuovexChip(
                            label = "$totalActiveStudents Live",
                            isSelected = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.LocalFireDepartment,
                                    contentDescription = "Live Study Count",
                                    tint = colors.warning,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            )
        },
        containerColor = colors.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                containerColor = colors.primary,
                contentColor = colors.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Create Room",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Category Filter Pills
            LazyRow(
                contentPadding = PaddingValues(horizontal = QuovexTheme.spacing.lg, vertical = QuovexTheme.spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(QuovexTheme.spacing.sm)
            ) {
                items(filters) { filter ->
                    QuovexChip(
                        label = filter,
                        isSelected = state.selectedFilter == filter,
                        onClick = { viewModel.selectFilter(filter) }
                    )
                }
            }

            if (state.isLoading) {
                QuovexLoading(message = "Loading live study rooms...")
            } else if (filteredRooms.isEmpty()) {
                QuovexEmptyState(
                    title = "No Active Study Rooms",
                    description = "No live study groups found for \"${state.selectedFilter}\". Start your own room now!",
                    actionText = "Create Study Room",
                    onActionClick = { }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = QuovexTheme.spacing.lg, vertical = QuovexTheme.spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(QuovexTheme.spacing.md)
                ) {
                    items(filteredRooms) { room ->
                        QuovexCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = colors.surface,
                            elevation = QuovexTheme.elevation.card
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(QuovexTheme.spacing.base)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    QuovexChip(
                                        label = room.targetExam,
                                        isSelected = false
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Groups,
                                            contentDescription = "Members",
                                            tint = colors.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(QuovexTheme.spacing.xs))
                                        Text(
                                            text = "${room.activeMembers} studying",
                                            style = QuovexTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(QuovexTheme.spacing.sm))

                                Text(
                                    text = room.name,
                                    style = QuovexTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )

                                Spacer(modifier = Modifier.height(QuovexTheme.spacing.xxs))

                                Text(
                                    text = "Subject: ${room.subject}",
                                    style = QuovexTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )

                                Spacer(modifier = Modifier.height(QuovexTheme.spacing.base))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar Stack
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy((-10).dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val avatarDrawables = listOf(R.drawable.avatar_2, R.drawable.avatar_3, R.drawable.avatar_4)
                                        avatarDrawables.forEach { resId ->
                                            Image(
                                                painter = painterResource(id = resId),
                                                contentDescription = "Participant Avatar",
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .border(2.dp, colors.surface, CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }

                                    val isJoined = state.joinedRoomId == room.id

                                    QuovexButton(
                                        text = if (isJoined) "Joined ✓" else "Join Room",
                                        onClick = { viewModel.joinRoom(room.id) },
                                        variant = if (isJoined) QuovexButtonVariant.Secondary else QuovexButtonVariant.Primary,
                                        height = 38.dp,
                                        leadingIcon = if (isJoined) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Filled.CheckCircle,
                                                    contentDescription = "Joined",
                                                    tint = colors.primary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        } else null
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
            }
        }
    }
}
