package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.data.DefaultSampleProvider
import com.example.model.AudioTrack
import com.example.model.EqualizerPreset
import com.example.ui.theme.KeyframeAmber
import com.example.ui.theme.YouTubeRed
import com.example.viewmodel.EditorViewModel

@Composable
fun AudioEditorSheet(
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier
) {
    val project by viewModel.project.collectAsState()
    val selectedAudioId by viewModel.selectedAudioId.collectAsState()
    val proj = project ?: return

    val audioTracks = proj.audioTracks
    val activeTrack = audioTracks.find { it.id == selectedAudioId } ?: audioTracks.firstOrNull()

    var recentToOld by remember { mutableStateOf(true) }
    var showImportAudioDialog by remember { mutableStateOf(false) }

    val eqFrequencies = listOf("31Hz", "62Hz", "125Hz", "250Hz", "500Hz", "1kHz", "2kHz", "4kHz", "8kHz", "16kHz")

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
                        text = "Music & Studio Equalizer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "10-Band EQ & 100%–300% Volume Boost",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { viewModel.closeSheet() },
                    modifier = Modifier.testTag("close_audio_sheet")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sort & Import Music Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Arrange: ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FilledTonalButton(
                        onClick = {
                            recentToOld = !recentToOld
                            viewModel.sortAudioTracks(recentToOld)
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(
                            if (recentToOld) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (recentToOld) "Recent to Old" else "Time New to Old", fontSize = 11.sp)
                    }
                }

                Button(
                    onClick = { showImportAudioDialog = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Add Music", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Recent Music Track Selector
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(audioTracks) { track ->
                    val isSelected = activeTrack?.id == track.id
                    Card(
                        modifier = Modifier
                            .clickable { viewModel.selectAudio(track.id) }
                            .width(180.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = track.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${track.artist} • Vol ${track.volume.toInt()}%",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (activeTrack != null) {
                Spacer(modifier = Modifier.height(16.dp))

                // Volume Slider: 100% to 300% boost!
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Master Volume Boost (100% – 300%):",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${activeTrack.volume.toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (activeTrack.volume > 150f) YouTubeRed else KeyframeAmber
                    )
                }
                Slider(
                    value = activeTrack.volume,
                    onValueChange = { viewModel.updateAudioVolume(activeTrack.id, it) },
                    valueRange = 100f..300f,
                    colors = SliderDefaults.colors(
                        thumbColor = YouTubeRed,
                        activeTrackColor = YouTubeRed
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Equalizer Presets
                Text(
                    text = "Equalizer Preset (4 Default Presets & Custom):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(EqualizerPreset.values()) { preset ->
                        val isSelected = activeTrack.equalizerPreset == preset
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateAudioPreset(activeTrack.id, preset) },
                            label = { Text(preset.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = YouTubeRed,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 10-Band Custom Adjust Sliders
                Text(
                    text = "Custom 10-Band EQ Adjust (-12dB to +12dB):",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(activeTrack.eqBands) { index, bandValue ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(44.dp)
                        ) {
                            Text(
                                text = "${bandValue.toInt()}dB",
                                fontSize = 9.sp,
                                color = if (bandValue > 0) YouTubeRed else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Slider(
                                value = bandValue,
                                onValueChange = { viewModel.updateAudioBand(activeTrack.id, index, it) },
                                valueRange = -12f..12f,
                                modifier = Modifier.height(100.dp)
                            )
                            Text(
                                text = eqFrequencies.getOrElse(index) { "${index}k" },
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }

    if (showImportAudioDialog) {
        val sampleTracks = DefaultSampleProvider.getSampleAudioTracks()
        AlertDialog(
            onDismissRequest = { showImportAudioDialog = false },
            title = { Text("Add YouTube Audio Library Track") },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                    items(sampleTracks) { track ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    viewModel.addAudioTrack(
                                        track.copy(id = java.util.UUID.randomUUID().toString(), addedTimestamp = System.currentTimeMillis())
                                    )
                                    showImportAudioDialog = false
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(track.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${track.artist} • ${track.durationMs / 1000}s", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.Default.AddCircle, contentDescription = "Add", tint = YouTubeRed)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImportAudioDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
