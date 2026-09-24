package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.model.ExportConfig
import com.example.model.ExportFps
import com.example.model.ExportResolution
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.YouTubeRed
import com.example.viewmodel.EditorViewModel

@Composable
fun ExportDialog(
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier
) {
    val exportProgress by viewModel.exportProgress.collectAsState()
    val project by viewModel.project.collectAsState()
    val proj = project ?: return

    var selectedRes by remember { mutableStateOf(ExportResolution.RES_1080P) }
    var selectedFps by remember { mutableStateOf(ExportFps.FPS_60) }

    // Dynamic bitrate rule requested: 7.5Mb for 720P, 12 mb for 1080P
    val activeBitrate = if (selectedRes == ExportResolution.RES_720P) 7.5f else 12.0f
    val durationSec = proj.durationMs / 1000f
    val estimatedSizeMb = (activeBitrate * durationSec) / 8f

    AlertDialog(
        onDismissRequest = {
            if (exportProgress?.isExporting != true) {
                viewModel.closeSheet()
            }
        },
        modifier = modifier.testTag("export_dialog"),
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = YouTubeRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export YouTube Video", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                val progress = exportProgress
                if (progress != null && (progress.isExporting || progress.isCompleted)) {
                    // Export Progress View
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (progress.isCompleted) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Export Completed",
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Video Exported Successfully!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Saved to Gallery as ${progress.outputFileName} (${String.format("%.1f", progress.outputSizeMb)} MB)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LinearProgressIndicator(
                                progress = { progress.progressPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = YouTubeRed
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${progress.progressPercent}% Rendered",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${progress.currentFrame} / ${progress.totalFrames} frames",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Encoding at ${progress.config.resolution.label} • ${progress.config.bitrateMbps} Mbps • ~${progress.estimatedTimeRemainingSec}s left",
                                fontSize = 11.sp,
                                color = StudioCyan
                            )
                        }
                    }
                } else {
                    // Configuration view
                    Text(
                        text = "Resolution (YouTube Optimized)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExportResolution.values().forEach { res ->
                            val isSel = selectedRes == res
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedRes = res },
                                label = { Text(res.label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = YouTubeRed,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Frame Rate",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExportFps.values().forEach { fps ->
                            val isSel = selectedFps == fps
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedFps = fps },
                                label = { Text("${fps.fps} FPS", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = YouTubeRed,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Bitrate Setting:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${activeBitrate} Mbps (Constant)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = YouTubeRed)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Est. File Size:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${String.format("%.1f", estimatedSizeMb)} MB", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Aspect Ratio:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(proj.aspectRatio.ratioStr, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val progress = exportProgress
            if (progress != null && progress.isCompleted) {
                Button(
                    onClick = { viewModel.cancelExport() },
                    colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed)
                ) {
                    Text("Done")
                }
            } else if (progress != null && progress.isExporting) {
                OutlinedButton(onClick = { viewModel.cancelExport() }) {
                    Text("Cancel")
                }
            } else {
                Button(
                    onClick = {
                        viewModel.startExport(
                            ExportConfig(
                                resolution = selectedRes,
                                fps = selectedFps,
                                bitrateMbps = activeBitrate
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                    modifier = Modifier.testTag("start_export_button")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Start Export")
                }
            }
        },
        dismissButton = {
            if (exportProgress?.isExporting != true && exportProgress?.isCompleted != true) {
                TextButton(onClick = { viewModel.closeSheet() }) {
                    Text("Cancel")
                }
            }
        }
    )
}
