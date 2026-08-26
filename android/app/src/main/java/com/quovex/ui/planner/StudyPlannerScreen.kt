package com.quovex.ui.planner

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.domain.model.DailyStudyTask
import com.quovex.domain.model.StudyPlan
import com.quovex.domain.model.StudyTaskType
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.QuovexButton
import com.quovex.ui.components.QuovexButtonVariant
import com.quovex.ui.components.QuovexCard
import com.quovex.ui.components.QuovexChip
import com.quovex.ui.components.QuovexEmptyState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@Composable
fun StudyPlannerScreen(
    viewModel: StudyPlannerViewModel,
    onNavigateBack: () -> Unit,
    onStartFocusSession: (subject: String, taskTitle: String, minutes: Int) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val colors = QuovexTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (state.showWizard) {
            PlannerWizardView(
                state = state,
                viewModel = viewModel,
                onCancel = {
                    if (state.activePlan != null) viewModel.closeWizard() else onNavigateBack()
                }
            )
        } else if (state.activePlan != null) {
            PlanRoadmapView(
                state = state,
                viewModel = viewModel,
                onNavigateBack = onNavigateBack,
                onStartFocusSession = onStartFocusSession
            )
        } else {
            QuovexEmptyState(
                title = "No Active Study Plan",
                description = "Build a personalized day-by-day exam revision roadmap with Quovex AI",
                actionText = "Create Study Plan",
                onActionClick = viewModel::openWizard
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Active Plan Roadmap View
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PlanRoadmapView(
    state: StudyPlannerUiState,
    viewModel: StudyPlannerViewModel,
    onNavigateBack: () -> Unit,
    onStartFocusSession: (subject: String, taskTitle: String, minutes: Int) -> Unit
) {
    val plan = state.activePlan ?: return
    val colors = QuovexTheme.colors
    val daysState = rememberLazyListState()

    val daysLeft = ((plan.examDateMillis - System.currentTimeMillis()) / TimeUnit.DAYS.toMillis(1)).coerceAtLeast(0)

    LaunchedEffect(state.selectedDay) {
        if (plan.totalDays > 0) {
            daysState.animateScrollToItem((state.selectedDay - 1).coerceAtLeast(0))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Top Header ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plan.title,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${plan.targetExam} • $daysLeft days remaining",
                    color = colors.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            IconButton(onClick = viewModel::openWizard) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Adjust Plan",
                    tint = colors.textSecondary
                )
            }
        }

        // ── Progress Overview Card ───────────────────────────────────
        QuovexCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            backgroundColor = colors.surface,
            borderColor = colors.border
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Overall Roadmap Progress",
                            color = colors.textSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${(state.totalPlanProgress * 100).roundToInt()}% Completed",
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.primaryContainer)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Day ${state.selectedDay} of ${plan.totalDays}",
                            color = colors.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                val animatedProgress by animateFloatAsState(
                    targetValue = state.totalPlanProgress,
                    animationSpec = tween(600),
                    label = "plan_progress"
                )
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = colors.primary,
                    trackColor = colors.surfaceVariant
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Studied: ${String.format(Locale.getDefault(), "%.1f", state.totalHoursCompleted)} hrs",
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Total: ${String.format(Locale.getDefault(), "%.1f", state.totalHoursPlanned)} hrs",
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // ── Day Selector Carousel ────────────────────────────────────
        Text(
            text = "TIMELINE",
            color = colors.textSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 6.dp)
        )

        LazyRow(
            state = daysState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items((1..plan.totalDays).toList()) { dayNumber ->
                val isSelected = dayNumber == state.selectedDay
                val dayTasks = state.allPlanTasks.filter { it.dayNumber == dayNumber }
                val isDayCompleted = dayTasks.isNotEmpty() && dayTasks.all { it.isCompleted }

                DayPill(
                    dayNumber = dayNumber,
                    isSelected = isSelected,
                    isCompleted = isDayCompleted,
                    onClick = { viewModel.selectDay(dayNumber) }
                )
            }
        }

        // ── Daily Task List ──────────────────────────────────────────
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DAY ${state.selectedDay} OBJECTIVES",
                color = colors.textSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            val dayTasks = state.tasksForSelectedDay
            val completedDayCount = dayTasks.count { it.isCompleted }
            Text(
                text = "$completedDayCount/${dayTasks.size} done",
                color = colors.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (state.tasksForSelectedDay.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tasks generated for Day ${state.selectedDay}",
                    color = colors.textSecondary,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(state.tasksForSelectedDay) { task ->
                    DailyTaskCard(
                        task = task,
                        onToggle = { isChecked -> viewModel.toggleTask(task.id, isChecked) },
                        onStartSession = {
                            onStartFocusSession(task.subject, task.topic, task.estimatedMinutes)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayPill(
    dayNumber: Int,
    isSelected: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    val colors = QuovexTheme.colors
    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> colors.primary
            isCompleted -> colors.primaryContainer
            else -> colors.surface
        },
        label = "day_pill_bg"
    )
    val textColor by animateColorAsState(
        targetValue = when {
            isSelected -> colors.onPrimary
            isCompleted -> colors.primary
            else -> colors.textSecondary
        },
        label = "day_pill_text"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                1.dp,
                if (isSelected) colors.primary else colors.border,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Day",
                fontSize = 10.sp,
                color = textColor.copy(alpha = 0.8f)
            )
            Text(
                text = "$dayNumber",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = textColor
            )
            if (isCompleted) {
                Spacer(Modifier.height(2.dp))
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = if (isSelected) colors.onPrimary else colors.primary,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

@Composable
private fun DailyTaskCard(
    task: DailyStudyTask,
    onToggle: (Boolean) -> Unit,
    onStartSession: () -> Unit
) {
    val colors = QuovexTheme.colors

    QuovexCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (task.isCompleted) colors.surface.copy(alpha = 0.7f) else colors.surface,
        borderColor = if (task.isCompleted) colors.primary.copy(alpha = 0.3f) else colors.border
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox Icon
            IconButton(
                onClick = { onToggle(!task.isCompleted) },
                modifier = Modifier.size(36.dp)
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Completed",
                        tint = colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Circle,
                        contentDescription = "Pending",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Task Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Task Type Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.primaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${task.taskType.icon} ${task.taskType.label}",
                            color = colors.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Subject Pill
                    Text(
                        text = task.subject,
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = task.topic,
                    color = if (task.isCompleted) colors.textSecondary else colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = "⏱️ ${task.estimatedMinutes} mins",
                    color = colors.textSecondary,
                    fontSize = 11.sp
                )
            }

            // Start Session Quick Action
            if (!task.isCompleted) {
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = onStartSession,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.primary)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Start Focus",
                        tint = colors.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 4-Step Planner Creation Wizard
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PlannerWizardView(
    state: StudyPlannerUiState,
    viewModel: StudyPlannerViewModel,
    onCancel: () -> Unit
) {
    val colors = QuovexTheme.colors

    val exams = listOf("JEE Advanced", "NEET UG", "CBSE Class 12", "UPSC", "SAT", "Other")
    val defaultSubjects = listOf("Physics", "Chemistry", "Mathematics", "Biology", "General Focus")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Wizard Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Cancel",
                    tint = colors.textPrimary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI Study Planner",
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Step ${state.wizardStep} of 4",
                    color = colors.primary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Step Progress Indicator
        LinearProgressIndicator(
            progress = { state.wizardStep / 4f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = colors.primary,
            trackColor = colors.surfaceVariant
        )

        Spacer(Modifier.height(20.dp))

        // Step Content
        Box(modifier = Modifier.weight(1f)) {
            when (state.wizardStep) {
                1 -> WizardStep1Exam(state = state, exams = exams, viewModel = viewModel)
                2 -> WizardStep2HoursAndSubjects(state = state, subjects = defaultSubjects, viewModel = viewModel)
                3 -> WizardStep3WeakTopics(state = state, viewModel = viewModel)
                4 -> WizardStep4ReviewAndGenerate(state = state, viewModel = viewModel)
            }
        }

        // Wizard Bottom Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.wizardStep > 1) {
                QuovexButton(
                    text = "Back",
                    variant = QuovexButtonVariant.Outline,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setWizardStep(state.wizardStep - 1) }
                )
            }

            if (state.wizardStep < 4) {
                QuovexButton(
                    text = "Continue",
                    variant = QuovexButtonVariant.Primary,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setWizardStep(state.wizardStep + 1) }
                )
            } else {
                QuovexButton(
                    text = if (state.isGenerating) "Generating Roadmap..." else "Generate Study Plan ✨",
                    variant = QuovexButtonVariant.Primary,
                    modifier = Modifier.weight(1f),
                    enabled = !state.isGenerating,
                    onClick = viewModel::generatePlan
                )
            }
        }
    }
}

@Composable
private fun WizardStep1Exam(
    state: StudyPlannerUiState,
    exams: List<String>,
    viewModel: StudyPlannerViewModel
) {
    val colors = QuovexTheme.colors
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Target Competitive Exam",
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Text(
            text = "Select your target exam or enter custom exam name",
            color = colors.textSecondary,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(16.dp))

        exams.chunked(2).forEach { rowExams ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowExams.forEach { exam ->
                    QuovexChip(
                        label = exam,
                        isSelected = state.selectedExam == exam,
                        onClick = { viewModel.selectExam(exam) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowExams.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        if (state.selectedExam == "Other") {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.customExamName,
                onValueChange = viewModel::setCustomExamName,
                label = { Text("Exam Name", color = colors.textSecondary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.border
                )
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Target Exam Date",
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(Modifier.height(8.dp))
        QuovexCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.surface,
            borderColor = colors.border
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.DateRange, contentDescription = null, tint = colors.primary)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = dateFormat.format(Date(state.examDateMillis)),
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    val days = ((state.examDateMillis - System.currentTimeMillis()) / TimeUnit.DAYS.toMillis(1)).coerceAtLeast(0)
                    Text(
                        text = "$days days from today",
                        color = colors.textSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun WizardStep2HoursAndSubjects(
    state: StudyPlannerUiState,
    subjects: List<String>,
    viewModel: StudyPlannerViewModel
) {
    val colors = QuovexTheme.colors

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Daily Study Commitment",
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Text(
            text = "How many hours can you dedicate each day?",
            color = colors.textSecondary,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(20.dp))

        QuovexCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.surface,
            borderColor = colors.border
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Daily Target", color = colors.textSecondary, fontSize = 13.sp)
                    Text(
                        text = "${state.dailyHours.roundToInt()} hrs / day",
                        color = colors.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(Modifier.height(12.dp))
                Slider(
                    value = state.dailyHours,
                    onValueChange = viewModel::setDailyHours,
                    valueRange = 1f..12f,
                    steps = 10,
                    colors = SliderDefaults.colors(
                        thumbColor = colors.primary,
                        activeTrackColor = colors.primary,
                        inactiveTrackColor = colors.surfaceVariant
                    )
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Included Subjects",
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(Modifier.height(8.dp))

        subjects.chunked(2).forEach { rowSubjects ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowSubjects.forEach { subject ->
                    val isSelected = state.selectedSubjects.contains(subject)
                    QuovexChip(
                        label = subject,
                        isSelected = isSelected,
                        onClick = { viewModel.toggleSubject(subject) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowSubjects.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun WizardStep3WeakTopics(
    state: StudyPlannerUiState,
    viewModel: StudyPlannerViewModel
) {
    val colors = QuovexTheme.colors

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Weak Topics & Pain Points",
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Text(
            text = "AI will allocate higher practice frequency to these areas",
            color = colors.textSecondary,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.weakTopicsInput,
            onValueChange = viewModel::setWeakTopics,
            placeholder = {
                Text(
                    "e.g. Rotational Motion, Organic Chemistry Reactions, Integration by Parts",
                    color = colors.textSecondary.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
            },
            minLines = 4,
            maxLines = 6,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.border,
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface
            )
        )

        Spacer(Modifier.height(16.dp))

        QuovexCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.primaryContainer.copy(alpha = 0.4f),
            borderColor = colors.primary.copy(alpha = 0.3f)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Quovex AI balances theory review, active recall quizzes, and numerical problem sessions automatically.",
                    color = colors.textPrimary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun WizardStep4ReviewAndGenerate(
    state: StudyPlannerUiState,
    viewModel: StudyPlannerViewModel
) {
    val colors = QuovexTheme.colors
    val examName = if (state.selectedExam == "Other") state.customExamName.ifBlank { "Custom Exam" } else state.selectedExam
    val days = ((state.examDateMillis - System.currentTimeMillis()) / TimeUnit.DAYS.toMillis(1)).coerceAtLeast(1)

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Roadmap Synthesis",
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Text(
            text = "Review your configuration before AI generates your day-by-day roadmap",
            color = colors.textSecondary,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(16.dp))

        QuovexCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.surface,
            borderColor = colors.border
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ReviewRow(label = "Target Exam", value = examName)
                ReviewRow(label = "Timeline", value = "$days Days")
                ReviewRow(label = "Daily Target", value = "${state.dailyHours.roundToInt()} hrs / day")
                ReviewRow(label = "Total Hours", value = "${(days * state.dailyHours).roundToInt()} hrs planned")
                ReviewRow(label = "Subjects", value = state.selectedSubjects.joinToString(", "))
                if (state.weakTopicsInput.isNotBlank()) {
                    ReviewRow(label = "Focus Areas", value = state.weakTopicsInput.take(60))
                }
            }
        }

        if (state.generationError != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Error: ${state.generationError}",
                color = colors.error,
                fontSize = 12.sp
            )
        }

        if (state.isGenerating) {
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    color = colors.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Synthesizing curriculum with Cerebras / Groq...",
                    color = colors.textSecondary,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    val colors = QuovexTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = colors.textSecondary, fontSize = 13.sp)
        Text(
            text = value,
            color = colors.textPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
