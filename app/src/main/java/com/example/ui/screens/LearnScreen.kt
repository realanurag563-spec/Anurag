package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.curriculum.CurriculumCatalog
import com.example.model.LessonProgressDoc
import com.example.model.PracticeTestPack
import com.example.model.QuestLesson
import com.example.ui.components.OnlineStatusBadge
import com.example.ui.components.OwlMascotAvatar
import com.example.ui.components.OwlMood
import com.example.ui.theme.ForestEmerald
import com.example.ui.theme.ForestEmeraldDark
import com.example.ui.theme.MintBorder
import com.example.ui.theme.MintSurface
import com.example.ui.theme.ParchmentBorder
import com.example.ui.theme.ParchmentCard
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberSoft

@Composable
fun LearnScreen(
    selectedTrackId: String,
    selectedSubject: String,
    lessons: List<QuestLesson>,
    tests: List<PracticeTestPack>,
    progressMap: Map<String, LessonProgressDoc>,
    isOnline: Boolean,
    isAiLoading: Boolean,
    aiError: String?,
    initialModeTests: Boolean = false,
    onStartLesson: (QuestLesson) -> Unit,
    onStartTest: (PracticeTestPack) -> Unit,
    onGenerateAiLesson: (String) -> Unit,
    onGenerateAiTest: (topic: String, difficulty: String, count: Int) -> Unit,
    onOpen3DLab: (String?) -> Unit,
    onClearAiError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTrack = CurriculumCatalog.tracks.firstOrNull { it.id == selectedTrackId }
        ?: CurriculumCatalog.tracks.first()
    var showTestsTab by remember(initialModeTests) { mutableStateOf(initialModeTests) }
    var customTopic by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf("Standard") }
    var selectedCount by remember { mutableIntStateOf(5) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${activeTrack.title.uppercase()} • ${selectedSubject.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ForestEmerald
                    )
                    Text(
                        text = if (showTestsTab) "Practice Tests" else "Quest Path",
                        style = MaterialTheme.typography.displaySmall
                    )
                }
                OnlineStatusBadge(isOnline = isOnline)
            }
        }

        // Toggle: Quest Path vs Practice Tests
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ParchmentCard, RoundedCornerShape(18.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showTestsTab = false }
                        .testTag("learn_mode_quests"),
                    shape = RoundedCornerShape(14.dp),
                    color = if (!showTestsTab) ForestEmerald else Color.Transparent
                ) {
                    Text(
                        text = "Playable Quests",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (!showTestsTab) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showTestsTab = true }
                        .testTag("learn_mode_tests"),
                    shape = RoundedCornerShape(14.dp),
                    color = if (showTestsTab) ForestEmerald else Color.Transparent
                ) {
                    Text(
                        text = "Practice Tests",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (showTestsTab) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }

        if (!aiError.isNullOrBlank()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = aiError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(onClick = onClearAiError) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }

        if (!showTestsTab) {
            // Duolingo-style Winding Quest Path
            itemsIndexed(lessons) { idx, lesson ->
                val isCompleted = progressMap[lesson.id]?.completed == true
                val isCurrent = !isCompleted && (idx == 0 || progressMap[lessons[idx - 1].id]?.completed == true)
                val xOffset = when (idx % 4) {
                    0 -> (-28).dp
                    1 -> 26.dp
                    2 -> 42.dp
                    else -> (-18).dp
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .offset(x = xOffset)
                            .fillMaxWidth(0.84f)
                            .clip(RoundedCornerShape(22.dp))
                            .clickable { onStartLesson(lesson) }
                            .testTag("quest_node_${idx}"),
                        shape = RoundedCornerShape(22.dp),
                        color = if (isCurrent) MintSurface else ParchmentCard,
                        border = BorderStroke(
                            width = if (isCurrent) 2.dp else 1.dp,
                            color = if (isCurrent) ForestEmerald else ParchmentBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = when {
                                    isCompleted -> ForestEmerald
                                    isCurrent -> WarmAmber
                                    else -> ForestEmeraldDark
                                },
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = when {
                                            isCompleted -> Icons.Filled.Check
                                            isCurrent -> Icons.Filled.PlayArrow
                                            else -> Icons.Filled.Star
                                        },
                                        contentDescription = lesson.title,
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (isCurrent) "UP NEXT • +${lesson.xpReward} XP" else if (isCompleted) "COMPLETED" else "+${lesson.xpReward} XP",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isCurrent) WarmAmber else ForestEmerald
                                    )
                                    if (lesson.savedOffline) {
                                        Icon(
                                            Icons.Filled.CloudDone,
                                            contentDescription = "Offline ready",
                                            tint = ForestEmerald,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = lesson.title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = lesson.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (lesson.concept3dId != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = MaterialTheme.colorScheme.background,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .clickable { onOpen3DLab(lesson.concept3dId) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Filled.ViewInAr, null, tint = ForestEmerald, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Open 3D Visual", style = MaterialTheme.typography.labelSmall, color = ForestEmeraldDark)
                                        }
                                    }
                                }
                            }

                            if (isCurrent) {
                                OwlMascotAvatar(size = 40.dp, mood = OwlMood.HAPPY, animateBob = true)
                            }
                        }
                    }
                }
            }

            // Custom AI Quest Generator Card
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MintSurface,
                    border = BorderStroke(1.dp, MintBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.AutoAwesome, null, tint = ForestEmerald)
                            Text("Create Custom AI Quest", style = MaterialTheme.typography.titleMedium, color = ForestEmeraldDark)
                        }
                        Text(
                            text = "Type any topic from ${activeTrack.title} ($selectedSubject) — Owl builds a playable quiz lesson and saves it offline.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = customTopic,
                            onValueChange = { customTopic = it },
                            label = { Text("Topic (e.g. Quadratic Equations, Preamble)") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_lesson_topic_input"),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Button(
                            onClick = { onGenerateAiLesson(customTopic) },
                            enabled = isOnline && !isAiLoading && customTopic.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("generate_ai_lesson_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestEmerald),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (isAiLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Building Quest...")
                            } else {
                                Text(if (isOnline) "Build & Play Quest" else "Connect online to generate new AI quests")
                            }
                        }
                    }
                }
            }
        } else {
            // Practice Test Builder Card
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MintSurface,
                    border = BorderStroke(1.dp, MintBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Quiz, null, tint = ForestEmerald)
                            Text("Custom Practice Test Maker", style = MaterialTheme.typography.titleMedium, color = ForestEmeraldDark)
                        }
                        OutlinedTextField(
                            value = customTopic,
                            onValueChange = { customTopic = it },
                            label = { Text("Focus topic (optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Foundation", "Standard", "Exam Hard").forEach { diff ->
                                FilterChip(
                                    selected = selectedDifficulty == diff,
                                    onClick = { selectedDifficulty = diff },
                                    label = { Text(diff) }
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(5, 8, 10).forEach { count ->
                                FilterChip(
                                    selected = selectedCount == count,
                                    onClick = { selectedCount = count },
                                    label = { Text("$count Qs") }
                                )
                            }
                        }
                        Button(
                            onClick = { onGenerateAiTest(customTopic, selectedDifficulty, selectedCount) },
                            enabled = isOnline && !isAiLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("generate_practice_test_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestEmerald),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (isAiLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Crafting Practice Test...")
                            } else {
                                Text(if (isOnline) "Generate & Start Test" else "Offline: use saved tests below")
                            }
                        }
                    }
                }
            }

            itemsIndexed(tests) { idx, test ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onStartTest(test) }
                        .testTag("practice_test_card_$idx"),
                    shape = RoundedCornerShape(20.dp),
                    color = ParchmentCard,
                    border = BorderStroke(1.dp, ParchmentBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(shape = RoundedCornerShape(50), color = WarmAmberSoft) {
                                    Text(
                                        text = test.difficulty,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WarmAmber,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                if (test.savedOffline) {
                                    Icon(Icons.Filled.CloudDone, "Offline", tint = ForestEmerald, modifier = Modifier.size(15.dp))
                                }
                            }
                            Text(text = test.title, style = MaterialTheme.typography.titleMedium)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Timer, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${test.durationMinutes} min", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("${test.questions.size} questions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Surface(shape = CircleShape, color = ForestEmerald, modifier = Modifier.size(44.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.PlayArrow, "Start test", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
