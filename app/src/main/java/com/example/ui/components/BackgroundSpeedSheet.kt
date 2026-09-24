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
import androidx.compose.material.icons.filled.*
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
import com.example.model.BackgroundConfig
import com.example.model.BgType
import com.example.ui.theme.YouTubeRed
import com.example.viewmodel.EditorViewModel

@Composable
fun BackgroundSpeedSheet(
    viewModel: EditorViewModel,
    initialTab: Int = 0, // 0: Background, 1: Speed & Time
    modifier: Modifier = Modifier
) {
    val project by viewModel.project.collectAsState()
    val selectedClip by viewModel.selectedClip.collectAsState()
    val proj = project ?: return
    val clip = selectedClip ?: return

    var currentTab by remember { mutableStateOf(initialTab) }
    val bg = proj.background

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
                    modifier = Modifier.width(220.dp)
                ) {
                    Tab(selected = currentTab == 0, onClick = { currentTab = 0 }, text = { Text("Background") })
                    Tab(selected = currentTab == 1, onClick = { currentTab = 1 }, text = { Text("Speed & Time") })
                }

                IconButton(
                    onClick = { viewModel.closeSheet() },
                    modifier = Modifier.testTag("close_bg_speed_sheet")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (currentTab == 0) {
                // Background Options: Default Blur, Black, White, Custom Media
                Text(
                    text = "Canvas Background (YouTube Format)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(BgType.values()) { type ->
                        val isSelected = bg.type == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateBackground(bg.copy(type = type)) },
                            label = { Text(type.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = YouTubeRed,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                if (bg.type == BgType.BLUR) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Default Blur Intensity: ${bg.blurIntensity.toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = bg.blurIntensity,
                        onValueChange = { viewModel.updateBackground(bg.copy(blurIntensity = it)) },
                        valueRange = 10f..100f
                    )
                }

                if (bg.type == BgType.COLOR) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(0xFF000000, 0xFFFFFFFF, 0xFFFF0033, 0xFF18191E, 0xFF00E5FF).forEach { colorHex ->
                            val isSel = bg.solidColor == colorHex
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorHex))
                                    .border(if (isSel) 3.dp else 1.dp, if (isSel) Color.White else Color.Gray, CircleShape)
                                    .clickable { viewModel.updateBackground(bg.copy(solidColor = colorHex)) }
                            )
                        }
                    }
                }

                if (bg.type == BgType.MEDIA) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            viewModel.updateBackground(bg.copy(customMediaUri = "imported_bg_sample"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (bg.customMediaUri != null) "Background Image Loaded ✓" else "Import Background Image or Video")
                    }
                }
            } else {
                // Speed (0.20x to 5.0x), Freeze Frame, Reverse
                Text(
                    text = "Playback Speed: ${String.format("%.2f", clip.speed)}x",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Slider(
                    value = clip.speed,
                    onValueChange = { viewModel.setSpeed(it) },
                    valueRange = 0.20f..5.0f,
                    colors = SliderDefaults.colors(thumbColor = YouTubeRed, activeTrackColor = YouTubeRed)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(0.25f, 0.5f, 1.0f, 2.0f, 3.0f, 5.0f).forEach { speedVal ->
                        TextButton(
                            onClick = { viewModel.setSpeed(speedVal) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("${speedVal}x", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Reverse & Freeze Frame Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = { viewModel.toggleReverse() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FastRewind, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (clip.isReversed) "Reversed ✓" else "Reverse", fontSize = 11.sp)
                    }

                    FilledTonalButton(
                        onClick = { viewModel.freezeFrame(2000L) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PauseCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Freeze Frame (2s)", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
