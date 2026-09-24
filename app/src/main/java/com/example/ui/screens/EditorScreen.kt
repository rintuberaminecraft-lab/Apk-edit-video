package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectRatio
import com.example.model.PreviewLayoutStyle
import com.example.ui.components.*
import com.example.ui.theme.YouTubeRed
import com.example.viewmodel.EditorSheet
import com.example.viewmodel.EditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onBackToProjects: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val project by viewModel.project.collectAsState()
    val activeSheet by viewModel.activeSheet.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val proj = project ?: return

    var showAspectMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("editor_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = proj.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "YouTube ${proj.aspectRatio.ratioStr}",
                                fontSize = 11.sp,
                                color = YouTubeRed,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(
                                onClick = { showAspectMenu = true },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Change Aspect Ratio", tint = YouTubeRed)
                            }

                            DropdownMenu(
                                expanded = showAspectMenu,
                                onDismissRequest = { showAspectMenu = false }
                            ) {
                                AspectRatio.values().forEach { ar ->
                                    DropdownMenuItem(
                                        text = { Text("${ar.ratioStr} (${ar.label})") },
                                        onClick = {
                                            viewModel.setAspectRatio(ar)
                                            showAspectMenu = false
                                        },
                                        leadingIcon = {
                                            if (proj.aspectRatio == ar) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = YouTubeRed)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToProjects) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Projects")
                    }
                },
                actions = {
                    // Settings icon
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }

                    // Red Export Button (Prominent YouTube Studio style)
                    Button(
                        onClick = { viewModel.openSheet(EditorSheet.EXPORT) },
                        colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("header_export_button")
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Video Preview Panel (with InShot-style Keyframe toolbar on top)
                VideoPreviewCanvas(
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f)
                )

                // 2. Upper Quick ToolBar: Keyframe, Curve Graph, Delete, Split, Copy, Duplicate, Flip, Crop
                UpperQuickToolBar(
                    viewModel = viewModel
                )

                // 3. Multi-Track Timeline Panel (Undo, Redo, Play/Pause, Split, Scrubber, 50-70 layers)
                TimelinePanel(
                    viewModel = viewModel
                )

                // 4. Lower Category ToolBar: Layers, Audio, Text, Doodle, Mask, Blur, Adjust, Speed, BG, Blend, Chroma, Transition
                LowerCategoryToolBar(
                    viewModel = viewModel
                )
            }

            // Bottom Sheets Overlays
            AnimatedVisibility(
                visible = activeSheet != null && activeSheet != EditorSheet.EXPORT,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                when (activeSheet) {
                    EditorSheet.KEYFRAME_GRAPH -> KeyframeGraphSheet(viewModel = viewModel)
                    EditorSheet.MASK -> MaskEditorSheet(viewModel = viewModel)
                    EditorSheet.BLUR -> BlurEditorSheet(viewModel = viewModel)
                    EditorSheet.DOODLE -> DoodleEditorSheet(viewModel = viewModel)
                    EditorSheet.TEXT -> TextEditorSheet(viewModel = viewModel)
                    EditorSheet.AUDIO -> AudioEditorSheet(viewModel = viewModel)
                    EditorSheet.TRANSFORM -> TransformCropSheet(viewModel = viewModel)
                    EditorSheet.CROP -> TransformCropSheet(viewModel = viewModel)
                    EditorSheet.ADJUST -> AdjustEditorSheet(viewModel = viewModel)
                    EditorSheet.TRANSITION -> TransitionBlendChromaSheet(viewModel = viewModel, initialTab = 0)
                    EditorSheet.BLEND -> TransitionBlendChromaSheet(viewModel = viewModel, initialTab = 1)
                    EditorSheet.CHROMA_KEY -> TransitionBlendChromaSheet(viewModel = viewModel, initialTab = 2)
                    EditorSheet.BACKGROUND -> BackgroundSpeedSheet(viewModel = viewModel, initialTab = 0)
                    EditorSheet.SPEED -> BackgroundSpeedSheet(viewModel = viewModel, initialTab = 1)
                    EditorSheet.PIP_LAYERS -> PipLayersSheet(viewModel = viewModel)
                    else -> {}
                }
            }

            // Export Dialog
            if (activeSheet == EditorSheet.EXPORT) {
                ExportDialog(viewModel = viewModel)
            }
        }
    }
}
