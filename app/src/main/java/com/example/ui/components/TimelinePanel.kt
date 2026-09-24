package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Clip
import com.example.model.MediaType
import com.example.model.PreviewLayoutStyle
import com.example.ui.theme.KeyframeAmber
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.YouTubeRed
import com.example.viewmodel.EditorViewModel

@Composable
fun TimelinePanel(
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier
) {
    val project by viewModel.project.collectAsState()
    val playheadMs by viewModel.playheadMs.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val selectedClipId by viewModel.selectedClipId.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val proj = project ?: return
    var isMuted by remember { mutableStateOf(false) }

    val isShortsLayout = settings.previewLayoutStyle == PreviewLayoutStyle.SHORT_VERTICAL

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF13141B))
    ) {
        // 1. Playhead & Playback Control Bar (Matching uploaded screenshot)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Undo & Redo
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.undo() },
                    modifier = Modifier.size(36.dp).testTag("timeline_undo_button"),
                    enabled = viewModel.canUndo
                ) {
                    Icon(
                        Icons.Default.Undo,
                        contentDescription = "Undo",
                        tint = if (viewModel.canUndo) MaterialTheme.colorScheme.onSurface else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { viewModel.redo() },
                    modifier = Modifier.size(36.dp).testTag("timeline_redo_button"),
                    enabled = viewModel.canRedo
                ) {
                    Icon(
                        Icons.Default.Redo,
                        contentDescription = "Redo",
                        tint = if (viewModel.canRedo) MaterialTheme.colorScheme.onSurface else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Center: Large Play / Pause Button
            IconButton(
                onClick = { viewModel.togglePlayPause() },
                modifier = Modifier
                    .size(40.dp)
                    .background(YouTubeRed, CircleShape)
                    .testTag("play_pause_button")
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Right: Split marker & Fullscreen
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.splitClip() },
                    modifier = Modifier.size(36.dp).testTag("timeline_split_button")
                ) {
                    Icon(
                        Icons.Default.ContentCut,
                        contentDescription = "Split",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { /* Fullscreen toggle */ },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Fullscreen,
                        contentDescription = "Fullscreen",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 2. Sub-Bar: + Add Media, Mute Clip, Timecode format (0:00.0 / 2:06.5)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Large Gradient Plus Button (matches screenshot)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(YouTubeRed)
                        .clickable {
                            viewModel.addPipLayer(MediaType.VIDEO, "Imported Clip #${proj.pipLayers.size + 1}")
                        }
                        .testTag("timeline_add_media_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Media", tint = Color.White, modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Mute Clip Button (matches screenshot)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { isMuted = !isMuted }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Mute",
                        tint = if (isMuted) YouTubeRed else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                    Text("Mute Clip", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Timecode Indicator: 0:00.0 / 0:14.0 (matching screenshot style)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatTimecode(playheadMs),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatTimecode(proj.durationMs),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 3. Multi-Track Timeline Canvas & Tracks
        // Height adjusted according to setting:
        // In "Shorts" style: 2 layers visible at top, scroll down to see all 70 layers!
        val timelineHeight = if (isShortsLayout) 150.dp else 220.dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(timelineHeight)
                .background(Color(0xFF0C0D12))
                .padding(horizontal = 8.dp)
        ) {
            val totalDurationMs = proj.durationMs.coerceAtLeast(1000L)

            // Scrubber drag handler
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(totalDurationMs) {
                        detectTapGestures { offset ->
                            val percent = (offset.x / size.width).coerceIn(0f, 1f)
                            viewModel.seekTo((percent * totalDurationMs).toLong())
                        }
                    }
                    .pointerInput(totalDurationMs) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val percent = (change.position.x / size.width).coerceIn(0f, 1f)
                            viewModel.seekTo((percent * totalDurationMs).toLong())
                        }
                    }
            ) {
                // Scrollable Tracks Container
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Time Ruler Track
                    item {
                        TimeRuler(totalDurationMs = totalDurationMs)
                    }

                    // Main Track Row
                    item {
                        MainTrackRow(
                            clips = proj.mainClips,
                            totalDurationMs = totalDurationMs,
                            selectedClipId = selectedClipId,
                            onClipClick = { viewModel.selectClip(it.id) }
                        )
                    }

                    // PIP Layer Tracks (Supporting up to 70 layers!)
                    // In shorts style, shows top 2 layers and scrollable for rest
                    items(proj.pipLayers) { pipClip ->
                        PipTrackRow(
                            clip = pipClip,
                            totalDurationMs = totalDurationMs,
                            isSelected = pipClip.id == selectedClipId,
                            onClipClick = { viewModel.selectClip(pipClip.id) }
                        )
                    }

                    // Audio Track Row
                    items(proj.audioTracks) { audioTrack ->
                        AudioTrackRow(
                            audio = audioTrack,
                            totalDurationMs = totalDurationMs
                        )
                    }
                }

                // Vertical Playhead Line & Cursor
                val playheadPercent = (playheadMs.toFloat() / totalDurationMs).coerceIn(0f, 1f)
                PlayheadScrubberLine(
                    percent = playheadPercent,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun TimeRuler(totalDurationMs: Long) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(18.dp)
    ) {
        val w = size.width
        val seconds = (totalDurationMs / 1000L).toInt().coerceAtLeast(1)
        val step = maxOf(1, seconds / 6)
        for (sec in 0..seconds step step) {
            val x = (sec * 1000f / totalDurationMs) * w
            drawLine(
                color = Color(0xFF4A5064),
                start = Offset(x, 6f),
                end = Offset(x, 16f),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

@Composable
fun MainTrackRow(
    clips: List<Clip>,
    totalDurationMs: Long,
    selectedClipId: String?,
    onClipClick: (Clip) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color(0xFF191B24), RoundedCornerShape(8.dp))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (clip in clips) {
            val clipWeight = (clip.durationMs.toFloat() / totalDurationMs).coerceAtLeast(0.05f)
            val isSelected = clip.id == selectedClipId

            Box(
                modifier = Modifier
                    .weight(clipWeight)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(clip.previewColor).copy(alpha = 0.85f))
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) YouTubeRed else Color.Transparent,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onClipClick(clip) }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                // Clip title
                Text(
                    text = clip.name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    modifier = Modifier.align(Alignment.TopStart)
                )

                // Keyframe Gold Diamonds
                if (clip.keyframes.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        clip.keyframes.forEach { _ ->
                            Icon(
                                Icons.Default.Diamond,
                                contentDescription = "Keyframe",
                                tint = KeyframeAmber,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "${clip.durationMs / 1000}s",
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }
    }
}

@Composable
fun PipTrackRow(
    clip: Clip,
    totalDurationMs: Long,
    isSelected: Boolean,
    onClipClick: () -> Unit
) {
    val startPercent = (clip.startMs.toFloat() / totalDurationMs).coerceIn(0f, 1f)
    val durPercent = (clip.durationMs.toFloat() / totalDurationMs).coerceIn(0.05f, 1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .background(Color(0xFF151720), RoundedCornerShape(6.dp))
            .padding(2.dp)
    ) {
        if (startPercent > 0.01f) {
            Spacer(modifier = Modifier.weight(startPercent))
        }

        Box(
            modifier = Modifier
                .weight(durPercent)
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(clip.previewColor).copy(alpha = 0.7f))
                .border(
                    width = if (isSelected) 2.dp else 0.5.dp,
                    color = if (isSelected) StudioCyan else Color(0xFF353B4D),
                    shape = RoundedCornerShape(6.dp)
                )
                .clickable(onClick = onClipClick)
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    when (clip.mediaType) {
                        MediaType.TEXT -> Icons.Default.TextFields
                        MediaType.DOODLE -> Icons.Default.Brush
                        else -> Icons.Default.Layers
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${clip.name} (Z:${clip.transform.positionZ})",
                    fontSize = 10.sp,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }

        val remainingPercent = (1f - (startPercent + durPercent)).coerceAtLeast(0f)
        if (remainingPercent > 0.01f) {
            Spacer(modifier = Modifier.weight(remainingPercent))
        }
    }
}

@Composable
fun AudioTrackRow(
    audio: com.example.model.AudioTrack,
    totalDurationMs: Long
) {
    val startPercent = (audio.startMs.toFloat() / totalDurationMs).coerceIn(0f, 1f)
    val durPercent = (audio.durationMs.toFloat() / totalDurationMs).coerceIn(0.05f, 1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(Color(0xFF111714), RoundedCornerShape(6.dp))
            .padding(2.dp)
    ) {
        if (startPercent > 0.01f) {
            Spacer(modifier = Modifier.weight(startPercent))
        }

        Box(
            modifier = Modifier
                .weight(durPercent)
                .fillMaxHeight()
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF00E676).copy(alpha = 0.25f))
                .border(0.5.dp, Color(0xFF00E676), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${audio.title} • Vol ${audio.volume.toInt()}%",
                    fontSize = 9.sp,
                    color = Color(0xFF00E676),
                    maxLines = 1
                )
            }
        }

        val remainingPercent = (1f - (startPercent + durPercent)).coerceAtLeast(0f)
        if (remainingPercent > 0.01f) {
            Spacer(modifier = Modifier.weight(remainingPercent))
        }
    }
}

@Composable
fun PlayheadScrubberLine(
    percent: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val x = percent * size.width

        // Vertical Scrubber Needle Line
        drawLine(
            color = Color.White,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 2.dp.toPx()
        )

        // Top Scrubber Needle Head
        drawCircle(
            color = YouTubeRed,
            radius = 6.dp.toPx(),
            center = Offset(x, 4.dp.toPx())
        )
        drawCircle(
            color = Color.White,
            radius = 2.5.dp.toPx(),
            center = Offset(x, 4.dp.toPx())
        )
    }
}

private fun formatTimecode(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    val tenths = (ms % 1000) / 100
    return String.format("%d:%02d.%d", min, sec, tenths)
}
