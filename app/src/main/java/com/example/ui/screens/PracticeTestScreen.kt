package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.PracticeTestPack
import com.example.ui.components.OwlMascotAvatar
import com.example.ui.components.OwlMood
import com.example.ui.theme.CoralHeart
import com.example.ui.theme.CoralHeartSoft
import com.example.ui.theme.ForestEmerald
import com.example.ui.theme.ForestEmeraldDark
import com.example.ui.theme.MintBorder
import com.example.ui.theme.MintSurface
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.ParchmentBorder
import com.example.ui.theme.ParchmentCard
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberSoft
import kotlinx.coroutines.delay

@Composable
fun PracticeTestScreen(
    test: PracticeTestPack,
    onExit: () -> Unit,
    onTestSubmitted: (score: Int, durationSeconds: Int) -> Unit
) {
    BackHandler { onExit() }

    val totalSecondsAllowed = test.durationMinutes * 60
    var secondsLeft by remember { mutableIntStateOf(totalSecondsAllowed) }
    var currentIndex by remember { mutableIntStateOf(0) }
    val selectedAnswers = remember { mutableStateMapOf<Int, Int>() }
    var submitted by remember { mutableStateOf(false) }

    LaunchedEffect(submitted) {
        while (!submitted && secondsLeft > 0) {
            delay(1000L)
            secondsLeft--
            if (secondsLeft == 0) {
                submitted = true
                val score = test.questions.indices.count { selectedAnswers[it] == test.questions[it].correctIndex }
                onTestSubmitted(score, totalSecondsAllowed)
            }
        }
    }

    val currentQ = test.questions.getOrNull(currentIndex)
    val total = test.questions.size.coerceAtLeast(1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(shape = CircleShape, color = ParchmentCard, modifier = Modifier.size(44.dp)) {
                    IconButton(onClick = onExit) {
                        Icon(Icons.Filled.Close, contentDescription = "Exit test")
                    }
                }
                Text(
                    text = test.title,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
                    maxLines = 1
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (secondsLeft < 60) CoralHeartSoft else MintSurface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Timer,
                            null,
                            tint = if (secondsLeft < 60) CoralHeart else ForestEmeraldDark,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val mins = secondsLeft / 60
                        val secs = secondsLeft % 60
                        Text(
                            text = "%02d:%02d".format(mins, secs),
                            style = MaterialTheme.typography.labelLarge.copy(fontFamily = MonoFontFamily),
                            color = if (secondsLeft < 60) CoralHeart else ForestEmeraldDark
                        )
                    }
                }
            }

            // Question palette pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                test.questions.forEachIndexed { idx, q ->
                    val answered = selectedAnswers.containsKey(idx)
                    val isCurrent = idx == currentIndex
                    val isCorrect = submitted && selectedAnswers[idx] == q.correctIndex
                    val bg = when {
                        submitted && isCorrect -> ForestEmerald
                        submitted && answered -> CoralHeart
                        isCurrent -> WarmAmber
                        answered -> MintSurface
                        else -> ParchmentCard
                    }
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { currentIndex = idx },
                        shape = CircleShape,
                        color = bg
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${idx + 1}",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isCurrent || submitted) Color.White else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }

            LinearProgressIndicator(
                progress = { (selectedAnswers.size.toFloat() / total.toFloat()).coerceIn(0.05f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(50)),
                color = ForestEmerald,
                trackColor = ParchmentCard
            )

            if (submitted) {
                val finalScore = test.questions.indices.count { selectedAnswers[it] == test.questions[it].correctIndex }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MintSurface,
                    border = BorderStroke(1.dp, MintBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OwlMascotAvatar(size = 48.dp, mood = OwlMood.CELEBRATING)
                        Column {
                            Text("Test Complete: $finalScore / $total", style = MaterialTheme.typography.titleLarge, color = ForestEmeraldDark)
                            Text("Tap question numbers above to review Owl's explanations.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            if (currentQ != null) {
                Text(
                    text = "QUESTION ${currentIndex + 1} OF $total",
                    style = MaterialTheme.typography.labelSmall,
                    color = ForestEmerald
                )
                Text(
                    text = currentQ.prompt,
                    style = MaterialTheme.typography.headlineMedium
                )
                val letters = listOf("A", "B", "C", "D")
                currentQ.options.forEachIndexed { optIdx, opt ->
                    val isSelected = selectedAnswers[currentIndex] == optIdx
                    val isRightOpt = optIdx == currentQ.correctIndex
                    val bg = when {
                        submitted && isRightOpt -> MintSurface
                        submitted && isSelected && !isRightOpt -> CoralHeartSoft
                        isSelected -> MintSurface
                        else -> ParchmentCard
                    }
                    val border = when {
                        submitted && isRightOpt -> ForestEmerald
                        submitted && isSelected && !isRightOpt -> CoralHeart
                        isSelected -> ForestEmerald
                        else -> ParchmentBorder
                    }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(enabled = !submitted) { selectedAnswers[currentIndex] = optIdx },
                        shape = RoundedCornerShape(18.dp),
                        color = bg,
                        border = BorderStroke(1.dp, border)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) ForestEmerald else MaterialTheme.colorScheme.background,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = letters.getOrElse(optIdx) { "${optIdx + 1}" },
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                            Text(opt, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        }
                    }
                }

                if (submitted) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = WarmAmberSoft,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Filled.CheckCircle, null, tint = ForestEmerald, modifier = Modifier.size(18.dp))
                            Text(currentQ.explanation, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (currentIndex > 0) {
                OutlinedButton(
                    onClick = { currentIndex-- },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Previous")
                }
            }
            if (!submitted) {
                if (currentIndex < test.questions.lastIndex) {
                    Button(
                        onClick = { currentIndex++ },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestEmerald)
                    ) {
                        Text("Next")
                    }
                } else {
                    Button(
                        onClick = {
                            submitted = true
                            val score = test.questions.indices.count { selectedAnswers[it] == test.questions[it].correctIndex }
                            val elapsed = (totalSecondsAllowed - secondsLeft).coerceAtLeast(1)
                            onTestSubmitted(score, elapsed)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("submit_practice_test_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestEmerald)
                    ) {
                        Text("Submit Test", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Button(
                    onClick = onExit,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestEmerald)
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
