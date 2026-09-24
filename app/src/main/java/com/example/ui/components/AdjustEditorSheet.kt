package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.model.HslAdjust
import com.example.ui.theme.YouTubeRed
import com.example.viewmodel.EditorViewModel

@Composable
fun AdjustEditorSheet(
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier
) {
    val selectedClip by viewModel.selectedClip.collectAsState()
    val clip = selectedClip ?: return
    val adj = clip.adjust

    val hslColorNames = listOf("Red", "Orange", "Yellow", "Green", "Cyan", "Blue", "Purple", "Magenta")
    var selectedHslColor by remember { mutableStateOf("Red") }

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
                        text = "Color Grading & Adjustments",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "HDR, HSL, Exposure & Keyframed Color Curve",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { viewModel.closeSheet() },
                    modifier = Modifier.testTag("close_adjust_sheet")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Opacity & Fade
                item {
                    Text("Opacity: ${adj.opacity.toInt()}%", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Slider(
                        value = adj.opacity,
                        onValueChange = { viewModel.updateAdjust(adj.copy(opacity = it)) },
                        valueRange = 0f..100f
                    )
                }

                item {
                    Text("Fade: ${adj.fade.toInt()}%", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Slider(
                        value = adj.fade,
                        onValueChange = { viewModel.updateAdjust(adj.copy(fade = it)) },
                        valueRange = 0f..100f
                    )
                }

                item {
                    Text("Exposure: ${adj.exposure.toInt()}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Slider(
                        value = adj.exposure,
                        onValueChange = { viewModel.updateAdjust(adj.copy(exposure = it)) },
                        valueRange = -100f..100f
                    )
                }

                item {
                    Text("Saturation: ${adj.saturation.toInt()}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Slider(
                        value = adj.saturation,
                        onValueChange = { viewModel.updateAdjust(adj.copy(saturation = it)) },
                        valueRange = -100f..100f
                    )
                }

                item {
                    Text("Contrast: ${adj.contrast.toInt()}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Slider(
                        value = adj.contrast,
                        onValueChange = { viewModel.updateAdjust(adj.copy(contrast = it)) },
                        valueRange = -100f..100f
                    )
                }

                item {
                    Text("Sharpness: ${adj.sharpness.toInt()}%", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Slider(
                        value = adj.sharpness,
                        onValueChange = { viewModel.updateAdjust(adj.copy(sharpness = it)) },
                        valueRange = 0f..100f
                    )
                }

                item {
                    Text("HDR Enhancer: ${adj.hdr.toInt()}%", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Slider(
                        value = adj.hdr,
                        onValueChange = { viewModel.updateAdjust(adj.copy(hdr = it)) },
                        valueRange = 0f..100f
                    )
                }

                item {
                    Text("Vignette: ${adj.vignette.toInt()}%", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Slider(
                        value = adj.vignette,
                        onValueChange = { viewModel.updateAdjust(adj.copy(vignette = it)) },
                        valueRange = 0f..100f
                    )
                }

                item {
                    Text("Temperature: ${adj.temperature.toInt()}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Slider(
                        value = adj.temperature,
                        onValueChange = { viewModel.updateAdjust(adj.copy(temperature = it)) },
                        valueRange = -100f..100f
                    )
                }

                item {
                    Text("Hue Tint: ${adj.hue.toInt()}°", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Slider(
                        value = adj.hue,
                        onValueChange = { viewModel.updateAdjust(adj.copy(hue = it)) },
                        valueRange = -180f..180f
                    )
                }

                // 8-Channel HSL
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "8-Channel HSL Tuning",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(hslColorNames) { name ->
                            val isSel = selectedHslColor == name
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedHslColor = name },
                                label = { Text(name, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = YouTubeRed,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    val currentHsl = adj.hslChannels[selectedHslColor] ?: HslAdjust()

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("$selectedHslColor Hue: ${currentHsl.hue.toInt()}°", fontSize = 11.sp)
                    Slider(
                        value = currentHsl.hue,
                        onValueChange = { newH ->
                            val updated = adj.hslChannels.toMutableMap()
                            updated[selectedHslColor] = currentHsl.copy(hue = newH)
                            viewModel.updateAdjust(adj.copy(hslChannels = updated))
                        },
                        valueRange = -180f..180f
                    )

                    Text("$selectedHslColor Saturation: ${currentHsl.saturation.toInt()}%", fontSize = 11.sp)
                    Slider(
                        value = currentHsl.saturation,
                        onValueChange = { newS ->
                            val updated = adj.hslChannels.toMutableMap()
                            updated[selectedHslColor] = currentHsl.copy(saturation = newS)
                            viewModel.updateAdjust(adj.copy(hslChannels = updated))
                        },
                        valueRange = -100f..100f
                    )

                    Text("$selectedHslColor Lightness: ${currentHsl.lightness.toInt()}%", fontSize = 11.sp)
                    Slider(
                        value = currentHsl.lightness,
                        onValueChange = { newL ->
                            val updated = adj.hslChannels.toMutableMap()
                            updated[selectedHslColor] = currentHsl.copy(lightness = newL)
                            viewModel.updateAdjust(adj.copy(hslChannels = updated))
                        },
                        valueRange = -100f..100f
                    )
                }
            }
        }
    }
}
