package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TextConfig
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.YouTubeRed
import com.example.viewmodel.EditorViewModel

@Composable
fun TextEditorSheet(
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier
) {
    val selectedClip by viewModel.selectedClip.collectAsState()
    val clip = selectedClip ?: return
    val textConfig = clip.text

    val googleFonts = listOf(
        "Roboto", "Montserrat", "Oswald", "Bebas Neue",
        "Poppins", "Playfair Display", "Space Grotesk"
    )

    var showFontImportDialog by remember { mutableStateOf(false) }

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
                        text = "Text & Typography",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Google Fonts & Custom .TTF Import",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { viewModel.closeSheet() },
                    modifier = Modifier.testTag("close_text_sheet")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Text Input Field
            OutlinedTextField(
                value = textConfig.text,
                onValueChange = { viewModel.updateText(textConfig.copy(text = it)) },
                label = { Text("Title or Caption") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("text_input_field"),
                singleLine = false,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Google Fonts & Import Font Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Typography (Google Fonts)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Import font option button
                FilledTonalButton(
                    onClick = { showFontImportDialog = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Import .TTF", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (textConfig.isCustomFont && textConfig.customFontName != null) {
                    item {
                        FilterChip(
                            selected = true,
                            onClick = { },
                            label = { Text("★ ${textConfig.customFontName}", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StudioCyan,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                items(googleFonts) { fontName ->
                    val isSelected = !textConfig.isCustomFont && textConfig.fontFamilyName == fontName
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.updateText(textConfig.copy(fontFamilyName = fontName, isCustomFont = false))
                        },
                        label = {
                            Text(
                                text = fontName,
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

            Spacer(modifier = Modifier.height(12.dp))

            // Font Size Slider
            Text(
                text = "Font Size: ${textConfig.fontSizeSp.toInt()}sp",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = textConfig.fontSizeSp,
                onValueChange = { viewModel.updateText(textConfig.copy(fontSizeSp = it)) },
                valueRange = 14f..64f
            )

            // Color & Background Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Text Color:", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.width(8.dp))
                    listOf(0xFFFFFFFF, 0xFFFF0033, 0xFFFFD600, 0xFF00E5FF, 0xFF000000).forEach { color ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .border(1.dp, Color.Gray, CircleShape)
                                .clickable { viewModel.updateText(textConfig.copy(textColor = color)) }
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Shadow", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = textConfig.hasShadow,
                        onCheckedChange = { viewModel.updateText(textConfig.copy(hasShadow = it)) }
                    )
                }
            }
        }
    }

    if (showFontImportDialog) {
        AlertDialog(
            onDismissRequest = { showFontImportDialog = false },
            title = { Text("Import Font File (.TTF / .OTF)") },
            text = {
                Column {
                    Text(
                        "Select or type a font to bundle into the YouTube editor:",
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf("Futura Bold Condensed.ttf", "Anton YouTube Heavy.ttf", "Impact Studio.ttf", "Proxima Nova Extra.otf").forEach { sampleFont ->
                        TextButton(
                            onClick = {
                                viewModel.updateText(
                                    textConfig.copy(
                                        isCustomFont = true,
                                        customFontName = sampleFont.substringBeforeLast(".")
                                    )
                                )
                                showFontImportDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📁 $sampleFont", modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFontImportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
