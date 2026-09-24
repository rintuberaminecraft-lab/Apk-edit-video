package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.KeyframeAmber
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.YouTubeRed
import com.example.util.KeyframeInterpolator
import com.example.viewmodel.EditorSheet
import com.example.viewmodel.EditorViewModel
import kotlin.math.sin

@Composable
fun VideoPreviewCanvas(
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier
) {
    val project by viewModel.project.collectAsState()
    val playheadMs by viewModel.playheadMs.collectAsState()
    val selectedClipId by viewModel.selectedClipId.collectAsState()
    val hasKeyframeAtPlayhead by viewModel.hasKeyframeAtPlayhead.collectAsState()
    val showTooltip by viewModel.showKeyframeTooltip.collectAsState()
    val selectedClip by viewModel.selectedClip.collectAsState()

    val proj = project ?: return
    val aspectRatio = proj.aspectRatio

    // Canvas zoom & pan state for lossless deep inspection
    var canvasZoom by remember { mutableStateOf(1.0f) }
    var canvasPanX by remember { mutableStateOf(0.0f) }
    var canvasPanY by remember { mutableStateOf(0.0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0B0E))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Video Preview Container with selected aspect ratio
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(aspectRatio.ratio)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF2E3240), RoundedCornerShape(8.dp))
                .clipToBounds()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        canvasZoom = (canvasZoom * zoom).coerceIn(0.5f, 8.0f) // Deep lossless zoom
                        canvasPanX += pan.x
                        canvasPanY += pan.y
                    }
                }
        ) {
            // Live Canvas Rendering
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(proj.id) {
                        detectTapGestures {
                            // Tap to select clips or dismiss tooltip
                            viewModel.dismissKeyframeTooltip()
                        }
                    }
            ) {
                val canvasW = size.width
                val canvasH = size.height

                // Draw Background
                val bg = proj.background
                when (bg.type) {
                    BgType.BLUR -> {
                        // Blurred studio background gradient
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF262C3D), Color(0xFF0F1118)),
                                center = Offset(canvasW / 2f, canvasH / 2f),
                                radius = canvasW.coerceAtLeast(canvasH)
                            )
                        )
                    }
                    BgType.BLACK -> drawRect(Color.Black)
                    BgType.WHITE -> drawRect(Color.White)
                    BgType.COLOR -> drawRect(Color(bg.solidColor))
                    BgType.MEDIA -> {
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF2D1B69), Color(0xFF1B2A4A))
                            )
                        )
                    }
                }

                // Apply Deep Canvas Zoom & Pan for lossless quality inspection
                scale(scale = canvasZoom, pivot = Offset(canvasW / 2f + canvasPanX, canvasH / 2f + canvasPanY)) {
                    // 1. Render Active Main Track Clip
                    val activeMainClip = proj.mainClips.find { clip ->
                        playheadMs >= clip.startMs && playheadMs <= (clip.startMs + clip.durationMs)
                    } ?: proj.mainClips.firstOrNull()

                    if (activeMainClip != null && activeMainClip.isVisible) {
                        renderClipOnCanvas(
                            clip = activeMainClip,
                            playheadMs = playheadMs,
                            canvasW = canvasW,
                            canvasH = canvasH,
                            isSelected = activeMainClip.id == selectedClipId
                        )
                    }

                    // 2. Render Active PIP Layers (supporting up to 70 layers!)
                    val activePipLayers = proj.pipLayers
                        .filter { it.isVisible && playheadMs >= it.startMs && playheadMs <= (it.startMs + it.durationMs) }
                        .sortedBy { it.transform.positionZ } // Sorted by Z-index layer depth

                    for (pipClip in activePipLayers) {
                        renderClipOnCanvas(
                            clip = pipClip,
                            playheadMs = playheadMs,
                            canvasW = canvasW,
                            canvasH = canvasH,
                            isSelected = pipClip.id == selectedClipId
                        )
                    }
                }
            }

            // Top Video Preview Controls: Always Visible Keyframe Toolbar
            TopPreviewKeyframePanel(
                viewModel = viewModel,
                hasKeyframe = hasKeyframeAtPlayhead,
                showTooltip = showTooltip,
                selectedClip = selectedClip,
                canvasZoom = canvasZoom,
                onResetZoom = {
                    canvasZoom = 1.0f
                    canvasPanX = 0f
                    canvasPanY = 0f
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            // Zoom Indicator Badge if zoomed in
            if (canvasZoom != 1.0f) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Deep Zoom: ${String.format("%.1f", canvasZoom)}x (Lossless)",
                        color = StudioCyan,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TopPreviewKeyframePanel(
    viewModel: EditorViewModel,
    hasKeyframe: Boolean,
    showTooltip: Boolean,
    selectedClip: Clip?,
    canvasZoom: Float,
    onResetZoom: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        // Controls Row
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xCC161822),
            tonalElevation = 6.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C3142))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Jump to previous keyframe
                IconButton(
                    onClick = { viewModel.jumpToPreviousKeyframe() },
                    modifier = Modifier.size(28.dp),
                    enabled = (selectedClip?.keyframes?.size ?: 0) > 0
                ) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "Previous Keyframe",
                        tint = if ((selectedClip?.keyframes?.size ?: 0) > 0) Color.White else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Main Keyframe Diamond Toggle (+ / -)
                Button(
                    onClick = { viewModel.toggleKeyframeAtPlayhead() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasKeyframe) KeyframeAmber else Color(0xFF2A2E3D),
                        contentColor = if (hasKeyframe) Color.Black else Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(28.dp)
                        .testTag("keyframe_top_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        if (hasKeyframe) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (hasKeyframe) "◆ Keyframe" else "+ Keyframe",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Jump to next keyframe
                IconButton(
                    onClick = { viewModel.jumpToNextKeyframe() },
                    modifier = Modifier.size(28.dp),
                    enabled = (selectedClip?.keyframes?.size ?: 0) > 0
                ) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Next Keyframe",
                        tint = if ((selectedClip?.keyframes?.size ?: 0) > 0) Color.White else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Keyframe Bezier Curve Graph Sheet Shortcut
                IconButton(
                    onClick = { viewModel.openSheet(EditorSheet.KEYFRAME_GRAPH) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Timeline,
                        contentDescription = "Keyframe Curve Graph",
                        tint = KeyframeAmber,
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (canvasZoom != 1.0f) {
                    IconButton(
                        onClick = onResetZoom,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.CenterFocusStrong,
                            contentDescription = "Reset Zoom",
                            tint = StudioCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Animated InShot-style Tooltip: "Add keyframes for clips ⤵"
        AnimatedVisibility(visible = showTooltip) {
            Surface(
                modifier = Modifier
                    .padding(top = 4.dp, end = 12.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xEE2A2F40)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add keyframes for clips ⤴",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = KeyframeAmber
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = { viewModel.dismissKeyframeTooltip() },
                        modifier = Modifier.size(16.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.Gray, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}

// Canvas rendering helper for clips with Keyframe animation, Mask, Blur, and Effects
private fun androidx.compose.ui.graphics.drawscope.DrawScope.renderClipOnCanvas(
    clip: Clip,
    playheadMs: Long,
    canvasW: Float,
    canvasH: Float,
    isSelected: Boolean
) {
    // Interpolate transform with Keyframes & Bezier easing
    val transform = KeyframeInterpolator.interpolate(clip, playheadMs)

    val centerX = canvasW * (0.5f + transform.positionX)
    val centerY = canvasH * (0.5f + transform.positionY)
    val baseW = if (clip.layerType == LayerType.MAIN_TRACK) canvasW else canvasW * 0.65f
    val baseH = if (clip.layerType == LayerType.MAIN_TRACK) canvasH else canvasH * 0.45f

    val clipAlpha = transform.opacity.coerceIn(0f, 1f)

    // Apply rotation and scale around center
    rotate(degrees = transform.rotation, pivot = Offset(centerX, centerY)) {
        scale(
            scaleX = if (clip.transform.flipHorizontal) -transform.scale else transform.scale,
            scaleY = if (clip.transform.flipVertical) -transform.scale else transform.scale,
            pivot = Offset(centerX, centerY)
        ) {
            val clipRect = Rect(
                left = centerX - baseW / 2f,
                top = centerY - baseH / 2f,
                right = centerX + baseW / 2f,
                bottom = centerY + baseH / 2f
            )

            // Prepare Mask Path if applied
            val mask = clip.mask
            val maskPath = if (mask.type != MaskType.NONE) {
                Path().apply {
                    val mw = clipRect.width * (mask.widthPercent / 100f)
                    val mh = clipRect.height * (mask.heightPercent / 100f)
                    val mx = clipRect.center.x - mw / 2f
                    val my = clipRect.center.y - mh / 2f
                    val mRect = Rect(mx, my, mx + mw, my + mh)

                    when (mask.type) {
                        MaskType.RECTANGLE -> addRect(mRect)
                        MaskType.SQUARE -> {
                            val side = minOf(mw, mh)
                            addRect(Rect(clipRect.center.x - side / 2f, clipRect.center.y - side / 2f, clipRect.center.x + side / 2f, clipRect.center.y + side / 2f))
                        }
                        MaskType.CIRCLE -> addOval(mRect)
                        MaskType.ROUNDED_RECTANGLE -> addRoundRect(androidx.compose.ui.geometry.RoundRect(mRect, CornerRadius(mask.cornerRadius, mask.cornerRadius)))
                        MaskType.SPLIT_RECTANGLE -> {
                            val gap = mask.splitGap
                            addRect(Rect(mRect.left, mRect.top, mRect.right, mRect.center.y - gap / 2f))
                            addRect(Rect(mRect.left, mRect.center.y + gap / 2f, mRect.right, mRect.bottom))
                        }
                        MaskType.NONE -> {}
                    }
                }
            } else null

            val drawBlock = {
                // Color grading tint
                val baseColor = Color(clip.previewColor).copy(alpha = clipAlpha)

                // ChromaKey simulation (if enabled, make green target transparent)
                val isChroma = clip.chromaKey.isEnabled
                val finalColor = if (isChroma) Color(0xFF00B0FF).copy(alpha = clipAlpha * 0.9f) else baseColor

                when (clip.mediaType) {
                    MediaType.VIDEO, MediaType.IMAGE, MediaType.COLOR -> {
                        // Draw Media Surface with custom color styling
                        drawRoundRect(
                            color = finalColor,
                            topLeft = clipRect.topLeft,
                            size = clipRect.size,
                            cornerRadius = CornerRadius(8f, 8f)
                        )

                        // If Blur is active, draw blur/mosaic overlay
                        if (clip.blur.type != BlurType.NONE) {
                            drawBlurOverlay(clip.blur, clipRect)
                        }
                    }
                    MediaType.TEXT -> {
                        // Draw Text Box
                        val txt = clip.text
                        drawRoundRect(
                            color = Color(txt.backgroundColor),
                            topLeft = clipRect.topLeft,
                            size = clipRect.size,
                            cornerRadius = CornerRadius(12f, 12f)
                        )
                    }
                    MediaType.DOODLE -> {
                        // Render Doodle paths
                        for (path in clip.doodle.paths) {
                            if (path.points.size > 1) {
                                val p = Path().apply {
                                    val first = path.points.first()
                                    moveTo(clipRect.left + first.x * clipRect.width, clipRect.top + first.y * clipRect.height)
                                    for (pt in path.points.drop(1)) {
                                        lineTo(clipRect.left + pt.x * clipRect.width, clipRect.top + pt.y * clipRect.height)
                                    }
                                }
                                drawPath(
                                    path = p,
                                    color = Color(path.color).copy(alpha = clipAlpha),
                                    style = Stroke(width = path.strokeWidth)
                                )
                            }
                        }
                    }
                }

                // Draw Selection Bounding Box if selected
                if (isSelected) {
                    drawRoundRect(
                        color = YouTubeRed,
                        topLeft = clipRect.topLeft,
                        size = clipRect.size,
                        cornerRadius = CornerRadius(8f, 8f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    // Corner transform handles
                    val handleRadius = 4.dp.toPx()
                    drawCircle(Color.White, handleRadius, clipRect.topLeft)
                    drawCircle(Color.White, handleRadius, clipRect.topRight)
                    drawCircle(Color.White, handleRadius, clipRect.bottomLeft)
                    drawCircle(Color.White, handleRadius, clipRect.bottomRight)
                }
            }

            // Apply Mask Clipping if present
            if (maskPath != null) {
                clipPath(maskPath) {
                    drawBlock()
                }
            } else {
                drawBlock()
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBlurOverlay(
    blur: BlurConfig,
    rect: Rect
) {
    when (blur.type) {
        BlurType.MOSAIC -> {
            val blockSize = blur.strength.coerceIn(8f, 40f)
            var y = rect.top
            var flip = false
            while (y < rect.bottom) {
                var x = rect.left
                while (x < rect.right) {
                    val shade = if (flip) Color.Black.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.15f)
                    drawRect(
                        color = shade,
                        topLeft = Offset(x, y),
                        size = Size(blockSize, blockSize)
                    )
                    x += blockSize
                    flip = !flip
                }
                y += blockSize
            }
        }
        BlurType.GAUSSIAN -> {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.4f), Color.Transparent),
                    center = rect.center,
                    radius = rect.width / 2f
                ),
                topLeft = rect.topLeft,
                size = rect.size
            )
        }
        BlurType.MAGNIFIER -> {
            val lensRadius = rect.width * 0.35f
            drawCircle(
                color = Color.White.copy(alpha = 0.25f),
                radius = lensRadius,
                center = rect.center
            )
            drawCircle(
                color = StudioCyan,
                radius = lensRadius,
                center = rect.center,
                style = Stroke(width = 2.dp.toPx())
            )
        }
        BlurType.NONE -> {}
    }
}
