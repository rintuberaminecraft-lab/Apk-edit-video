package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CurvePreset
import com.example.model.Keyframe
import com.example.ui.theme.KeyframeAmber
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.YouTubeRed
import com.example.util.KeyframeInterpolator
import com.example.viewmodel.EditorViewModel

@Composable
fun KeyframeGraphSheet(
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier
) {
    val selectedClip by viewModel.selectedClip.collectAsState()
    val playheadMs by viewModel.playheadMs.collectAsState()

    val clip = selectedClip ?: return
    val keyframes = clip.keyframes.sortedBy { it.timeMs }

    // Find active keyframe near playhead or first available
    val activeKeyframe = keyframes.minByOrNull { kotlin.math.abs(it.timeMs - playheadMs) }
        ?: keyframes.firstOrNull()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Keyframe Curve Graph",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (activeKeyframe != null) "Keyframe at ${(activeKeyframe.timeMs / 1000f)}s • Drag Tangent Handles"
                        else "No keyframe selected - add one on timeline",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { viewModel.closeSheet() },
                    modifier = Modifier.testTag("close_graph_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Curve Presets Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CurvePreset.values()) { preset ->
                    val isSelected = activeKeyframe?.curvePreset == preset
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.updateSelectedKeyframeCurve(preset) },
                        label = {
                            Text(
                                text = preset.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = YouTubeRed,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Bezier Curve Canvas with Left and Right Tangent Handles
            if (activeKeyframe != null) {
                InteractiveBezierCanvas(
                    keyframe = activeKeyframe,
                    onHandleMoved = { leftX, leftY, rightX, rightY ->
                        viewModel.updateCustomBezierHandles(
                            activeKeyframe.id,
                            leftX, leftY, rightX, rightY
                        )
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tap '+ Keyframe' above to create animation keyframe",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Handle coordinate info & reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (activeKeyframe != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Left Handle: (${String.format("%.2f", activeKeyframe.leftHandleX)}, ${String.format("%.2f", activeKeyframe.leftHandleY)})",
                            fontSize = 11.sp,
                            color = StudioCyan
                        )
                        Text(
                            text = "Right Handle: (${String.format("%.2f", activeKeyframe.rightHandleX)}, ${String.format("%.2f", activeKeyframe.rightHandleY)})",
                            fontSize = 11.sp,
                            color = KeyframeAmber
                        )
                    }

                    TextButton(
                        onClick = {
                            viewModel.updateCustomBezierHandles(
                                activeKeyframe.id,
                                -0.35f, 0.0f,
                                0.35f, 0.0f
                            )
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Handles", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveBezierCanvas(
    keyframe: Keyframe,
    onHandleMoved: (leftX: Float, leftY: Float, rightX: Float, rightY: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var leftHandleX by remember(keyframe.id) { mutableStateOf(keyframe.leftHandleX) }
    var leftHandleY by remember(keyframe.id) { mutableStateOf(keyframe.leftHandleY) }
    var rightHandleX by remember(keyframe.id) { mutableStateOf(keyframe.rightHandleX) }
    var rightHandleY by remember(keyframe.id) { mutableStateOf(keyframe.rightHandleY) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color(0xFF0F1117), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(keyframe.id) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val w = size.width.toFloat().coerceAtLeast(1f)
                        val h = size.height.toFloat().coerceAtLeast(1f)

                        val p0 = Offset(0f, h)
                        val p3 = Offset(w, 0f)

                        val currentLeftPixel = Offset(
                            p3.x + leftHandleX * w,
                            p3.y - leftHandleY * h
                        )
                        val currentRightPixel = Offset(
                            p0.x + rightHandleX * w,
                            p0.y - rightHandleY * h
                        )

                        val touchPos = change.position
                        val distToLeft = (touchPos - currentLeftPixel).getDistance()
                        val distToRight = (touchPos - currentRightPixel).getDistance()

                        if (distToLeft < distToRight) {
                            // Dragging left handle (relative to end point P3)
                            val deltaXNorm = dragAmount.x / w
                            val deltaYNorm = -dragAmount.y / h
                            leftHandleX = (leftHandleX + deltaXNorm).coerceIn(-1.0f, 0.0f)
                            leftHandleY = (leftHandleY + deltaYNorm).coerceIn(-1.0f, 1.0f)
                        } else {
                            // Dragging right handle (relative to start point P0)
                            val deltaXNorm = dragAmount.x / w
                            val deltaYNorm = -dragAmount.y / h
                            rightHandleX = (rightHandleX + deltaXNorm).coerceIn(0.0f, 1.0f)
                            rightHandleY = (rightHandleY + deltaYNorm).coerceIn(-1.0f, 1.0f)
                        }

                        onHandleMoved(leftHandleX, leftHandleY, rightHandleX, rightHandleY)
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            // Coordinate Grid
            val gridColor = Color(0xFF222634)
            for (i in 1..3) {
                val gx = w * (i / 4f)
                val gy = h * (i / 4f)
                drawLine(gridColor, Offset(gx, 0f), Offset(gx, h), strokeWidth = 1.dp.toPx())
                drawLine(gridColor, Offset(0f, gy), Offset(w, gy), strokeWidth = 1.dp.toPx())
            }

            val p0 = Offset(0f, h)
            val p3 = Offset(w, 0f)

            // Calculate handle positions
            // Right handle from start point
            val rightHandlePixel = Offset(
                p0.x + rightHandleX * w,
                p0.y - rightHandleY * h
            )
            // Left handle toward end point
            val leftHandlePixel = Offset(
                p3.x + leftHandleX * w,
                p3.y - leftHandleY * h
            )

            // Draw Tangent Lines (Dashed)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            drawLine(
                color = KeyframeAmber.copy(alpha = 0.6f),
                start = p0,
                end = rightHandlePixel,
                strokeWidth = 2.dp.toPx(),
                pathEffect = dashEffect
            )
            drawLine(
                color = StudioCyan.copy(alpha = 0.6f),
                start = p3,
                end = leftHandlePixel,
                strokeWidth = 2.dp.toPx(),
                pathEffect = dashEffect
            )

            // Draw Cubic Bezier Curve
            val curvePath = Path().apply {
                moveTo(p0.x, p0.y)
                cubicTo(
                    rightHandlePixel.x, rightHandlePixel.y,
                    leftHandlePixel.x, leftHandlePixel.y,
                    p3.x, p3.y
                )
            }

            drawPath(
                path = curvePath,
                color = Color.White,
                style = Stroke(width = 3.5.dp.toPx())
            )

            // Draw Endpoints
            drawCircle(Color.White, radius = 6.dp.toPx(), center = p0)
            drawCircle(Color.White, radius = 6.dp.toPx(), center = p3)

            // Draw Tangent Handle Control Points (Draggable)
            drawCircle(KeyframeAmber, radius = 10.dp.toPx(), center = rightHandlePixel)
            drawCircle(Color.White, radius = 4.dp.toPx(), center = rightHandlePixel)

            drawCircle(StudioCyan, radius = 10.dp.toPx(), center = leftHandlePixel)
            drawCircle(Color.White, radius = 4.dp.toPx(), center = leftHandlePixel)
        }
    }
}
