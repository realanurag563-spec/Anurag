package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Concept3DScene
import com.example.model.PrimitiveShape3D
import com.example.model.SceneNode3D
import com.example.model.UiState
import com.example.ui.components.OnlineStatusBadge
import com.example.ui.components.OwlMascotAvatar
import com.example.ui.components.OwlMood
import com.example.ui.theme.ForestEmerald
import com.example.ui.theme.ForestEmeraldDark
import com.example.ui.theme.MintBorder
import com.example.ui.theme.MintSurface
import com.example.ui.theme.ParchmentBorder
import com.example.ui.theme.ParchmentCard
import com.example.ui.theme.ParchmentDeep
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmAmberSoft
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

private data class Vec3(val x: Float, val y: Float, val z: Float)

private data class ProjectedNode(
    val node: SceneNode3D,
    val screenX: Float,
    val screenY: Float,
    val depth: Float,
    val scale: Float,
    val rotatedPos: Vec3,
    val rotatedEnd: Vec3?
)

@Composable
fun Lab3DScreen(
    scenes: List<Concept3DScene>,
    selectedSceneId: String,
    selectedNode: SceneNode3D?,
    generationState: UiState<Concept3DScene>?,
    trackTitle: String,
    subject: String,
    isOnline: Boolean,
    onSelectScene: (String) -> Unit,
    onSelectNode: (SceneNode3D) -> Unit,
    onGenerateCustom3D: (String) -> Unit,
    onClearGenerationError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeScene = scenes.firstOrNull { it.id == selectedSceneId } ?: scenes.first()
    var yawDeg by remember(activeScene.id) { mutableFloatStateOf(24f) }
    var pitchDeg by remember(activeScene.id) { mutableFloatStateOf(-18f) }
    var zoomFactor by remember(activeScene.id) { mutableFloatStateOf(1.0f) }
    var explodeFactor by remember(activeScene.id) { mutableFloatStateOf(0f) }
    var autoOrbit by remember(activeScene.id) { mutableStateOf(true) }
    var customTopic by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "orbit_3d")
    val animClock by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_phase"
    )

    val effectiveYaw = if (autoOrbit) yawDeg + (animClock * 180f / PI.toFloat()) * 0.25f else yawDeg
    val isGenerating = generationState is UiState.Loading

    // Keep last projected node screen positions for tap hit-testing
    var lastHitTargets by remember { mutableStateOf<List<Pair<SceneNode3D, Offset>>>(emptyList()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
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
                            text = "INTERACTIVE 3D CONCEPT LAB",
                            style = MaterialTheme.typography.labelSmall,
                            color = ForestEmerald
                        )
                        OnlineStatusBadge(isOnline = isOnline)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = activeScene.title,
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        text = "${activeScene.subject} • ${activeScene.levelLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OwlMascotAvatar(
                    size = 48.dp,
                    mood = if (isGenerating) OwlMood.THINKING else OwlMood.HAPPY
                )
            }
        }

        // Scene Selector Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                scenes.forEach { scene ->
                    val isSelected = scene.id == activeScene.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectScene(scene.id) },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(scene.title)
                                if (scene.savedOffline) {
                                    Icon(
                                        Icons.Filled.CloudDone,
                                        contentDescription = "Offline ready",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForestEmerald,
                            selectedLabelColor = Color.White,
                            containerColor = ParchmentCard
                        )
                    )
                }
            }
        }

        // Interactive 3D Viewport Canvas
        item {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = ParchmentCard,
                border = BorderStroke(1.5.dp, ParchmentBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(310.dp)
                            .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF1F3026), Color(0xFF101914))
                                )
                            )
                            .testTag("lab3d_canvas")
                            .pointerInput(activeScene.id) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    autoOrbit = false
                                    yawDeg += dragAmount.x * 0.45f
                                    pitchDeg = (pitchDeg + dragAmount.y * 0.45f).coerceIn(-80f, 80f)
                                }
                            }
                            .pointerInput(activeScene.id, lastHitTargets) {
                                detectTapGestures { tapOffset ->
                                    val closest = lastHitTargets
                                        .minByOrNull { hypot(it.second.x - tapOffset.x, it.second.y - tapOffset.y) }
                                    if (closest != null &&
                                        hypot(closest.second.x - tapOffset.x, closest.second.y - tapOffset.y) < 56f
                                    ) {
                                        onSelectNode(closest.first)
                                    }
                                }
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val cx = w / 2f
                            val cy = h / 2f
                            val baseScale = min(w, h) * 0.175f * zoomFactor
                            val cameraDist = 7.5f

                            // Subtle 3D floor grid
                            drawPerspectiveFloorGrid(
                                center = Offset(cx, cy + baseScale * 1.45f),
                                scale = baseScale,
                                yawDeg = effectiveYaw,
                                pitchDeg = pitchDeg
                            )

                            val hitTargets = mutableListOf<Pair<SceneNode3D, Offset>>()
                            val projected = activeScene.nodes.map { node ->
                                val orbitAngle = if (node.orbitSpeed != 0f) animClock * node.orbitSpeed else 0f
                                val basePos = if (node.orbitRadius > 0f && node.shape == PrimitiveShape3D.SPHERE) {
                                    val ox = cos(orbitAngle) * node.orbitRadius
                                    val oz = sin(orbitAngle) * node.orbitRadius
                                    rotateAroundX(Vec3(ox, node.y, oz), node.orbitTiltDeg)
                                } else {
                                    Vec3(
                                        x = node.x + node.explodeVectorX * explodeFactor * 1.5f,
                                        y = node.y + node.explodeVectorY * explodeFactor * 1.5f,
                                        z = node.z + node.explodeVectorZ * explodeFactor * 1.5f
                                    )
                                }

                                val rotPos = rotateScenePoint(basePos, effectiveYaw, pitchDeg)
                                val persp = cameraDist / (cameraDist - rotPos.z).coerceAtLeast(1.5f)
                                val sx = cx + rotPos.x * baseScale * persp
                                val sy = cy - rotPos.y * baseScale * persp

                                val rotEnd = if (node.shape == PrimitiveShape3D.ROD) {
                                    val endPos = Vec3(
                                        x = node.endX + node.explodeVectorX * explodeFactor * 1.5f,
                                        y = node.endY + node.explodeVectorY * explodeFactor * 1.5f,
                                        z = node.endZ + node.explodeVectorZ * explodeFactor * 1.5f
                                    )
                                    rotateScenePoint(endPos, effectiveYaw, pitchDeg)
                                } else null

                                hitTargets.add(node to Offset(sx, sy))
                                ProjectedNode(node, sx, sy, rotPos.z, persp, rotPos, rotEnd)
                            }.sortedBy { it.depth }

                            lastHitTargets = hitTargets

                            projected.forEach { p ->
                                val baseColor = Color(p.node.colorHex)
                                val isHighlighted = selectedNode?.id == p.node.id
                                when (p.node.shape) {
                                    PrimitiveShape3D.RING -> {
                                        drawProjectedRing(
                                            node = p.node,
                                            center = Offset(cx, cy),
                                            baseScale = baseScale,
                                            cameraDist = cameraDist,
                                            yawDeg = effectiveYaw,
                                            pitchDeg = pitchDeg,
                                            explodeFactor = explodeFactor,
                                            color = baseColor,
                                            highlighted = isHighlighted
                                        )
                                    }

                                    PrimitiveShape3D.ROD -> {
                                        val end = p.rotatedEnd
                                        if (end != null) {
                                            val endPersp = cameraDist / (cameraDist - end.z).coerceAtLeast(1.5f)
                                            val ex = cx + end.x * baseScale * endPersp
                                            val ey = cy - end.y * baseScale * endPersp
                                            drawLine(
                                                color = if (isHighlighted) Color.White else baseColor,
                                                start = Offset(p.screenX, p.screenY),
                                                end = Offset(ex, ey),
                                                strokeWidth = (p.node.radiusOrSize * baseScale * 1.1f).coerceIn(4f, 18f),
                                                cap = StrokeCap.Round
                                            )
                                        }
                                    }

                                    PrimitiveShape3D.CUBE -> {
                                        drawProjectedCube(
                                            node = p.node,
                                            center = Offset(cx, cy),
                                            baseScale = baseScale,
                                            cameraDist = cameraDist,
                                            yawDeg = effectiveYaw,
                                            pitchDeg = pitchDeg,
                                            explodeFactor = explodeFactor,
                                            color = baseColor,
                                            highlighted = isHighlighted
                                        )
                                    }

                                    PrimitiveShape3D.CONE -> {
                                        drawProjectedCone(
                                            node = p.node,
                                            center = Offset(cx, cy),
                                            baseScale = baseScale,
                                            cameraDist = cameraDist,
                                            yawDeg = effectiveYaw,
                                            pitchDeg = pitchDeg,
                                            explodeFactor = explodeFactor,
                                            color = baseColor,
                                            highlighted = isHighlighted
                                        )
                                    }

                                    PrimitiveShape3D.SPHERE -> {
                                        val r = (p.node.radiusOrSize * baseScale * p.scale).coerceIn(8f, 120f)
                                        val centerPt = Offset(p.screenX, p.screenY)
                                        // Ambient shadow + 3D radial specular highlight
                                        drawCircle(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    lerpColor(baseColor, Color.White, 0.55f),
                                                    baseColor,
                                                    lerpColor(baseColor, Color.Black, 0.45f)
                                                ),
                                                center = Offset(centerPt.x - r * 0.28f, centerPt.y - r * 0.28f),
                                                radius = r * 1.35f
                                            ),
                                            radius = r,
                                            center = centerPt
                                        )
                                        if (isHighlighted) {
                                            drawCircle(
                                                color = Color.White,
                                                radius = r + 4f,
                                                center = centerPt,
                                                style = Stroke(width = 3f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Overlay control badges inside 3D canvas
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xAA101914)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(Icons.Filled.TouchApp, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Text(
                                        text = "Drag to rotate • Tap parts",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xAA101914)
                                ) {
                                    IconButton(
                                        onClick = { autoOrbit = !autoOrbit },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (autoOrbit) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                            contentDescription = if (autoOrbit) "Pause orbit" else "Resume orbit",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xAA101914)
                                ) {
                                    IconButton(
                                        onClick = {
                                            yawDeg = 24f
                                            pitchDeg = -18f
                                            zoomFactor = 1.0f
                                            explodeFactor = 0f
                                            autoOrbit = true
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Refresh,
                                            contentDescription = "Reset 3D camera",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Sliders: Explode Layers & Zoom
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Explode Layers",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.width(108.dp)
                            )
                            Slider(
                                value = explodeFactor,
                                onValueChange = { explodeFactor = it },
                                valueRange = 0f..1f,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("lab3d_explode_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = ForestEmerald,
                                    activeTrackColor = ForestEmerald
                                )
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.width(108.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Filled.ZoomIn, null, tint = ForestEmerald, modifier = Modifier.size(16.dp))
                                Text("Zoom", style = MaterialTheme.typography.labelLarge)
                            }
                            Slider(
                                value = zoomFactor,
                                onValueChange = { zoomFactor = it },
                                valueRange = 0.65f..1.75f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = WarmAmber,
                                    activeTrackColor = WarmAmber
                                )
                            )
                        }
                    }
                }
            }
        }

        // Selectable Part / Hotspot Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Inspect 3D Components",
                    style = MaterialTheme.typography.titleMedium
                )
                val distinctLabels = activeScene.nodes.distinctBy { it.label }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    distinctLabels.forEach { node ->
                        val isChosen = selectedNode?.label == node.label
                        FilterChip(
                            selected = isChosen,
                            onClick = { onSelectNode(node) },
                            label = { Text(node.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MintSurface,
                                selectedLabelColor = ForestEmeraldDark,
                                containerColor = ParchmentCard
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isChosen,
                                borderColor = ParchmentBorder,
                                selectedBorderColor = ForestEmerald
                            )
                        )
                    }
                }
            }
        }

        // Owl Component Detail & Concept Summary Card
        item {
            val activeDetail = selectedNode
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MintSurface,
                border = BorderStroke(1.dp, MintBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (activeDetail != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(Color(activeDetail.colorHex), CircleShape)
                            )
                            Text(
                                text = activeDetail.label,
                                style = MaterialTheme.typography.titleLarge,
                                color = ForestEmeraldDark
                            )
                        }
                        Text(
                            text = activeDetail.detail,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "OWL'S 3D OVERVIEW",
                                style = MaterialTheme.typography.labelSmall,
                                color = ForestEmerald
                            )
                            Text(
                                text = activeScene.owlSummary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            activeScene.keyTakeaways.forEach { point ->
                                Text(
                                    text = "• $point",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Custom AI 3D Concept Generator
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = ParchmentCard,
                border = BorderStroke(1.dp, ParchmentBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.AutoAwesome, null, tint = ForestEmerald)
                        Column {
                            Text("Generate Any Topic in 3D", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = if (isOnline) "AI builds a rotatable 3D model for $trackTitle ($subject)"
                                else "Reconnect to generate new custom 3D topics",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedTextField(
                        value = customTopic,
                        onValueChange = {
                            customTopic = it
                            if (generationState is UiState.Error) onClearGenerationError()
                        },
                        placeholder = { Text("e.g., Human Heart, Methane CH4, Prism Dispersion...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lab3d_topic_input"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ForestEmerald,
                            unfocusedBorderColor = ParchmentBorder,
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background
                        )
                    )

                    if (generationState is UiState.Error) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = WarmAmberSoft,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = generationState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF7B3E00),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            onGenerateCustom3D(customTopic)
                            customTopic = ""
                        },
                        enabled = isOnline && !isGenerating && customTopic.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("lab3d_generate_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestEmerald)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Building 3D Model...")
                        } else {
                            Text("Visualize in 3D", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun rotateAroundX(p: Vec3, deg: Float): Vec3 {
    val rad = deg * PI.toFloat() / 180f
    val c = cos(rad)
    val s = sin(rad)
    return Vec3(p.x, p.y * c - p.z * s, p.y * s + p.z * c)
}

private fun rotateScenePoint(p: Vec3, yawDeg: Float, pitchDeg: Float): Vec3 {
    val yaw = yawDeg * PI.toFloat() / 180f
    val pitch = pitchDeg * PI.toFloat() / 180f
    // Y-axis rotation (yaw)
    val cy = cos(yaw)
    val sy = sin(yaw)
    val x1 = p.x * cy + p.z * sy
    val z1 = -p.x * sy + p.z * cy
    // X-axis rotation (pitch)
    val cp = cos(pitch)
    val sp = sin(pitch)
    val y2 = p.y * cp - z1 * sp
    val z2 = p.y * sp + z1 * cp
    return Vec3(x1, y2, z2)
}

private fun projectPoint(
    p: Vec3,
    center: Offset,
    baseScale: Float,
    cameraDist: Float
): Offset {
    val persp = cameraDist / (cameraDist - p.z).coerceAtLeast(1.5f)
    return Offset(
        center.x + p.x * baseScale * persp,
        center.y - p.y * baseScale * persp
    )
}

private fun DrawScope.drawPerspectiveFloorGrid(
    center: Offset,
    scale: Float,
    yawDeg: Float,
    pitchDeg: Float
) {
    val gridColor = Color(0x28A7D3B9)
    val range = 2
    for (i in -range..range) {
        val a = rotateScenePoint(Vec3(i.toFloat(), -1.55f, -range.toFloat()), yawDeg, pitchDeg)
        val b = rotateScenePoint(Vec3(i.toFloat(), -1.55f, range.toFloat()), yawDeg, pitchDeg)
        val pa = projectPoint(a, center, scale * 0.72f, 8.5f)
        val pb = projectPoint(b, center, scale * 0.72f, 8.5f)
        drawLine(gridColor, pa, pb, strokeWidth = 1.5f)

        val c = rotateScenePoint(Vec3(-range.toFloat(), -1.55f, i.toFloat()), yawDeg, pitchDeg)
        val d = rotateScenePoint(Vec3(range.toFloat(), -1.55f, i.toFloat()), yawDeg, pitchDeg)
        val pc = projectPoint(c, center, scale * 0.72f, 8.5f)
        val pd = projectPoint(d, center, scale * 0.72f, 8.5f)
        drawLine(gridColor, pc, pd, strokeWidth = 1.5f)
    }
}

private fun DrawScope.drawProjectedRing(
    node: SceneNode3D,
    center: Offset,
    baseScale: Float,
    cameraDist: Float,
    yawDeg: Float,
    pitchDeg: Float,
    explodeFactor: Float,
    color: Color,
    highlighted: Boolean
) {
    val segments = 48
    val r = node.radiusOrSize
    val offset = Vec3(
        node.x + node.explodeVectorX * explodeFactor * 1.5f,
        node.y + node.explodeVectorY * explodeFactor * 1.5f,
        node.z + node.explodeVectorZ * explodeFactor * 1.5f
    )
    val path = Path()
    for (i in 0..segments) {
        val theta = (i.toFloat() / segments) * 2f * PI.toFloat()
        val local = rotateAroundX(Vec3(cos(theta) * r, 0f, sin(theta) * r), node.orbitTiltDeg)
        val world = Vec3(local.x + offset.x, local.y + offset.y, local.z + offset.z)
        val rot = rotateScenePoint(world, yawDeg, pitchDeg)
        val pt = projectPoint(rot, center, baseScale, cameraDist)
        if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
    }
    drawPath(
        path = path,
        color = if (highlighted) Color.White else color.copy(alpha = 0.85f),
        style = Stroke(width = if (highlighted) 5f else 3f)
    )
}

private fun DrawScope.drawProjectedCube(
    node: SceneNode3D,
    center: Offset,
    baseScale: Float,
    cameraDist: Float,
    yawDeg: Float,
    pitchDeg: Float,
    explodeFactor: Float,
    color: Color,
    highlighted: Boolean
) {
    val h = node.radiusOrSize * 0.5f
    val ox = node.x + node.explodeVectorX * explodeFactor * 1.5f
    val oy = node.y + node.explodeVectorY * explodeFactor * 1.5f
    val oz = node.z + node.explodeVectorZ * explodeFactor * 1.5f
    val rawVertices = listOf(
        Vec3(-h + ox, -h + oy, -h + oz),
        Vec3(h + ox, -h + oy, -h + oz),
        Vec3(h + ox, h + oy, -h + oz),
        Vec3(-h + ox, h + oy, -h + oz),
        Vec3(-h + ox, -h + oy, h + oz),
        Vec3(h + ox, -h + oy, h + oz),
        Vec3(h + ox, h + oy, h + oz),
        Vec3(-h + ox, h + oy, h + oz)
    )
    val pts = rawVertices.map { v ->
        val rot = rotateScenePoint(v, yawDeg, pitchDeg)
        projectPoint(rot, center, baseScale, cameraDist)
    }
    val faces = listOf(
        listOf(0, 1, 2, 3),
        listOf(4, 5, 6, 7),
        listOf(0, 1, 5, 4),
        listOf(2, 3, 7, 6)
    )
    faces.forEachIndexed { idx, f ->
        val path = Path().apply {
            moveTo(pts[f[0]].x, pts[f[0]].y)
            lineTo(pts[f[1]].x, pts[f[1]].y)
            lineTo(pts[f[2]].x, pts[f[2]].y)
            lineTo(pts[f[3]].x, pts[f[3]].y)
            close()
        }
        drawPath(path, color.copy(alpha = 0.20f + idx * 0.06f))
    }
    val edges = listOf(
        0 to 1, 1 to 2, 2 to 3, 3 to 0,
        4 to 5, 5 to 6, 6 to 7, 7 to 4,
        0 to 4, 1 to 5, 2 to 6, 3 to 7
    )
    edges.forEach { (a, b) ->
        drawLine(
            color = if (highlighted) Color.White else color,
            start = pts[a],
            end = pts[b],
            strokeWidth = if (highlighted) 4.5f else 3f
        )
    }
}

private fun DrawScope.drawProjectedCone(
    node: SceneNode3D,
    center: Offset,
    baseScale: Float,
    cameraDist: Float,
    yawDeg: Float,
    pitchDeg: Float,
    explodeFactor: Float,
    color: Color,
    highlighted: Boolean
) {
    val r = node.radiusOrSize * 0.55f
    val height = node.radiusOrSize * 1.15f
    val ox = node.x + node.explodeVectorX * explodeFactor * 1.5f
    val oy = node.y + node.explodeVectorY * explodeFactor * 1.5f
    val oz = node.z + node.explodeVectorZ * explodeFactor * 1.5f

    val apex = projectPoint(
        rotateScenePoint(Vec3(ox, oy + height * 0.55f, oz), yawDeg, pitchDeg),
        center,
        baseScale,
        cameraDist
    )
    val segs = 18
    val basePts = (0 until segs).map { i ->
        val a = (i.toFloat() / segs) * 2f * PI.toFloat()
        val v = Vec3(ox + cos(a) * r, oy - height * 0.45f, oz + sin(a) * r)
        projectPoint(rotateScenePoint(v, yawDeg, pitchDeg), center, baseScale, cameraDist)
    }

    val basePath = Path().apply {
        basePts.forEachIndexed { idx, pt ->
            if (idx == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
        }
        close()
    }
    drawPath(basePath, color.copy(alpha = 0.35f))
    drawPath(basePath, if (highlighted) Color.White else color, style = Stroke(width = 3f))

    basePts.forEachIndexed { idx, pt ->
        if (idx % 2 == 0) {
            drawLine(
                color = if (highlighted) Color.White else color.copy(alpha = 0.85f),
                start = apex,
                end = pt,
                strokeWidth = 2.5f
            )
        }
    }
}

private fun lerpColor(a: Color, b: Color, t: Float): Color {
    val f = t.coerceIn(0f, 1f)
    return Color(
        red = a.red + (b.red - a.red) * f,
        green = a.green + (b.green - a.green) * f,
        blue = a.blue + (b.blue - a.blue) * f,
        alpha = a.alpha + (b.alpha - a.alpha) * f
    )
}
