package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.model.CropConfig
import com.example.ui.theme.YouTubeRed
import com.example.viewmodel.EditorViewModel

@Composable
fun TransformCropSheet(
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier
) {
    val selectedClip by viewModel.selectedClip.collectAsState()
    val clip = selectedClip ?: return
    val t = clip.transform
    val crop = clip.crop

    var selectedTab by remember { mutableStateOf(0) } // 0: Transform, 1: Crop

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
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.width(220.dp)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Transform & Z") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Crop") }
                    )
                }

                IconButton(
                    onClick = { viewModel.closeSheet() },
                    modifier = Modifier.testTag("close_transform_sheet")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTab == 0) {
                // Quick Flip & Mirror Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(onClick = { viewModel.flipHorizontal() }) {
                        Icon(Icons.Default.Flip, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (t.flipHorizontal) "Flipped H ✓" else "Flip H", fontSize = 11.sp)
                    }
                    OutlinedButton(onClick = { viewModel.flipVertical() }) {
                        Icon(Icons.Default.FlipCameraAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (t.flipVertical) "Flipped V ✓" else "Flip V", fontSize = 11.sp)
                    }
                    OutlinedButton(onClick = { viewModel.updateTransform(mirror = !t.mirror) }) {
                        Icon(Icons.Default.Compare, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (t.mirror) "Mirror ✓" else "Mirror", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Zoom / Scale Slider (0.2x to 5.0x)
                Text(
                    text = "Zoom / Scale: ${String.format("%.2f", t.scale)}x",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = t.scale,
                    onValueChange = { viewModel.updateTransform(scale = it) },
                    valueRange = 0.2f..5.0f
                )

                // Rotation Slider (-180° to +180°)
                Text(
                    text = "Rotation: ${t.rotation.toInt()}°",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = t.rotation,
                    onValueChange = { viewModel.updateTransform(rotation = it) },
                    valueRange = -180f..180f
                )

                // Position X and Z
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Position X: ${String.format("%.2f", t.positionX)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = t.positionX,
                            onValueChange = { viewModel.updateTransform(posX = it) },
                            valueRange = -1.0f..1.0f
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Layer Depth (Position Z): ${t.positionZ}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = t.positionZ.toFloat(),
                            onValueChange = { viewModel.updateTransform(posZ = it.toInt()) },
                            valueRange = 1f..70f
                        )
                    }
                }
            } else {
                // Crop Controls
                Text(
                    text = "Custom Crop Aspect Ratio",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                val cropPresets = listOf("Free", "16:9", "9:16", "1:1", "4:5")
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cropPresets) { preset ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                viewModel.updateCrop(
                                    crop.copy(
                                        isEnabled = true,
                                        left = 0.1f,
                                        top = 0.1f,
                                        right = 0.9f,
                                        bottom = 0.9f
                                    )
                                )
                            },
                            label = { Text(preset, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Crop Insets (Left / Right / Top / Bottom)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = crop.left,
                    onValueChange = { viewModel.updateCrop(crop.copy(isEnabled = true, left = it, right = 1f - it)) },
                    valueRange = 0f..0.4f
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { viewModel.updateCrop(CropConfig(isEnabled = false)) }
                    ) {
                        Text("Reset Crop", color = YouTubeRed)
                    }
                }
            }
        }
    }
}
