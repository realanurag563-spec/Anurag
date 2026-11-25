package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.curriculum.CurriculumCatalog
import com.example.model.LearningTrack
import com.example.model.LessonProgressDoc
import com.example.model.QuestLesson
import com.example.model.TrackCategory
import com.example.model.UserProgressProfile
import com.example.ui.components.OnlineStatusBadge
import com.example.ui.components.OwlMascotAvatar
import com.example.ui.components.OwlMood
import com.example.ui.components.StatSummaryCard
import com.example.ui.theme.ForestEmerald
import com.example.ui.theme.ForestEmeraldDark
import com.example.ui.theme.MintBorder
import com.example.ui.theme.MintSurface
import com.example.ui.theme.ParchmentBorder
import com.example.ui.theme.ParchmentCard
import com.example.ui.theme.WarmAmber
import java.util.Calendar

@Composable
fun HomeScreen(
    profile: UserProgressProfile,
    selectedCategory: TrackCategory,
    selectedTrackId: String,
    selectedSubject: String,
    lessons: List<QuestLesson>,
    progressMap: Map<String, LessonProgressDoc>,
    isOnline: Boolean,
    onSelectCategory: (TrackCategory) -> Unit,
    onSelectTrack: (LearningTrack) -> Unit,
    onSelectSubject: (String) -> Unit,
    onStartLesson: (QuestLesson) -> Unit,
    onOpen3DLab: (String?) -> Unit,
    onOpenPracticeTests: () -> Unit,
    onSaveOffline: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tracksInCategory = CurriculumCatalog.tracks.filter { it.category == selectedCategory }
    val activeTrack = CurriculumCatalog.tracks.firstOrNull { it.id == selectedTrackId }
        ?: tracksInCategory.first()

    val nextLesson = lessons.firstOrNull { progressMap[it.id]?.completed != true } ?: lessons.firstOrNull()
    val completedCountInPath = lessons.count { progressMap[it.id]?.completed == true }
    val greeting = greetingTimeLabel()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header matching Screenshot 1
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "$greeting, ${profile.displayName.substringBefore(" ").uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ForestEmerald
                        )
                        OnlineStatusBadge(isOnline = isOnline)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Ready for your next quest?",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OwlMascotAvatar(size = 50.dp, mood = OwlMood.HAPPY, animateBob = true)
                    IconButton(
                        onClick = onSignOut,
                        modifier = Modifier.testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign out",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Stat Summary Card
        item {
            StatSummaryCard(
                streakDays = profile.streakDays,
                totalXp = profile.totalXp,
                gems = profile.gems
            )
        }

        // Dual Mode Switcher: School (Classes 1-12) vs Gov exams
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TrackCategory.entries.forEach { cat ->
                        val isSelected = cat == selectedCategory
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { onSelectCategory(cat) }
                                .testTag("category_tab_${cat.name.lowercase()}"),
                            shape = RoundedCornerShape(18.dp),
                            color = if (isSelected) ForestEmerald else ParchmentCard,
                            border = BorderStroke(1.dp, if (isSelected) ForestEmerald else ParchmentBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                Text(
                                    text = cat.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = cat.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) Color(0xFFE0F2E7) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Class 1-12 or Exam track chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tracksInCategory.forEach { track ->
                        val selected = track.id == activeTrack.id
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectTrack(track) },
                            label = {
                                Text(
                                    text = track.title,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MintSurface,
                                selectedLabelColor = ForestEmeraldDark,
                                containerColor = ParchmentCard
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = ParchmentBorder,
                                selectedBorderColor = ForestEmerald
                            )
                        )
                    }
                }

                // Subject chips for the selected track
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    activeTrack.subjects.forEach { subject ->
                        val selected = subject.equals(selectedSubject, ignoreCase = true)
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectSubject(subject) },
                            label = { Text(subject) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ForestEmerald,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    }
                }
            }
        }

        // Continue your path (Hero card matching Screenshot 1)
        if (nextLesson != null) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Continue your path",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .clickable { onStartLesson(nextLesson) }
                            .testTag("continue_quest_card"),
                        shape = RoundedCornerShape(24.dp),
                        color = MintSurface,
                        border = BorderStroke(1.5.dp, MintBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.background
                                ) {
                                    Text(
                                        text = "${activeTrack.shortTitle} • ${nextLesson.subject}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = ForestEmeraldDark,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                                Text(
                                    text = "${nextLesson.durationMinutes} min",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = nextLesson.title,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = nextLesson.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = ForestEmerald,
                                    modifier = Modifier.size(54.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.PlayArrow,
                                            contentDescription = "Start quest",
                                            tint = Color.White,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }
                            }

                            val progressFraction = if (lessons.isEmpty()) 0f
                            else (completedCountInPath.toFloat() / lessons.size.toFloat()).coerceIn(0.15f, 1f)
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = ForestEmerald,
                                trackColor = Color.White
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$completedCountInPath of ${lessons.size} quests complete",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = MaterialTheme.colorScheme.background,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .clickable { onOpen3DLab(nextLesson.concept3dId) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Filled.ViewInAr, null, tint = ForestEmerald, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("3D View", style = MaterialTheme.typography.labelMedium, color = ForestEmeraldDark)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Action Row: Practice Test & Save Offline
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onOpenPracticeTests() }
                        .testTag("home_practice_test_button"),
                    shape = RoundedCornerShape(18.dp),
                    color = ParchmentCard,
                    border = BorderStroke(1.dp, ParchmentBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.Quiz, null, tint = WarmAmber, modifier = Modifier.size(22.dp))
                        Column {
                            Text("Practice Test", style = MaterialTheme.typography.titleSmall)
                            Text("Timed & AI drills", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onSaveOffline() }
                        .testTag("home_save_offline_button"),
                    shape = RoundedCornerShape(18.dp),
                    color = ParchmentCard,
                    border = BorderStroke(1.dp, ParchmentBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.DownloadForOffline, null, tint = ForestEmerald, modifier = Modifier.size(22.dp))
                        Column {
                            Text("Offline Pack", style = MaterialTheme.typography.titleSmall)
                            Text("Save $selectedSubject", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        // Your learning path card (matching Screenshot 1 timeline)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your learning path",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = ParchmentCard,
                    border = BorderStroke(1.dp, ParchmentBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        lessons.forEachIndexed { index, lesson ->
                            val isCompleted = progressMap[lesson.id]?.completed == true
                            val isUpNext = !isCompleted && (index == 0 || progressMap[lessons[index - 1].id]?.completed == true)
                            val showLine = index < lessons.lastIndex

                            PathTimelineRow(
                                title = lesson.title,
                                statusText = when {
                                    isCompleted -> "Completed • +${progressMap[lesson.id]?.xpEarned ?: lesson.xpReward} XP"
                                    isUpNext -> "Up next • ${lesson.durationMinutes} min"
                                    else -> "Available • Tap to preview"
                                },
                                isCompleted = isCompleted,
                                isUpNext = isUpNext,
                                showConnector = showLine,
                                savedOffline = lesson.savedOffline,
                                onClick = { onStartLesson(lesson) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PathTimelineRow(
    title: String,
    statusText: String,
    isCompleted: Boolean,
    isUpNext: Boolean,
    showConnector: Boolean,
    savedOffline: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(42.dp)
        ) {
            val circleColor = when {
                isCompleted -> ForestEmerald
                isUpNext -> WarmAmber
                else -> ForestEmeraldDark
            }
            Surface(
                shape = CircleShape,
                color = circleColor,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when {
                            isCompleted -> Icons.Filled.Check
                            isUpNext -> Icons.Filled.Star
                            else -> Icons.Filled.Lock
                        },
                        contentDescription = statusText,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            if (showConnector) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(30.dp)
                        .background(WarmAmber.copy(alpha = 0.65f))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (savedOffline) {
                    Icon(
                        imageVector = Icons.Filled.CloudDone,
                        contentDescription = "Available offline",
                        tint = ForestEmerald,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun greetingTimeLabel(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "GOOD MORNING"
        hour < 17 -> "GOOD AFTERNOON"
        else -> "GOOD EVENING"
    }
}
