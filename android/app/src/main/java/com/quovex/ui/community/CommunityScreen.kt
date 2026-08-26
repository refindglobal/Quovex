package com.quovex.ui.community

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.domain.model.FriendProfile
import com.quovex.domain.model.LeaderboardEntry
import com.quovex.domain.model.LeaderboardType
import com.quovex.domain.model.RankTrend
import com.quovex.domain.model.StudyBattle
import com.quovex.domain.model.StudyRoomModel
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.QuovexChip
import com.quovex.ui.components.QuovexEmptyState
import com.quovex.ui.components.QuovexLoading
import com.quovex.ui.components.QuovexTopAppBar

/** Root Community hub screen with 3 tabs: Rooms | Leaderboard | Battles. */
@Composable
fun CommunityScreen(
    viewModel: CommunityViewModel,
    onNavigateToRoom: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val colors = QuovexTheme.colors
    val roomFilters = listOf("All", "JEE Advanced", "NEET UG", "NEET / JEE", "Maths", "Physics")

    Scaffold(
        topBar = {
            QuovexTopAppBar(
                title = "Community",
                actions = {
                    val totalLive = state.rooms.sumOf { it.activeMembers }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = QuovexTheme.spacing.base)
                    ) {
                        QuovexChip(
                            label = "$totalLive Live",
                            isSelected = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.LocalFireDepartment,
                                    contentDescription = null,
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
            if (state.activeTab == CommunityTab.ROOMS) {
                FloatingActionButton(
                    onClick = { /* TODO: Create Room sheet */ },
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Create Room")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {

            // ---- Tab Selector ----
            CommunityTabRow(
                selected = state.activeTab,
                onSelect = viewModel::selectTab,
                colors = colors
            )

            HorizontalDivider(color = colors.surfaceVariant)

            // ---- Tab Content ----
            AnimatedContent(targetState = state.activeTab, label = "CommunityTabContent") { tab ->
                when (tab) {
                    CommunityTab.ROOMS -> RoomsTabContent(
                        rooms = state.rooms,
                        filters = roomFilters,
                        selectedFilter = state.selectedRoomFilter,
                        isLoading = state.isRoomsLoading,
                        onFilterSelect = viewModel::selectRoomFilter,
                        onJoinRoom = { roomId ->
                            viewModel.joinRoom(roomId)
                            onNavigateToRoom(roomId)
                        }
                    )
                    CommunityTab.LEADERBOARD -> LeaderboardTabContent(
                        entries = state.leaderboardEntries,
                        type = state.leaderboardType,
                        subjectFilter = state.leaderboardSubjectFilter,
                        isLoading = state.isLeaderboardLoading,
                        onTypeSelect = viewModel::selectLeaderboardType,
                        onSubjectSelect = viewModel::selectLeaderboardSubject
                    )
                    CommunityTab.BATTLES -> BattlesTabContent(
                        friends = state.friends,
                        battles = state.battles,
                        isLoading = state.isBattlesLoading
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
// Tab Row
// ─────────────────────────────────────────────────────────

@Composable
private fun CommunityTabRow(
    selected: CommunityTab,
    onSelect: (CommunityTab) -> Unit,
    colors: com.quovex.theme.QuovexColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = QuovexTheme.spacing.base, vertical = QuovexTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(QuovexTheme.spacing.sm)
    ) {
        listOf(
            Triple(CommunityTab.ROOMS, "Study Rooms", Icons.Filled.Groups),
            Triple(CommunityTab.LEADERBOARD, "Leaderboard", Icons.Filled.EmojiEvents),
            Triple(CommunityTab.BATTLES, "Battles", Icons.Filled.SportsKabaddi)
        ).forEach { (tab, label, icon) ->
            val isSelected = selected == tab
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) colors.primaryContainer else colors.surface,
                animationSpec = tween(200), label = "tabBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) colors.primary else colors.textSecondary,
                animationSpec = tween(200), label = "tabText"
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor)
                    .clickable { onSelect(tab) }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = label, tint = textColor, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = label,
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
// ROOMS TAB
// ─────────────────────────────────────────────────────────

@Composable
private fun RoomsTabContent(
    rooms: List<StudyRoomModel>,
    filters: List<String>,
    selectedFilter: String,
    isLoading: Boolean,
    onFilterSelect: (String) -> Unit,
    onJoinRoom: (String) -> Unit
) {
    val colors = QuovexTheme.colors
    val filtered = if (selectedFilter == "All") rooms
    else rooms.filter {
        it.targetExam.contains(selectedFilter, true) || it.subject.contains(selectedFilter, true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = QuovexTheme.spacing.base, vertical = QuovexTheme.spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(QuovexTheme.spacing.sm)
        ) {
            items(filters) { filter ->
                QuovexChip(label = filter, isSelected = filter == selectedFilter, onClick = { onFilterSelect(filter) })
            }
        }
        if (isLoading) {
            QuovexLoading(modifier = Modifier.fillMaxSize())
        } else if (filtered.isEmpty()) {
            QuovexEmptyState(title = "No rooms found", description = "Try a different filter or create your own room")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = QuovexTheme.spacing.base, vertical = QuovexTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(QuovexTheme.spacing.sm)
            ) {
                items(filtered) { room ->
                    StudyRoomCard(room = room, onJoin = { onJoinRoom(room.id) })
                }
            }
        }
    }
}

@Composable
private fun StudyRoomCard(room: StudyRoomModel, onJoin: () -> Unit) {
    val colors = QuovexTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.primaryContainer, RoundedCornerShape(16.dp))
            .clickable(onClick = onJoin)
            .padding(QuovexTheme.spacing.base)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = room.name,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    QuovexChip(label = room.subject, isSelected = false)
                    Spacer(Modifier.width(6.dp))
                    QuovexChip(label = room.targetExam, isSelected = false)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(colors.primary)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${room.activeMembers}",
                    color = colors.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(text = "studying", color = colors.textSecondary, fontSize = 10.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(colors.primary)
                .clickable(onClick = onJoin)
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Join Room →", color = colors.onPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────
// LEADERBOARD TAB
// ─────────────────────────────────────────────────────────

@Composable
private fun LeaderboardTabContent(
    entries: List<LeaderboardEntry>,
    type: LeaderboardType,
    subjectFilter: String,
    isLoading: Boolean,
    onTypeSelect: (LeaderboardType) -> Unit,
    onSubjectSelect: (String) -> Unit
) {
    val colors = QuovexTheme.colors
    val subjects = listOf("ALL", "Physics", "Chemistry", "Maths", "Biology")

    Column(modifier = Modifier.fillMaxSize()) {
        // Type selector
        LazyRow(
            contentPadding = PaddingValues(horizontal = QuovexTheme.spacing.base, vertical = QuovexTheme.spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(QuovexTheme.spacing.sm)
        ) {
            items(LeaderboardType.values()) { t ->
                QuovexChip(
                    label = t.name.lowercase().replaceFirstChar { it.uppercase() },
                    isSelected = type == t,
                    onClick = { onTypeSelect(t) }
                )
            }
        }
        if (type == LeaderboardType.SUBJECT) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = QuovexTheme.spacing.base, vertical = QuovexTheme.spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(QuovexTheme.spacing.sm)
            ) {
                items(subjects) { s ->
                    QuovexChip(label = s, isSelected = subjectFilter == s, onClick = { onSubjectSelect(s) })
                }
            }
        }

        if (isLoading) {
            QuovexLoading(modifier = Modifier.fillMaxSize())
        } else if (entries.isEmpty()) {
            QuovexEmptyState(
                title = "No leaderboard data yet",
                description = "Complete study sessions this week to appear on the leaderboard"
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = QuovexTheme.spacing.base,
                    end = QuovexTheme.spacing.base,
                    bottom = QuovexTheme.spacing.base
                )
            ) {
                // Podium for top 3
                if (entries.size >= 3) {
                    item {
                        LeaderboardPodium(top3 = entries.take(3))
                        Spacer(Modifier.height(QuovexTheme.spacing.base))
                    }
                }
                itemsIndexed(entries.drop(if (entries.size >= 3) 3 else 0)) { index, entry ->
                    LeaderboardRow(entry = entry, rank = index + 4)
                }
            }
        }
    }
}

@Composable
private fun LeaderboardPodium(top3: List<LeaderboardEntry>) {
    val colors = QuovexTheme.colors
    val podiumOrder = listOf(1, 0, 2) // Silver, Gold, Bronze positions
    val podiumColors = listOf(Color(0xFFB0BEC5), Color(0xFFFFD700), Color(0xFFCD7F32))
    val podiumHeights = listOf(80.dp, 110.dp, 60.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = QuovexTheme.spacing.base),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        podiumOrder.forEachIndexed { pos, entryIndex ->
            val entry = top3[entryIndex]
            val medalColor = podiumColors[pos]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Scholar avatar circle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(colors.primaryContainer)
                        .border(2.dp, medalColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = colors.primary, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = entry.userName.take(10),
                    color = if (entry.isCurrentUser) colors.primary else colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = "${entry.studyMinutes}m", color = colors.textSecondary, fontSize = 10.sp)
                Spacer(Modifier.height(4.dp))
                // Podium block
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .height(podiumHeights[pos])
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(medalColor.copy(alpha = 0.2f))
                        .border(1.dp, medalColor, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (entryIndex) { 0 -> "🥇"; 1 -> "🥈"; else -> "🥉" },
                        fontSize = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(entry: LeaderboardEntry, rank: Int) {
    val colors = QuovexTheme.colors
    val bgColor = if (entry.isCurrentUser) colors.primaryContainer else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(10.dp))
            .padding(horizontal = QuovexTheme.spacing.sm, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#$rank",
            color = colors.textSecondary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.width(36.dp)
        )
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.surface)
                .border(1.dp, colors.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.userName,
                color = if (entry.isCurrentUser) colors.primary else colors.textPrimary,
                fontWeight = if (entry.isCurrentUser) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = entry.scholarRank, color = colors.textSecondary, fontSize = 11.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${entry.studyMinutes}m",
                color = colors.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (entry.trend) {
                        RankTrend.UP -> Icons.Filled.Star
                        RankTrend.DOWN -> Icons.Filled.Bolt
                        RankTrend.SAME -> Icons.Filled.Bolt
                    },
                    contentDescription = null,
                    tint = when (entry.trend) {
                        RankTrend.UP -> colors.success
                        RankTrend.DOWN -> colors.error
                        RankTrend.SAME -> colors.textSecondary
                    },
                    modifier = Modifier.size(12.dp)
                )
                Text(text = "${entry.xp} XP", color = colors.textSecondary, fontSize = 10.sp)
            }
        }
    }
    HorizontalDivider(color = colors.surfaceVariant.copy(alpha = 0.5f))
}

// ─────────────────────────────────────────────────────────
// BATTLES & FRIENDS TAB
// ─────────────────────────────────────────────────────────

@Composable
private fun BattlesTabContent(
    friends: List<FriendProfile>,
    battles: List<StudyBattle>,
    isLoading: Boolean
) {
    val colors = QuovexTheme.colors
    if (isLoading) {
        QuovexLoading(modifier = Modifier.fillMaxSize())
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(horizontal = QuovexTheme.spacing.base, vertical = QuovexTheme.spacing.base),
        verticalArrangement = Arrangement.spacedBy(QuovexTheme.spacing.base)
    ) {
        // Active Battles
        if (battles.isNotEmpty()) {
            item {
                Text("⚔️ Active Battles", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(QuovexTheme.spacing.sm))
            }
            items(battles) { battle ->
                StudyBattleCard(battle = battle)
            }
            item { Spacer(Modifier.height(QuovexTheme.spacing.sm)) }
        }

        // Friends
        item {
            Text("👥 Friends", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(QuovexTheme.spacing.sm))
        }
        if (friends.isEmpty()) {
            item {
                QuovexEmptyState(
                    title = "No friends yet",
                    description = "Add friends by username or QR code to challenge them to study battles"
                )
            }
        } else {
            items(friends) { friend ->
                FriendRow(friend = friend)
            }
        }
    }
}

@Composable
private fun StudyBattleCard(battle: StudyBattle) {
    val colors = QuovexTheme.colors
    val totalGoal = 600f
    val challengerPct = (battle.challengerMinutes / totalGoal).coerceIn(0f, 1f)
    val opponentPct = (battle.opponentMinutes / totalGoal).coerceIn(0f, 1f)
    val challengerAnim by animateFloatAsState(
        targetValue = challengerPct, animationSpec = tween(800), label = "cPct"
    )
    val opponentAnim by animateFloatAsState(
        targetValue = opponentPct, animationSpec = tween(800), label = "oPct"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.primaryContainer, RoundedCornerShape(16.dp))
            .padding(QuovexTheme.spacing.base)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(battle.targetExam, color = colors.textSecondary, fontSize = 11.sp)
            QuovexChip(
                label = battle.status.name,
                isSelected = battle.status == com.quovex.domain.model.BattleStatus.ACTIVE
            )
        }
        Spacer(Modifier.height(12.dp))
        // Challenger vs Opponent
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text(battle.challengerName, color = colors.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                Text("${battle.challengerMinutes}m", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Text("VS", color = colors.textSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text(battle.opponentName, color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1)
                Text("${battle.opponentMinutes}m", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        // Progress bars
        LinearProgressIndicator(
            progress = { challengerAnim },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = colors.primary,
            trackColor = colors.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { opponentAnim },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = colors.textSecondary,
            trackColor = colors.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun FriendRow(friend: FriendProfile) {
    val colors = QuovexTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .padding(QuovexTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.primaryContainer)
                .border(
                    width = 2.dp,
                    color = if (friend.isStudyingNow) colors.primary else colors.surfaceVariant,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = colors.primary, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = friend.displayName,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (friend.isStudyingNow) {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(colors.primary)
                    )
                }
            }
            Text(
                text = "${friend.scholarRank} • 🔥 ${friend.streakDays}d • ${friend.topSubject}",
                color = colors.textSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = "${friend.totalStudyHours.toInt()}h",
            color = colors.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}
