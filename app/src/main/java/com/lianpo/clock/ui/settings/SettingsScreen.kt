package com.lianpo.clock.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            TimerSettingsSection(
                workDuration = uiState.workDuration,
                shortBreakDuration = uiState.shortBreakDuration,
                longBreakDuration = uiState.longBreakDuration,
                longBreakInterval = uiState.longBreakInterval,
                onWorkDurationChange = { viewModel.updateWorkDuration(it); viewModel.saveSettings() },
                onShortBreakDurationChange = { viewModel.updateShortBreakDuration(it); viewModel.saveSettings() },
                onLongBreakDurationChange = { viewModel.updateLongBreakDuration(it); viewModel.saveSettings() },
                onLongBreakIntervalChange = { viewModel.updateLongBreakInterval(it); viewModel.saveSettings() }
            )

            SoundSettingsSection(
                selectedSound = uiState.selectedSound,
                onSoundSelected = { viewModel.updateSelectedSound(it); viewModel.saveSettings() }
            )

            NotificationSettingsSection(
                enabled = uiState.notificationsEnabled,
                onToggle = { viewModel.updateNotificationsEnabled(it); viewModel.saveSettings() }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TimerSettingsSection(
    workDuration: Int,
    shortBreakDuration: Int,
    longBreakDuration: Int,
    longBreakInterval: Int,
    onWorkDurationChange: (Int) -> Unit,
    onShortBreakDurationChange: (Int) -> Unit,
    onLongBreakDurationChange: (Int) -> Unit,
    onLongBreakIntervalChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "计时器设置",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingSlider(
                label = "工作时长",
                value = workDuration,
                valueRange = 1..60,
                onValueChange = onWorkDurationChange,
                suffix = "分钟"
            )

            SettingSlider(
                label = "短休息时长",
                value = shortBreakDuration,
                valueRange = 1..30,
                onValueChange = onShortBreakDurationChange,
                suffix = "分钟"
            )

            SettingSlider(
                label = "长休息时长",
                value = longBreakDuration,
                valueRange = 1..60,
                onValueChange = onLongBreakDurationChange,
                suffix = "分钟"
            )

            SettingSlider(
                label = "长休息间隔",
                value = longBreakInterval,
                valueRange = 2..10,
                onValueChange = onLongBreakIntervalChange,
                suffix = "个番茄钟"
            )
        }
    }
}

@Composable
private fun SettingSlider(
    label: String,
    value: Int,
    valueRange: IntRange,
    onValueChange: (Int) -> Unit,
    suffix: String
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "$value $suffix",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
            steps = valueRange.last - valueRange.first - 1
        )
    }
}

@Composable
private fun SoundSettingsSection(
    selectedSound: String,
    onSoundSelected: (String) -> Unit
) {
    val sounds = listOf(
        "default" to "默认",
        "crisp" to "清脆",
        "soft" to "柔和"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "声音设置",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "提示音",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sounds.forEach { (key, displayName) ->
                    FilterChip(
                        selected = selectedSound == key,
                        onClick = { onSoundSelected(key) },
                        label = { Text(displayName) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationSettingsSection(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "通知设置",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "启用通知",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle
            )
        }
    }
}
