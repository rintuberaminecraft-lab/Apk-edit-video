package com.example.ui.components

import androidx.compose.foundation.layout.*
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
import com.example.model.BlurConfig
import com.example.model.BlurType
import com.example.ui.theme.YouTubeRed
import com.example.viewmodel.EditorViewModel

@Composable
fun BlurEditorSheet(
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier
) {
    val selectedClip by viewModel.selectedClip.collectAsState()
    val clip = selectedClip ?: return
    val blur = clip.blur

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
                        text = "Blur & Censoring",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Mosaic, Gaussian, or Magnifier lens",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { viewModel.closeSheet() },
                    modifier = Modifier.testTag("close_blur_sheet")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3 Blur Types Selector
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(BlurType.values()) { type ->
                    val isSelected = blur.type == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.updateBlur(blur.copy(type = type)) },
                        label = {
                            Text(
                                text = type.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = YouTubeRed,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (blur.type != BlurType.NONE) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when (blur.type) {
                        BlurType.MOSAIC -> "Mosaic Block Size: ${blur.strength.toInt()}px"
                        BlurType.GAUSSIAN -> "Gaussian Radius: ${blur.strength.toInt()}"
                        BlurType.MAGNIFIER -> "Lens Bubble Radius: ${blur.strength.toInt()}dp"
                        BlurType.NONE -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = blur.strength,
                    onValueChange = { viewModel.updateBlur(blur.copy(strength = it)) },
                    valueRange = 5f..100f
                )

                if (blur.type == BlurType.MAGNIFIER) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Magnification Zoom: ${String.format("%.1f", blur.magnification)}x",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = blur.magnification,
                        onValueChange = { viewModel.updateBlur(blur.copy(magnification = it)) },
                        valueRange = 1.0f..4.0f
                    )
                }
            }
        }
    }
}
