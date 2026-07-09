package com.lianpo.clock.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
            TopAppBar(title = { Text("设置") })
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
                customSounds = uiState.customSounds,
                onSoundSelected = { viewModel.updateSelectedSound(it) },
                onImportSound = { viewModel.importSound(it) },
                onDeleteSound = { viewModel.deleteCustomSound(it) },
                onTestSound = { viewModel.testSound(it) }
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "计时器设置", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            SettingSlider(label = "工作时长", value = workDuration, valueRange = 1..60, onValueChange = onWorkDurationChange, suffix = "分钟")
            SettingSlider(label = "短休息时长", value = shortBreakDuration, valueRange = 1..30, onValueChange = onShortBreakDurationChange, suffix = "分钟")
            SettingSlider(label = "长休息时长", value = longBreakDuration, valueRange = 1..60, onValueChange = onLongBreakDurationChange, suffix = "分钟")
            SettingSlider(label = "长休息间隔", value = longBreakInterval, valueRange = 2..10, onValueChange = onLongBreakIntervalChange, suffix = "个番茄钟")
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
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(text = "$value $suffix", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
            steps = valueRange.last - valueRange.first - 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SoundSettingsSection(
    selectedSound: String,
    customSounds: List<Pair<String, String>>,
    onSoundSelected: (String) -> Unit,
    onImportSound: (Uri) -> Unit,
    onDeleteSound: (String) -> Unit,
    onTestSound: (String) -> Unit
) {
    val defaultSounds = listOf(
        "default" to "默认",
        "crisp" to "清脆",
        "gentle" to "柔和"
    )

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImportSound(it) }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "声音设置", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "内置铃声", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                defaultSounds.forEach { (key, displayName) ->
                    FilterChip(
                        selected = selectedSound == key,
                        onClick = { onSoundSelected(key) },
                        label = { Text(displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "自定义铃声", style = MaterialTheme.typography.bodyLarge)
                IconButton(onClick = { launcher.launch("audio/*") }) {
                    Icon(Icons.Default.Add, contentDescription = "导入铃声")
                }
            }

            if (customSounds.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                customSounds.forEach { (key, displayName) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = selectedSound == key,
                            onClick = { onSoundSelected(key) },
                            label = { Text(displayName) },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onTestSound(key) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "试听", modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { onDeleteSound(key) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            } else {
                Text(
                    text = "点击 + 导入自定义铃声",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "通知设置", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "启用通知", style = MaterialTheme.typography.bodyLarge)
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}