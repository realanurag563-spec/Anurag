package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.ViewInAr
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
import androidx.compose.ui.unit.dp
import com.example.core.parseRichBoldText
import com.example.model.QuestLesson
import com.example.ui.components.OwlMascotAvatar
import com.example.ui.components.OwlMood
import com.example.ui.theme.CoralHeart
import com.example.ui.theme.CoralHeartSoft
import com.example.ui.theme.ForestEmerald
import com.example.ui.theme.ForestEmeraldDark
import com.example.ui.theme.MintBorder
import com.example.ui.theme.MintSurface
import com.example.ui.theme.ParchmentBorder
import com.example.ui.theme.ParchmentCard
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberSoft

@Composable
fun LessonPlayerScreen(
    lesson: QuestLesson,
    initialHearts: Int,
    onExit: () -> Unit,
    onOpen3DConcept: (String?) -> Unit,
    onLessonFinished: (correctCount: Int, totalQuestions: Int, heartsLeft: Int) -> Unit
) {
    BackHandler { onExit() }

    val steps = lesson.steps
    var stepIndex by remember { mutableIntStateOf(0) }
    var showQuizPhase by remember { mutableStateOf(false) }
    var selectedOptionIndex by remember { mutableIntStateOf(-1) }
    var answerChecked by remember { mutableStateOf(false) }
    var showHint by remember { mutableStateOf(false) }
    var correctCount by remember { mutableIntStateOf(0) }
    var hearts by remember { mutableIntStateOf(initialHearts.coerceIn(1, 5)) }

    val currentStep = steps.getOrNull(stepIndex)
    val totalSteps = steps.size.coerceAtLeast(1)
    val totalQuestions = steps.count { it.checkpointQuestion != null }.coerceAtLeast(1)
    val question = currentStep?.checkpointQuestion

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Header matching Screenshot 3 (X | Title | step counter + hearts)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = CircleShape,
                    color = ParchmentCard,
                    modifier = Modifier.size(44.dp)
                ) {
                    IconButton(
                        onClick = onExit,
                        modifier = Modifier.testTag("lesson_close_button")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close lesson")
                    }
                }

                Text(
                    text = if (showQuizPhase) "Checkpoint quiz" else lesson.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Favorite, "Hearts", tint = CoralHeart, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("$hearts", style = MaterialTheme.typography.labelLarge, color = CoralHeart)
                    }
                    Text(
                        text = "${stepIndex + 1}/$totalSteps",
                        style = MaterialTheme.typography.labelLarge,
                        color = ForestEmerald
                    )
                }
            }

            // Progress bar
            val progress = ((stepIndex + if (showQuizPhase) 1f else 0.5f) / totalSteps.toFloat()).coerceIn(0.1f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = ForestEmerald,
                trackColor = ParchmentCard
            )

            // Owl's quick check / teaching banner matching Screenshot 3
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MintSurface,
                border = BorderStroke(1.dp, MintBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OwlMascotAvatar(
                        size = 44.dp,
                        mood = if (answerChecked && selectedOptionIndex == question?.correctIndex) OwlMood.CELEBRATING else OwlMood.HAPPY
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (showQuizPhase) "Owl's quick check" else "Owl teaches • ${currentStep?.title ?: ""}",
                            style = MaterialTheme.typography.titleSmall,
                            color = ForestEmeraldDark
                        )
                        Text(
                            text = if (showQuizPhase) "Pick the best answer." else "Read the core idea, then test it!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (!showQuizPhase && currentStep != null) {
                // Teaching Slide Card
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = ParchmentCard,
                    border = BorderStroke(1.dp, ParchmentBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "STEP ${stepIndex + 1} CONCEPT",
                            style = MaterialTheme.typography.labelSmall,
                            color = ForestEmerald
                        )
                        Text(
                            text = currentStep.title,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = parseRichBoldText(currentStep.owlPrompt),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (currentStep.keyFormulaOrFact.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = WarmAmberSoft,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Filled.Lightbulb, null, tint = WarmAmber, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = currentStep.keyFormulaOrFact,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color(0xFF7A3D03)
                                    )
                                }
                            }
                        }

                        if (lesson.concept3dId != null) {
                            OutlinedButton(
                                onClick = { onOpen3DConcept(lesson.concept3dId) },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, MintBorder)
                            ) {
                                Icon(Icons.Filled.ViewInAr, null, tint = ForestEmerald, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Visualize Concept in 3D", color = ForestEmeraldDark)
                            }
                        }
                    }
                }
            } else if (question != null) {
                // Checkpoint Quiz Card matching Screenshot 3
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "QUESTION ${stepIndex + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ForestEmerald
                    )
                    Text(
                        text = question.prompt,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    val letters = listOf("A", "B", "C", "D")
                    question.options.forEachIndexed { idx, opt ->
                        val isSelected = selectedOptionIndex == idx
                        val isCorrectOpt = idx == question.correctIndex
                        val cardBg = when {
                            answerChecked && isCorrectOpt -> MintSurface
                            answerChecked && isSelected && !isCorrectOpt -> CoralHeartSoft
                            isSelected -> MintSurface
                            else -> ParchmentCard
                        }
                        val cardBorder = when {
                            answerChecked && isCorrectOpt -> ForestEmerald
                            answerChecked && isSelected && !isCorrectOpt -> CoralHeart
                            isSelected -> ForestEmerald
                            else -> ParchmentBorder
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .clickable(enabled = !answerChecked) { selectedOptionIndex = idx }
                                .testTag("quiz_option_$idx"),
                            shape = RoundedCornerShape(18.dp),
                            color = cardBg,
                            border = BorderStroke(if (isSelected || (answerChecked && isCorrectOpt)) 1.5.dp else 1.dp, cardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
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
                                            text = letters.getOrElse(idx) { "${idx + 1}" },
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = opt,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    if (!answerChecked) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { showHint = !showHint },
                                shape = RoundedCornerShape(50)
                            ) {
                                Icon(Icons.Filled.Lightbulb, null, tint = WarmAmber, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (showHint) "Hide Hint" else "Owl Hint")
                            }
                        }
                    }

                    AnimatedVisibility(visible = showHint && !answerChecked) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = WarmAmberSoft,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Hint: ${question.hint}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF7A3D03),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    AnimatedVisibility(visible = answerChecked) {
                        val isRight = selectedOptionIndex == question.correctIndex
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isRight) MintSurface else CoralHeartSoft,
                            border = BorderStroke(1.dp, if (isRight) MintBorder else CoralHeart),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = if (isRight) "Great job! +XP combo" else "Good try — let's review",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (isRight) ForestEmeraldDark else CoralHeart
                                )
                                Text(
                                    text = question.explanation,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom CTA matching Screenshot 3
        Spacer(modifier = Modifier.height(12.dp))
        val isLastStep = stepIndex >= steps.lastIndex
        Button(
            onClick = {
                if (!showQuizPhase) {
                    if (currentStep?.checkpointQuestion != null) {
                        showQuizPhase = true
                        selectedOptionIndex = -1
                        answerChecked = false
                        showHint = false
                    } else if (!isLastStep) {
                        stepIndex++
                    } else {
                        onLessonFinished(correctCount, totalQuestions, hearts)
                    }
                } else {
                    if (!answerChecked) {
                        if (selectedOptionIndex >= 0 && question != null) {
                            answerChecked = true
                            if (selectedOptionIndex == question.correctIndex) {
                                correctCount++
                            } else {
                                hearts = (hearts - 1).coerceAtLeast(1)
                            }
                        }
                    } else {
                        if (!isLastStep) {
                            stepIndex++
                            showQuizPhase = false
                            selectedOptionIndex = -1
                            answerChecked = false
                            showHint = false
                        } else {
                            onLessonFinished(correctCount, totalQuestions, hearts)
                        }
                    }
                }
            },
            enabled = !showQuizPhase || selectedOptionIndex >= 0,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("lesson_primary_cta"),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ForestEmerald,
                disabledContainerColor = MintBorder
            )
        ) {
            val label = when {
                !showQuizPhase && question != null -> "Quick Check ->"
                !showQuizPhase && isLastStep -> "Finish quest ->"
                !showQuizPhase -> "Next step ->"
                !answerChecked -> "Check Answer"
                isLastStep -> "Finish quest ->"
                else -> "Continue ->"
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
