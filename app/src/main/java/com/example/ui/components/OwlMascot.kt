package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.MilestoneReward
import com.example.ui.theme.ForestEmerald
import com.example.ui.theme.ForestEmeraldDark
import com.example.ui.theme.MintBorder
import com.example.ui.theme.MintSurface
import com.example.ui.theme.ParchmentBorder
import com.example.ui.theme.ParchmentCard
import com.example.ui.theme.SkyGem
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberSoft

enum class OwlMood {
    HAPPY,
    THINKING,
    CELEBRATING
}

@Composable
fun OwlMascotAvatar(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    mood: OwlMood = OwlMood.HAPPY,
    animateBob: Boolean = false
) {
    val transition = rememberInfiniteTransition(label = "owl_bob")
    val bobOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (animateBob) -4f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer { translationY = bobOffset },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            // Soft shadow base
            drawOval(
                color = Color(0x181C241E),
                topLeft = Offset(w * 0.14f, h * 0.86f),
                size = Size(w * 0.72f, h * 0.12f)
            )

            // Ear tufts
            val leftEar = Path().apply {
                moveTo(w * 0.14f, h * 0.34f)
                lineTo(w * 0.16f, h * 0.08f)
                lineTo(w * 0.36f, h * 0.22f)
                close()
            }
            val rightEar = Path().apply {
                moveTo(w * 0.86f, h * 0.34f)
                lineTo(w * 0.84f, h * 0.08f)
                lineTo(w * 0.64f, h * 0.22f)
                close()
            }
            drawPath(leftEar, ForestEmeraldDark)
            drawPath(rightEar, ForestEmeraldDark)

            // Rounded owl body/head
            drawRoundRect(
                color = ForestEmerald,
                topLeft = Offset(w * 0.1f, h * 0.16f),
                size = Size(w * 0.8f, h * 0.74f),
                cornerRadius = CornerRadius(w * 0.38f, h * 0.38f)
            )

            // Mint eye masks
            drawCircle(
                color = Color(0xFF43A467),
                radius = w * 0.20f,
                center = Offset(w * 0.34f, h * 0.48f)
            )
            drawCircle(
                color = Color(0xFF43A467),
                radius = w * 0.20f,
                center = Offset(w * 0.66f, h * 0.48f)
            )

            // White eye globes
            drawCircle(
                color = Color.White,
                radius = w * 0.135f,
                center = Offset(w * 0.34f, h * 0.48f)
            )
            drawCircle(
                color = Color.White,
                radius = w * 0.135f,
                center = Offset(w * 0.66f, h * 0.48f)
            )

            // Pupils (shifted subtly when thinking)
            val pupilDx = if (mood == OwlMood.THINKING) w * 0.025f else 0f
            val pupilDy = if (mood == OwlMood.THINKING) -h * 0.02f else 0f
            drawCircle(
                color = Color(0xFF142419),
                radius = w * 0.068f,
                center = Offset(w * 0.34f + pupilDx, h * 0.48f + pupilDy)
            )
            drawCircle(
                color = Color(0xFF142419),
                radius = w * 0.068f,
                center = Offset(w * 0.66f + pupilDx, h * 0.48f + pupilDy)
            )

            // Sparkle in eyes
            drawCircle(
                color = Color.White,
                radius = w * 0.024f,
                center = Offset(w * 0.32f + pupilDx, h * 0.45f + pupilDy)
            )
            drawCircle(
                color = Color.White,
                radius = w * 0.024f,
                center = Offset(w * 0.64f + pupilDx, h * 0.45f + pupilDy)
            )

            // Warm orange diamond beak
            val beak = Path().apply {
                moveTo(w * 0.50f, h * 0.52f)
                lineTo(w * 0.57f, h * 0.61f)
                lineTo(w * 0.50f, h * 0.70f)
                lineTo(w * 0.43f, h * 0.61f)
                close()
            }
            drawPath(beak, WarmAmber)

            // Mint leaf crest when celebrating
            if (mood == OwlMood.CELEBRATING) {
                drawCircle(
                    color = MintSurface,
                    radius = w * 0.06f,
                    center = Offset(w * 0.50f, h * 0.13f)
                )
            }
        }
    }
}

@Composable
fun OnlineStatusBadge(
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isOnline) MintSurface else WarmAmberSoft
    val dotColor = if (isOnline) ForestEmerald else WarmAmber
    val textColor = if (isOnline) ForestEmeraldDark else Color(0xFF8C4605)
    val label = if (isOnline) "Ready" else "Offline"

    Surface(
        modifier = modifier.testTag("connectivity_status_badge"),
        shape = RoundedCornerShape(50),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(dotColor, CircleShape)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = textColor
            )
        }
    }
}

@Composable
fun StatSummaryCard(
    streakDays: Long,
    totalXp: Long,
    gems: Long,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = ParchmentCard,
        border = BorderStroke(1.dp, ParchmentBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItemColumn(
                icon = Icons.Filled.LocalFireDepartment,
                iconTint = WarmAmber,
                value = streakDays.toString(),
                label = "day streak"
            )
            StatItemColumn(
                icon = Icons.Filled.Bolt,
                iconTint = ForestEmerald,
                value = totalXp.toString(),
                label = "total XP"
            )
            StatItemColumn(
                icon = Icons.Filled.Diamond,
                iconTint = SkyGem,
                value = gems.toString(),
                label = "gems"
            )
        }
    }
}

@Composable
private fun StatItemColumn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MilestoneCelebrationDialog(
    title: String,
    subtitle: String,
    xpGained: Int,
    gemsGained: Int,
    milestone: MilestoneReward?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OwlMascotAvatar(size = 72.dp, mood = OwlMood.CELEBRATING, animateBob = true)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(shape = RoundedCornerShape(50), color = MintSurface) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Bolt, null, tint = ForestEmerald, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+$xpGained XP", style = MaterialTheme.typography.labelLarge, color = ForestEmeraldDark)
                        }
                    }
                    Surface(shape = RoundedCornerShape(50), color = WarmAmberSoft) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Diamond, null, tint = SkyGem, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+$gemsGained gems", style = MaterialTheme.typography.labelLarge, color = ForestEmeraldDark)
                        }
                    }
                }
                if (milestone != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MintSurface,
                        border = BorderStroke(1.dp, MintBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Filled.EmojiEvents,
                                contentDescription = "Milestone unlocked",
                                tint = WarmAmber,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = "MILESTONE UNLOCKED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ForestEmerald
                                )
                                Text(
                                    text = milestone.title,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("milestone_continue_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ForestEmerald),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Continue Quest")
            }
        }
    )
}
