package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.YouTubeRed
import com.example.viewmodel.EditorSheet
import com.example.viewmodel.EditorViewModel

@Composable
fun LowerCategoryToolBar(
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("lower_category_toolbar"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CategoryToolItem(
                icon = Icons.Default.Layers,
                label = "PIP (70 L)",
                tint = StudioCyan,
                testTag = "category_pip_layers",
                onClick = { viewModel.openSheet(EditorSheet.PIP_LAYERS) }
            )

            CategoryToolItem(
                icon = Icons.Default.MusicNote,
                label = "Audio",
                testTag = "category_audio",
                onClick = { viewModel.openSheet(EditorSheet.AUDIO) }
            )

            CategoryToolItem(
                icon = Icons.Default.TextFields,
                label = "Text",
                testTag = "category_text",
                onClick = { viewModel.openSheet(EditorSheet.TEXT) }
            )

            CategoryToolItem(
                icon = Icons.Default.Brush,
                label = "Doodle",
                testTag = "category_doodle",
                onClick = { viewModel.openSheet(EditorSheet.DOODLE) }
            )

            CategoryToolItem(
                icon = Icons.Default.Animation,
                label = "Mask",
                testTag = "category_mask",
                onClick = { viewModel.openSheet(EditorSheet.MASK) }
            )

            CategoryToolItem(
                icon = Icons.Default.BlurOn,
                label = "Blur",
                testTag = "category_blur",
                onClick = { viewModel.openSheet(EditorSheet.BLUR) }
            )

            CategoryToolItem(
                icon = Icons.Default.Tune,
                label = "Adjust",
                testTag = "category_adjust",
                onClick = { viewModel.openSheet(EditorSheet.ADJUST) }
            )

            CategoryToolItem(
                icon = Icons.Default.Speed,
                label = "Speed",
                testTag = "category_speed",
                onClick = { viewModel.openSheet(EditorSheet.SPEED) }
            )

            CategoryToolItem(
                icon = Icons.Default.Wallpaper,
                label = "Canvas BG",
                testTag = "category_bg",
                onClick = { viewModel.openSheet(EditorSheet.BACKGROUND) }
            )

            CategoryToolItem(
                icon = Icons.Default.AutoFixHigh,
                label = "ChromaKey",
                testTag = "category_chroma",
                onClick = { viewModel.openSheet(EditorSheet.CHROMA_KEY) }
            )

            CategoryToolItem(
                icon = Icons.Default.Grain,
                label = "Blend",
                testTag = "category_blend",
                onClick = { viewModel.openSheet(EditorSheet.BLEND) }
            )

            CategoryToolItem(
                icon = Icons.Default.SwapHoriz,
                label = "Transition",
                testTag = "category_transition",
                onClick = { viewModel.openSheet(EditorSheet.TRANSITION) }
            )

            CategoryToolItem(
                icon = Icons.Default.Transform,
                label = "Transform",
                testTag = "category_transform",
                onClick = { viewModel.openSheet(EditorSheet.TRANSFORM) }
            )
        }
    }
}

@Composable
private fun CategoryToolItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    testTag: String = ""
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(22.dp), tint = tint)
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
