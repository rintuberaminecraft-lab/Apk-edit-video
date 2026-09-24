package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppSettings
import com.example.model.AppThemeSetting
import com.example.model.PreviewLayoutStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("tubecut_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        val layoutName = prefs.getString("layout_style", PreviewLayoutStyle.SHORT_VERTICAL.name)
        val themeName = prefs.getString("app_theme", AppThemeSetting.DARK.name)
        val lossless = prefs.getBoolean("lossless_decode", true)

        val layout = try {
            PreviewLayoutStyle.valueOf(layoutName ?: PreviewLayoutStyle.SHORT_VERTICAL.name)
        } catch (e: Exception) {
            PreviewLayoutStyle.SHORT_VERTICAL
        }

        val theme = try {
            AppThemeSetting.valueOf(themeName ?: AppThemeSetting.DARK.name)
        } catch (e: Exception) {
            AppThemeSetting.DARK
        }

        return AppSettings(
            previewLayoutStyle = layout,
            appTheme = theme,
            losslessDecodeEnabled = lossless
        )
    }

    fun updateSettings(newSettings: AppSettings) {
        prefs.edit()
            .putString("layout_style", newSettings.previewLayoutStyle.name)
            .putString("app_theme", newSettings.appTheme.name)
            .putBoolean("lossless_decode", newSettings.losslessDecodeEnabled)
            .apply()
        _settings.value = newSettings
    }
}
