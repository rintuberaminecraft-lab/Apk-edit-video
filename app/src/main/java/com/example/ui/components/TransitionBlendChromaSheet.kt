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
import com.example.model.BlendModeType
import com.example.model.TransitionType
import com.example.ui.theme.YouTubeRed
import com.example.viewmodel.EditorViewModel

@Composable
fun TransitionBlendChromaSheet(
    viewModel: EditorViewModel,
    initialTab: Int = 0, // 0: Transition, 1: Blend, 2: ChromaKey
    modifier: Modifier = Modifier
) {
    val selectedClip by viewModel.selectedClip.collectAsState()
    val clip = selectedClip ?: return

    var currentTab by remember { mutableStateOf(initialTab) }
    val trans = clip.transition
    val blend = clip.blendMode
    val chroma = clip.chromaKey

    val chromaColors = listOf(
        0xFF00FF00 to "Green Screen",
        0xFF0066FF to "Blue Screen",
        0xFFFF0033 to "Red Screen",
        0xFF000000 to "Black Screen"
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
                TabRow(
                    selectedTabIndex = currentTab,
                    modifier = Modifier.weight(1f)
                ) {
                    Tab(selected = currentTab == 0, onClick = { currentTab = 0 }, text = { Text("Transitions", fontSize = 11.sp) })
                    Tab(selected = currentTab == 1, onClick = { currentTab = 1 }, text = { Text("Blend", fontSize = 11.sp) })
                    Tab(selected = currentTab == 2, onClick = { currentTab = 2 }, text = { Text("ChromaKey", fontSize = 11.sp) })
                }

                IconButton(
                    onClick = { viewModel.closeSheet() },
                    modifier = Modifier.testTag("close_effects_sheet")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (currentTab) {
                0 -> {
                    // Transitions: 12 Types
                    Text(
                        text = "12 Animation & Transition Presets",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(TransitionType.values()) { type ->
                            val isSelected = trans.type == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateTransition(trans.copy(type = type)) },
                                label = { Text(type.label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = YouTubeRed,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    if (trans.type != TransitionType.NONE) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Transition Duration: ${(trans.durationMs / 1000f)}s",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = trans.durationMs.toFloat(),
                            onValueChange = { viewModel.updateTransition(trans.copy(durationMs = it.toLong())) },
                            valueRange = 200f..2000f
                        )
                    }
                }

                1 -> {
                    // Blend Modes: Normal, Screen, Multiply, Overlay, Hard Light, Divide
                    Text(
                        text = "Composite Blend Modes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(BlendModeType.values()) { b ->
                            val isSelected = blend == b
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateBlendMode(b) },
                                label = { Text(b.label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = YouTubeRed,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                2 -> {
                    // ChromaKey: Green, other colors, Strength, Shadow
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enable Chroma Key (Green Screen)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(
                            checked = chroma.isEnabled,
                            onCheckedChange = { viewModel.updateChromaKey(chroma.copy(isEnabled = it)) }
                        )
                    }

                    if (chroma.isEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Target Key Color:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            chromaColors.forEach { (colorHex, name) ->
                                val isSel = chroma.targetColor == colorHex
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(colorHex))
                                            .border(
                                                width = if (isSel) 3.dp else 1.dp,
                                                color = if (isSel) Color.White else Color.Gray,
                                                shape = CircleShape
                                            )
                                            .clickable { viewModel.updateChromaKey(chroma.copy(targetColor = colorHex)) }
                                    )
                                    Text(name.split(" ").first(), fontSize = 9.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Strength Control
                        Text(
                            text = "Color Key Strength: ${chroma.strength.toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = chroma.strength,
                            onValueChange = { viewModel.updateChromaKey(chroma.copy(strength = it)) },
                            valueRange = 0f..100f
                        )

                        // Shadow Control
                        Text(
                            text = "Shadow / Feather: ${chroma.shadow.toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = chroma.shadow,
                            onValueChange = { viewModel.updateChromaKey(chroma.copy(shadow = it)) },
                            valueRange = 0f..100f
                        )
                    }
                }
            }
        }
    }
}
