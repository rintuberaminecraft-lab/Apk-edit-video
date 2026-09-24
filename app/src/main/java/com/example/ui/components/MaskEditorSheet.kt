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
import com.example.model.MaskConfig
import com.example.model.MaskType
import com.example.ui.theme.YouTubeRed
import com.example.viewmodel.EditorViewModel

@Composable
fun MaskEditorSheet(
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier
) {
    val selectedClip by viewModel.selectedClip.collectAsState()
    val clip = selectedClip ?: return
    val mask = clip.mask

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
                        text = "Mask Effect",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Cutout shape for ${clip.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { viewModel.closeSheet() },
                    modifier = Modifier.testTag("close_mask_sheet")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5 Mask Types Selector
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(MaskType.values()) { type ->
                    val isSelected = mask.type == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.updateMask(mask.copy(type = type)) },
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

            if (mask.type != MaskType.NONE) {
                Spacer(modifier = Modifier.height(16.dp))

                // Invert Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Invert Mask",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = mask.inverted,
                        onCheckedChange = { viewModel.updateMask(mask.copy(inverted = it)) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Feather Slider
                Text(
                    text = "Feather (Soft Edge): ${mask.feather.toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = mask.feather,
                    onValueChange = { viewModel.updateMask(mask.copy(feather = it)) },
                    valueRange = 0f..100f
                )

                // Split Rectangle specific slider
                if (mask.type == MaskType.SPLIT_RECTANGLE) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Split Gap: ${mask.splitGap.toInt()}dp",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = mask.splitGap,
                        onValueChange = { viewModel.updateMask(mask.copy(splitGap = it)) },
                        valueRange = 0f..60f
                    )
                }

                // Rounded Rectangle specific slider
                if (mask.type == MaskType.ROUNDED_RECTANGLE) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Corner Radius: ${mask.cornerRadius.toInt()}dp",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = mask.cornerRadius,
                        onValueChange = { viewModel.updateMask(mask.copy(cornerRadius = it)) },
                        valueRange = 0f..64f
                    )
                }
            }
        }
    }
}
