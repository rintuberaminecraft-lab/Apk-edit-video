package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DoodleAnimType
import com.example.model.DrawDoodle
import com.example.ui.theme.YouTubeRed
import com.example.viewmodel.EditorViewModel

@Composable
fun DoodleEditorSheet(
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier
) {
    val selectedClip by viewModel.selectedClip.collectAsState()
    val clip = selectedClip ?: return
    val doodle = clip.doodle

    val paletteColors = listOf(
        0xFFFF0055, 0xFFFFCC00, 0xFF00E5FF, 0xFF00E676,
        0xFFD500F9, 0xFFFFFFFF, 0xFF000000, 0xFFFF6D00
    )

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Draw & Doodle",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Brush color, strokes & entrance animation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(
                        onClick = {
                            if (doodle.paths.isNotEmpty()) {
                                viewModel.updateDoodle(doodle.copy(paths = doodle.paths.dropLast(1)))
                            }
                        }
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = "Undo stroke", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(
                        onClick = { viewModel.updateDoodle(doodle.copy(paths = emptyList())) }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear all", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(
                        onClick = { viewModel.closeSheet() },
                        modifier = Modifier.testTag("close_doodle_sheet")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Color Palette
            Text(
                text = "Brush Color",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(paletteColors) { colorHex ->
                    val isSelected = doodle.currentColor == colorHex
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(colorHex))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.Gray,
                                shape = CircleShape
                            )
                            .clickable { viewModel.updateDoodle(doodle.copy(currentColor = colorHex)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stroke Width Slider
            Text(
                text = "Stroke Width: ${doodle.currentStrokeWidth.toInt()}px",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = doodle.currentStrokeWidth,
                onValueChange = { viewModel.updateDoodle(doodle.copy(currentStrokeWidth = it)) },
                valueRange = 2f..40f
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Doodle Animation Selection: Draw in, Fade, Zoom in, Up, down, left, right
            Text(
                text = "Entrance Animation",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(DoodleAnimType.values()) { anim ->
                    val isSelected = doodle.animation == anim
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.updateDoodle(doodle.copy(animation = anim)) },
                        label = {
                            Text(
                                text = anim.label,
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

            Spacer(modifier = Modifier.height(8.dp))

            // Animation Duration / Speed Control
            Text(
                text = "Animation Speed: ${(doodle.animDurationMs / 1000f)}s",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = doodle.animDurationMs.toFloat(),
                onValueChange = { viewModel.updateDoodle(doodle.copy(animDurationMs = it.toLong())) },
                valueRange = 200f..2500f
            )
        }
    }
}
