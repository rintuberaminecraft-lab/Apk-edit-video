package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KeyframeAmber
import com.example.ui.theme.YouTubeRed
import com.example.viewmodel.EditorSheet
import com.example.viewmodel.EditorViewModel

@Composable
fun UpperQuickToolBar(
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier
) {
    val selectedClip by viewModel.selectedClip.collectAsState()
    val hasKeyframe by viewModel.hasKeyframeAtPlayhead.collectAsState()
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("upper_quick_toolbar"),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 1. Keyframe Button (Always visible at first in tools options)
            QuickToolButton(
                icon = if (hasKeyframe) Icons.Default.Check else Icons.Default.Diamond,
                label = if (hasKeyframe) "Keyframe ✓" else "+ Keyframe",
                color = if (hasKeyframe) KeyframeAmber else MaterialTheme.colorScheme.onSurface,
                onClick = { viewModel.toggleKeyframeAtPlayhead() },
                testTag = "quick_tool_keyframe"
            )

            // 2. Keyframe Graph (Custom Bezier handles)
            QuickToolButton(
                icon = Icons.Default.Timeline,
                label = "Curve Graph",
                color = KeyframeAmber,
                onClick = { viewModel.openSheet(EditorSheet.KEYFRAME_GRAPH) },
                testTag = "quick_tool_graph"
            )

            // 3. Delete
            QuickToolButton(
                icon = Icons.Default.Delete,
                label = "Delete",
                color = YouTubeRed,
                onClick = { viewModel.deleteClip() },
                testTag = "quick_tool_delete"
            )

            // 4. Split
            QuickToolButton(
                icon = Icons.Default.ContentCut,
                label = "Split",
                onClick = { viewModel.splitClip() },
                testTag = "quick_tool_split"
            )

            // 5. Copy
            QuickToolButton(
                icon = Icons.Default.ContentCopy,
                label = "Copy",
                onClick = { viewModel.duplicateClip() },
                testTag = "quick_tool_copy"
            )

            // 6. Duplicate
            QuickToolButton(
                icon = Icons.Default.ControlPointDuplicate,
                label = "Duplicate",
                onClick = { viewModel.duplicateClip() },
                testTag = "quick_tool_duplicate"
            )

            // 7. Flip
            QuickToolButton(
                icon = Icons.Default.Flip,
                label = "Flip",
                onClick = { viewModel.flipHorizontal() },
                testTag = "quick_tool_flip"
            )

            // 8. Crop
            QuickToolButton(
                icon = Icons.Default.Crop,
                label = "Crop",
                onClick = { viewModel.openSheet(EditorSheet.CROP) },
                testTag = "quick_tool_crop"
            )
        }
    }
}

@Composable
private fun QuickToolButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurface,
    testTag: String = ""
) {
    FilledTonalButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        modifier = Modifier
            .height(34.dp)
            .testTag(testTag),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = color
        )
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(15.dp), tint = color)
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}
