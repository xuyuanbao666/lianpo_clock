package com.lianpo.clock.ui.timer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lianpo.clock.ui.theme.BreakGreen
import com.lianpo.clock.ui.theme.TomatoRed
import com.lianpo.clock.ui.timer.components.CircularProgress
import com.lianpo.clock.ui.timer.components.TimeDisplay
import com.lianpo.clock.ui.timer.components.TimerControls
import com.lianpo.clock.util.TimerState
import com.lianpo.clock.util.TimerType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    viewModel: TimerViewModel = hiltViewModel()
) {
    val timerState by viewModel.timerState.collectAsState()
    val timerType by viewModel.timerType.collectAsState()
    val timeRemaining by viewModel.timeRemaining.collectAsState()
    val completedPomodoros by viewModel.completedPomodoros.collectAsState()
    val currentTask by viewModel.currentTask.collectAsState()
    val activeTasks by viewModel.activeTasks.collectAsState()
    val showTaskSelector by viewModel.showTaskSelector.collectAsState()

    val progressColor = when (timerType) {
        TimerType.WORK -> TomatoRed
        TimerType.SHORT_BREAK, TimerType.LONG_BREAK -> BreakGreen
    }

    // 任务选择对话框
    if (showTaskSelector) {
        AlertDialog(
            onDismissRequest = { viewModel.hideTaskSelector() },
            title = { Text("选择任务") },
            text = {
                if (activeTasks.isEmpty()) {
                    Text("暂无进行中的任务，请先在任务页面创建任务")
                } else {
                    LazyColumn {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectTask(null) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("不关联任务", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                        items(activeTasks) { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectTask(task) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(task.title, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        "番茄钟 ${task.pomodoroCount}/${task.targetPomodoroCount}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (currentTask?.id == task.id) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.hideTaskSelector() }) {
                    Text("关闭")
                }
            }
        )
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // 当前任务显示
                Card(
                    onClick = { viewModel.showTaskSelector() },
                    modifier = Modifier.padding(horizontal = 32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentTask?.title ?: "点击选择任务",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (currentTask != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { viewModel.selectTask(null) },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "取消关联",
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "已完成 $completedPomodoros 个番茄钟",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                Box(contentAlignment = Alignment.Center) {
                    CircularProgress(
                        progress = viewModel.getProgress(),
                        progressColor = progressColor
                    )
                    TimeDisplay(
                        timeText = viewModel.formatTime(timeRemaining),
                        timerType = timerType
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                TimerControls(
                    timerState = timerState,
                    onStartPause = {
                        if (timerState == TimerState.RUNNING) {
                            viewModel.pauseTimer()
                        } else {
                            viewModel.startTimer()
                        }
                    },
                    onReset = { viewModel.resetTimer() },
                    onSkip = { viewModel.skipToNext() }
                )
            }
        }
    }
}