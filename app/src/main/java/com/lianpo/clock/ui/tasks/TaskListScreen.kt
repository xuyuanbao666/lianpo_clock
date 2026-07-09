package com.lianpo.clock.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lianpo.clock.ui.tasks.components.AddTaskDialog
import com.lianpo.clock.ui.tasks.components.TaskItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel = hiltViewModel()
) {
    val tasks by viewModel.tasks.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val filterMode by viewModel.filterMode.collectAsState()
    val filteredTasks = remember(tasks, filterMode) { viewModel.getFilteredTasks(tasks) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "添加任务")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 筛选栏
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // 月份选择（仅在按月份筛选时显示）
                    if (filterMode == FilterMode.THIS_MONTH) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.previousMonth() }) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "上个月")
                            }
                            Text(
                                text = viewModel.getMonthName(),
                                style = MaterialTheme.typography.titleSmall
                            )
                            IconButton(onClick = { viewModel.nextMonth() }) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "下个月")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // 筛选模式选择
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filterMode == FilterMode.ALL,
                            onClick = { viewModel.setFilterMode(FilterMode.ALL) },
                            label = { Text("全部") }
                        )
                        FilterChip(
                            selected = filterMode == FilterMode.THIS_MONTH,
                            onClick = { viewModel.setFilterMode(FilterMode.THIS_MONTH) },
                            label = { Text("按月份") }
                        )
                        FilterChip(
                            selected = filterMode == FilterMode.ACTIVE,
                            onClick = { viewModel.setFilterMode(FilterMode.ACTIVE) },
                            label = { Text("进行中") }
                        )
                        FilterChip(
                            selected = filterMode == FilterMode.COMPLETED,
                            onClick = { viewModel.setFilterMode(FilterMode.COMPLETED) },
                            label = { Text("已完成") }
                        )
                    }

                    // 显示任务数量
                    Text(
                        text = "共 ${filteredTasks.size} 个任务",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // 任务列表
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = when (filterMode) {
                                FilterMode.ALL -> "暂无任务"
                                FilterMode.THIS_MONTH -> "本月暂无任务"
                                FilterMode.ACTIVE -> "暂无进行中的任务"
                                FilterMode.COMPLETED -> "暂无已完成的任务"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "点击右下角按钮添加新任务",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskItem(
                            task = task,
                            onToggleCompletion = { viewModel.toggleTaskCompletion(it) },
                            onDelete = { viewModel.deleteTask(it) },
                            onClick = { viewModel.selectTask(it) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            onConfirm = { title, description, targetPomodoroCount ->
                viewModel.addTask(title, description, targetPomodoroCount)
            },
            onDismiss = { viewModel.dismissAddDialog() }
        )
    }
}