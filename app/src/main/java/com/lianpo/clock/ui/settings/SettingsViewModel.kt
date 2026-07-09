package com.lianpo.clock.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.lianpo.clock.util.SoundPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SettingsUiState(
    val workDuration: Int = 25,
    val shortBreakDuration: Int = 5,
    val longBreakDuration: Int = 15,
    val longBreakInterval: Int = 4,
    val selectedSound: String = "default",
    val notificationsEnabled: Boolean = true,
    val customSounds: List<Pair<String, String>> = emptyList()
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val soundPlayer: SoundPlayer
) : ViewModel() {

    companion object {
        private const val PREFS_NAME = "lianpo_clock_settings"
        private const val KEY_WORK_DURATION = "work_duration"
        private const val KEY_SHORT_BREAK_DURATION = "short_break_duration"
        private const val KEY_LONG_BREAK_DURATION = "long_break_duration"
        private const val KEY_LONG_BREAK_INTERVAL = "long_break_interval"
        private const val KEY_SELECTED_SOUND = "selected_sound"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        loadCustomSounds()
    }

    fun updateWorkDuration(value: Int) {
        _uiState.value = _uiState.value.copy(workDuration = value)
    }

    fun updateShortBreakDuration(value: Int) {
        _uiState.value = _uiState.value.copy(shortBreakDuration = value)
    }

    fun updateLongBreakDuration(value: Int) {
        _uiState.value = _uiState.value.copy(longBreakDuration = value)
    }

    fun updateLongBreakInterval(value: Int) {
        _uiState.value = _uiState.value.copy(longBreakInterval = value)
    }

    fun updateSelectedSound(value: String) {
        _uiState.value = _uiState.value.copy(selectedSound = value)
        saveSettings()
    }

    fun updateNotificationsEnabled(value: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = value)
    }

    fun importSound(uri: Uri) {
        val fileName = soundPlayer.importSound(uri)
        if (fileName != null) {
            loadCustomSounds()
            // 自动选择新导入的铃声
            updateSelectedSound("custom_$fileName")
        }
    }

    fun deleteCustomSound(soundKey: String) {
        val fileName = soundKey.removePrefix("custom_")
        soundPlayer.deleteCustomSound(fileName)
        loadCustomSounds()
        // 如果删除的是当前选中的铃声，切换回默认
        if (_uiState.value.selectedSound == soundKey) {
            updateSelectedSound("default")
        }
    }

    fun testSound(soundType: String) {
        soundPlayer.playSound(soundType)
    }

    private fun loadCustomSounds() {
        _uiState.value = _uiState.value.copy(
            customSounds = soundPlayer.getCustomSounds()
        )
    }

    fun saveSettings() {
        prefs.edit().apply {
            putInt(KEY_WORK_DURATION, _uiState.value.workDuration)
            putInt(KEY_SHORT_BREAK_DURATION, _uiState.value.shortBreakDuration)
            putInt(KEY_LONG_BREAK_DURATION, _uiState.value.longBreakDuration)
            putInt(KEY_LONG_BREAK_INTERVAL, _uiState.value.longBreakInterval)
            putString(KEY_SELECTED_SOUND, _uiState.value.selectedSound)
            putBoolean(KEY_NOTIFICATIONS_ENABLED, _uiState.value.notificationsEnabled)
            apply()
        }
    }

    private fun loadSettings() {
        _uiState.value = SettingsUiState(
            workDuration = prefs.getInt(KEY_WORK_DURATION, 25),
            shortBreakDuration = prefs.getInt(KEY_SHORT_BREAK_DURATION, 5),
            longBreakDuration = prefs.getInt(KEY_LONG_BREAK_DURATION, 15),
            longBreakInterval = prefs.getInt(KEY_LONG_BREAK_INTERVAL, 4),
            selectedSound = prefs.getString(KEY_SELECTED_SOUND, "default") ?: "default",
            notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        )
    }
}