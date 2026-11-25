package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.curriculum.CurriculumCatalog
import com.example.model.LessonProgressDoc
import com.example.model.PracticeTestAttemptDoc
import com.example.model.StudyScheduleItemDoc
import com.example.model.UiState
import com.example.model.UserProgressProfile
import com.example.ui.components.OnlineStatusBadge
import com.example.ui.components.StatSummaryCard
import com.example.ui.theme.ForestEmerald
import com.example.ui.theme.ForestEmeraldDark
import com.example.ui.theme.MintBorder
import com.example.ui.theme.MintSurface
import com.example.ui.theme.ParchmentBorder
import com.example.ui.theme.ParchmentCard
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberSoft

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProgressScheduleScreen(
    profile: UserProgressProfile,
    lessonProgressMap: Map<String, LessonProgressDoc>,
    scheduleState: UiState<List<StudyScheduleItemDoc>>,
    testAttemptsState: UiState<List<PracticeTestAttemptDoc>>,
    isGeneratingPlan: Boolean,
    planError: String?,
    isOnline: Boolean,
    onGenerateSchedule: (Long, String) -> Unit,
    onAddCustomTask: (String, String, String, Long) -> Unit,
    onToggleScheduleTask: (StudyScheduleItemDoc) -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTrack = CurriculumCatalog.tracks.firstOrNull { it.id == profile.selectedTrackId }
        ?: CurriculumCatalog.tracks.first()
    var dailyMinutes by remember(profile.dailyGoalMinutes) {
        mutableLongStateOf(profile.dailyGoalMinutes.coerceIn(15L, 90L))
    }
    var examGoal by remember(profile.targetExamDate) { mutableStateOf(profile.targetExamDate) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    val scheduleItems = (scheduleState as? UiState.Success)?.data.orEmpty()
    val testAttempts = (testAttemptsState as? UiState.Success)?.data.orEmpty()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PROGRESS & STUDY PLANNER",
                        style = MaterialTheme.typography.labelSmall,
                        color = ForestEmerald
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Track your mastery",
                        style = MaterialTheme.typography.displaySmall
                    )
                }
                OnlineStatusBadge(isOnline = isOnline)
            }
        }

        // Core XP/Gems/Streak Card
        item {
            StatSummaryCard(
                streakDays = profile.streakDays,
                totalXp = profile.totalXp,
                gems = profile.gems
            )
        }

        // Personalized Study Schedule Builder
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MintSurface,
                border = BorderStroke(1.dp, MintBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.CalendarMonth, null, tint = ForestEmerald)
                        Column {
                            Text(
                                text = "Personalized Study Schedule",
                                style = MaterialTheme.typography.titleLarge,
                                color = ForestEmeraldDark
                            )
                            Text(
                                text = "Tailored for ${activeTrack.title} (${activeTrack.tagline})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "Daily commitment (minutes)",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(15L, 25L, 45L, 60L).forEach { mins ->
                            val selected = dailyMinutes == mins
                            FilterChip(
                                selected = selected,
                                onClick = { dailyMinutes = mins },
                                label = { Text("${mins}m") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ForestEmerald,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.background
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = examGoal,
                        onValueChange = {
                            examGoal = it
                            if (planError != null) onClearError()
                        },
                        label = { Text("Target Exam or Goal Date (optional)") },
                        placeholder = { Text("e.g., March Boards / UPSC Prelims June") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("schedule_goal_input"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ForestEmerald,
                            unfocusedBorderColor = MintBorder,
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background
                        )
                    )

                    if (!planError.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = WarmAmberSoft,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = planError,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF7B3E00),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onGenerateSchedule(dailyMinutes, examGoal) },
                            enabled = !isGeneratingPlan,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("generate_schedule_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestEmerald)
                        ) {
                            if (isGeneratingPlan) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Planning...")
                            } else {
                                Icon(Icons.Filled.AutoAwesome, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Build AI Plan")
                            }
                        }

                        OutlinedButton(
                            onClick = { showAddTaskDialog = true },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("add_schedule_task_button"),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, ForestEmerald)
                        ) {
                            Icon(Icons.Filled.Add, null, tint = ForestEmerald)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add", color = ForestEmeraldDark)
                        }
                    }
                }
            }
        }

        // Study Schedule Task List
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Study Routine",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    val doneCount = scheduleItems.count { it.completed }
                    Text(
                        text = "$doneCount/${scheduleItems.size} done",
                        style = MaterialTheme.typography.labelLarge,
                        color = ForestEmerald
                    )
                }

                if (scheduleItems.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = ParchmentCard,
                        border = BorderStroke(1.dp, ParchmentBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("No scheduled blocks yet", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "Tap 'Build AI Plan' to let Owl design a balanced schedule across your subjects, or tap 'Add' to create custom study slots.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        scheduleItems.forEach { task ->
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = if (task.completed) MintSurface else ParchmentCard,
                                border = BorderStroke(1.dp, if (task.completed) MintBorder else ParchmentBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { onToggleScheduleTask(task) }
                                    .testTag("schedule_item_${task.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (task.completed) ForestEmerald else MaterialTheme.colorScheme.background,
                                        border = BorderStroke(1.5.dp, if (task.completed) ForestEmerald else ParchmentBorder),
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (task.completed) {
                                                Icon(
                                                    Icons.Filled.Check,
                                                    contentDescription = "Completed",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(17.dp)
                                                )
                                            }
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = task.topicTitle,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
                                            )
                                        )
                                        Text(
                                            text = "${task.dayLabel} • ${task.subject} • ${task.activityType.replace('_', ' ')}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = MaterialTheme.colorScheme.background
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Filled.Schedule, null, tint = WarmAmber, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "${task.durationMinutes}m",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Practice Test Analytics Chart & History
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Practice Test Performance",
                    style = MaterialTheme.typography.headlineMedium
                )
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = ParchmentCard,
                    border = BorderStroke(1.dp, ParchmentBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val recent = testAttempts.take(6).reversed()
                        if (recent.isEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Filled.Quiz, null, tint = WarmAmber)
                                Text(
                                    text = "Complete a practice test in the Learn tab to see your accuracy trend here.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Text(
                                text = "Recent Accuracy (%)",
                                style = MaterialTheme.typography.labelLarge,
                                color = ForestEmeraldDark
                            )
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                            ) {
                                val barAreaW = size.width / recent.size.coerceAtLeast(1)
                                val barW = (barAreaW * 0.52f).coerceAtMost(48f)
                                recent.forEachIndexed { idx, attempt ->
                                    val ratio = (attempt.score.toFloat() / attempt.totalQuestions.coerceAtLeast(1).toFloat())
                                        .coerceIn(0.08f, 1f)
                                    val barH = size.height * 0.85f * ratio
                                    val left = idx * barAreaW + (barAreaW - barW) / 2f
                                    val top = size.height - barH
                                    drawRoundRect(
                                        color = if (ratio >= 0.7f) ForestEmerald else WarmAmber,
                                        topLeft = Offset(left, top),
                                        size = Size(barW, barH),
                                        cornerRadius = CornerRadius(12f, 12f)
                                    )
                                }
                            }
                            testAttempts.take(4).forEach { attempt ->
                                val pct = (attempt.score * 100) / attempt.totalQuestions.coerceAtLeast(1)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${attempt.testTitle} (${attempt.subject})",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${attempt.score}/${attempt.totalQuestions} ($pct%)",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = ForestEmeraldDark
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Milestone Badges
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Milestone Rewards (${profile.unlockedMilestones.size}/${CurriculumCatalog.milestones.size})",
                    style = MaterialTheme.typography.headlineMedium
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CurriculumCatalog.milestones.forEach { badge ->
                        val unlocked = badge.id in profile.unlockedMilestones ||
                            (badge.id == "first_lesson" && lessonProgressMap.values.any { it.completed })
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (unlocked) MintSurface else ParchmentCard,
                            border = BorderStroke(1.dp, if (unlocked) MintBorder else ParchmentBorder),
                            modifier = Modifier.fillMaxWidth(0.48f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (unlocked) Icons.Filled.EmojiEvents else Icons.Filled.Lock,
                                        contentDescription = badge.title,
                                        tint = if (unlocked) WarmAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "+${badge.gemBonus} gems",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (unlocked) ForestEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(badge.title, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = badge.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        AddScheduleTaskDialog(
            subjects = activeTrack.subjects,
            defaultSubject = profile.selectedSubject,
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { subj, topic, day, mins ->
                onAddCustomTask(subj, topic, day, mins)
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
private fun AddScheduleTaskDialog(
    subjects: List<String>,
    defaultSubject: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Long) -> Unit
) {
    var chosenSubject by remember { mutableStateOf(defaultSubject.ifBlank { subjects.first() }) }
    var topic by remember { mutableStateOf("") }
    var dayLabel by remember { mutableStateOf("Today") }
    var mins by remember { mutableLongStateOf(25L) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        title = { Text("Add Study Block", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Topic to study") },
                    placeholder = { Text("e.g., Revise Quadratic Equations") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Day", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Today", "Tomorrow", "Weekend").forEach { d ->
                        FilterChip(
                            selected = dayLabel == d,
                            onClick = { dayLabel = d },
                            label = { Text(d) }
                        )
                    }
                }
                Text("Duration", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(15L, 25L, 45L).forEach { m ->
                        FilterChip(
                            selected = mins == m,
                            onClick = { mins = m },
                            label = { Text("${m}m") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(chosenSubject, topic, dayLabel, mins) },
                enabled = topic.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ForestEmerald)
            ) {
                Text("Save Block")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
