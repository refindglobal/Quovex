package com.quovex.ui.community

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.domain.model.RoomChatMessage
import com.quovex.domain.model.RoomMember
import com.quovex.theme.QuovexTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Full-screen Live Study Room experience.
 *
 * Features:
 * - Live member presence grid (avatar circles with focus indicator)
 * - Break-time chat panel (slides in from bottom)
 * - Chat input with send on IME action
 * - Back handler auto-leaves the room via ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyRoomLiveScreen(
    viewModel: StudyRoomLiveViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val colors = QuovexTheme.colors

    // Intercept hardware back — must leave room before navigating away
    BackHandler {
        viewModel.leaveRoom()
        onNavigateBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top App Bar ──────────────────────────────────────────────
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Live Study Room",
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${state.members.size} studying now",
                            color = colors.primary,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.leaveRoom()
                        onNavigateBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }
                },
                actions = {
                    // Break-chat toggle button
                    IconButton(onClick = viewModel::toggleBreakChat) {
                        Icon(
                            imageVector = if (state.isBreakChatOpen) Icons.Filled.Close else Icons.Filled.Chat,
                            contentDescription = "Break Chat",
                            tint = if (state.isBreakChatOpen) colors.primary else colors.textSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface
                )
            )

            // ── Live presence header ─────────────────────────────────────
            LivePresenceStrip(members = state.members)

            // ── Main focus area ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                StudyFocusPanel()
            }
        }

        // ── Break-time chat overlay ──────────────────────────────────────
        AnimatedVisibility(
            visible = state.isBreakChatOpen,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(tween(300)) { it },
            exit = slideOutVertically(tween(300)) { it }
        ) {
            BreakChatPanel(
                messages = state.chatMessages,
                chatInput = state.chatInput,
                onInputChange = viewModel::onChatInputChange,
                onSend = viewModel::sendMessage,
                currentUserId = "guest_user"
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Live Presence Strip
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LivePresenceStrip(members: List<RoomMember>) {
    val colors = QuovexTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(vertical = 12.dp)
    ) {
        if (members.isEmpty()) {
            Text(
                text = "Connecting to room…",
                color = colors.textSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(members) { member ->
                    MemberAvatarChip(member = member)
                }
            }
        }
    }
}

@Composable
private fun MemberAvatarChip(member: RoomMember) {
    val colors = QuovexTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.primaryContainer)
                    .border(
                        width = 2.dp,
                        color = if (member.isStudying) colors.primary else colors.textSecondary.copy(alpha = 0.5f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = member.userName,
                    tint = colors.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
            // Online indicator dot
            if (member.isStudying) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(colors.primary)
                        .border(1.5.dp, colors.surface, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = member.userName.take(8),
            color = colors.textSecondary,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${member.currentSessionMinutes}m",
            color = colors.primary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Study Focus Panel (centre of screen while studying)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StudyFocusPanel() {
    val colors = QuovexTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(24.dp)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(colors.primaryContainer)
                .border(3.dp, colors.primary.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🎯", fontSize = 48.sp)
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Deep Work Mode",
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Stay focused. Your peers are watching. 🔥",
            color = colors.textSecondary,
            fontSize = 14.sp
        )
        Spacer(Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LiveBadge(label = "⚡ Focus on", color = colors.primary)
            LiveBadge(label = "🔕 Distractions off", color = colors.textSecondary)
        }
    }
}

@Composable
private fun LiveBadge(label: String, color: Color) {
    val colors = QuovexTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Break-time Chat Panel
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BreakChatPanel(
    messages: List<RoomChatMessage>,
    chatInput: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    currentUserId: String
) {
    val colors = QuovexTheme.colors
    val listState = rememberLazyListState()

    // Auto-scroll to latest message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(colors.surface)
            .border(
                1.dp,
                colors.primaryContainer,
                RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .imePadding()
            .navigationBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Chat, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Break Chat",
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${messages.size} messages",
                color = colors.textSecondary,
                fontSize = 11.sp
            )
        }

        // Messages list
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "💬 Be the first to say something!",
                    color = colors.textSecondary,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .animateContentSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(
                        message = msg,
                        isOwn = msg.senderId == currentUserId
                    )
                }
            }
        }

        // Input bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = chatInput,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Say something…", color = colors.textSecondary, fontSize = 13.sp) },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.surfaceVariant,
                    cursorColor = colors.primary,
                    focusedContainerColor = colors.background,
                    unfocusedContainerColor = colors.background
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() })
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (chatInput.isBlank()) colors.surfaceVariant else colors.primary)
                    .clickable(enabled = chatInput.isNotBlank(), onClick = onSend),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (chatInput.isBlank()) colors.textSecondary else colors.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: RoomChatMessage,
    isOwn: Boolean
) {
    val colors = QuovexTheme.colors
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val timeStr = timeFormat.format(Date(message.timestampMillis))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start
    ) {
        if (!isOwn) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(colors.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(6.dp))
        }

        Column(
            horizontalAlignment = if (isOwn) Alignment.End else Alignment.Start
        ) {
            if (!isOwn) {
                Text(
                    text = message.senderName,
                    color = colors.primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = if (isOwn) 16.dp else 4.dp,
                            topEnd = if (isOwn) 4.dp else 16.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp
                        )
                    )
                    .background(if (isOwn) colors.primary else colors.background)
                    .border(
                        width = if (isOwn) 0.dp else 1.dp,
                        color = if (isOwn) Color.Transparent else colors.surfaceVariant,
                        shape = RoundedCornerShape(
                            topStart = if (isOwn) 16.dp else 4.dp,
                            topEnd = if (isOwn) 4.dp else 16.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = message.messageText,
                    color = if (isOwn) colors.onPrimary else colors.textPrimary,
                    fontSize = 13.sp
                )
            }
            Text(
                text = timeStr,
                color = colors.textSecondary.copy(alpha = 0.6f),
                fontSize = 9.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}
