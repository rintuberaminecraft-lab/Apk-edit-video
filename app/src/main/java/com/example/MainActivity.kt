package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.EditorViewModel

enum class AppScreen {
    PROJECTS,
    EDITOR,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: EditorViewModel = viewModel()
            val settings by viewModel.settings.collectAsState()

            var currentScreen by remember { mutableStateOf(AppScreen.PROJECTS) }
            var previousScreen by remember { mutableStateOf(AppScreen.PROJECTS) }

            MyApplicationTheme(themeSetting = settings.appTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BackHandler(enabled = currentScreen != AppScreen.PROJECTS) {
                        when (currentScreen) {
                            AppScreen.EDITOR -> currentScreen = AppScreen.PROJECTS
                            AppScreen.SETTINGS -> currentScreen = previousScreen
                            AppScreen.PROJECTS -> {}
                        }
                    }

                    Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                        when (screen) {
                            AppScreen.PROJECTS -> {
                                ProjectsScreen(
                                    viewModel = viewModel,
                                    onOpenProject = { projectId ->
                                        viewModel.loadProject(projectId)
                                        currentScreen = AppScreen.EDITOR
                                    },
                                    onOpenSettings = {
                                        previousScreen = AppScreen.PROJECTS
                                        currentScreen = AppScreen.SETTINGS
                                    }
                                )
                            }
                            AppScreen.EDITOR -> {
                                EditorScreen(
                                    viewModel = viewModel,
                                    onBackToProjects = {
                                        currentScreen = AppScreen.PROJECTS
                                    },
                                    onOpenSettings = {
                                        previousScreen = AppScreen.EDITOR
                                        currentScreen = AppScreen.SETTINGS
                                    }
                                )
                            }
                            AppScreen.SETTINGS -> {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onBack = {
                                        currentScreen = previousScreen
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(text = "Hello $name!", modifier = modifier)
}
