package com.lianpo.clock.ui.privatetracker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivateScreen(
    onBack: () -> Unit,
    viewModel: PrivateViewModel = hiltViewModel()
) {
    val todayCount by viewModel.todayCount.collectAsState()
    val weekCount by viewModel.weekCount.collectAsState()
    val monthCount by viewModel.monthCount.collectAsState()
    val yearCount by viewModel.yearCount.collectAsState()
    val recentRecords by viewModel.recentRecords.collectAsState()
    val weeklyData by viewModel.weeklyData.collectAsState()
    val monthlyData by viewModel.monthlyData.collectAsState()
    val dateFormat = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }
    val dayFormat = remember { SimpleDateFormat("MM/dd", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🦌管统计") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.addRecord() },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("记录一次") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                // 频率统计
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "频率分析",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = viewModel.getFrequency(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            item {
                // 统计卡片 2x2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("今日", todayCount, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    StatCard("本周", weekCount, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("本月", monthCount, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                    StatCard("本年", yearCount, MaterialTheme.colorScheme.error, Modifier.weight(1f))
                }
            }

            // 本周柱状图
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "本周趋势",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        WeeklyBarChart(
                            data = weeklyData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                        )
                    }
                }
            }

            // 本月曲线图
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "本月趋势",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        MonthlyLineChart(
                            data = monthlyData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                        )
                    }
                }
            }

            // 最近记录
            item {
                Text(
                    text = "最近记录",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (recentRecords.isEmpty()) {
                item {
                    Text(
                        text = "暂无记录，点击右下角按钮开始记录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(recentRecords) { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = dateFormat.format(Date(record.timestamp)),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                val cal = Calendar.getInstance().apply { timeInMillis = record.timestamp }
                                val hour = cal.get(Calendar.HOUR_OF_DAY)
                                val timeOfDay = when {
                                    hour < 6 -> "凌晨"
                                    hour < 12 -> "上午"
                                    hour < 18 -> "下午"
                                    else -> "晚上"
                                }
                                Text(
                                    text = timeOfDay,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { viewModel.deleteRecord(record) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "删除",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WeeklyBarChart(
    data: List<DailyCount>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    val density = LocalDensity.current
    val textSize = with(density) { 10.sp.toPx() }
    val primaryColor = MaterialTheme.colorScheme.primary
    val maxCount = data.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    val dayLabels = listOf("一", "二", "三", "四", "五", "六", "日")

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val padding = 30.dp.toPx()
        val chartWidth = canvasWidth - padding * 2
        val chartHeight = canvasHeight - padding - 20.dp.toPx()
        val barWidth = chartWidth / data.size * 0.6f
        val barSpacing = chartWidth / data.size

        data.forEachIndexed { index, daily ->
            val barHeight = chartHeight * daily.count / maxCount
            val x = padding + barSpacing * index + (barSpacing - barWidth) / 2
            val y = canvasHeight - 20.dp.toPx() - barHeight

            drawRect(
                color = primaryColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight)
            )

            drawContext.canvas.nativeCanvas.drawText(
                if (index < dayLabels.size) dayLabels[index] else "",
                x + barWidth / 2,
                canvasHeight - 5.dp.toPx(),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    this.textSize = textSize
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )

            if (daily.count > 0) {
                drawContext.canvas.nativeCanvas.drawText(
                    "${daily.count}",
                    x + barWidth / 2,
                    y - 5.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = primaryColor.hashCode()
                        this.textSize = textSize
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
    }
}

@Composable
private fun MonthlyLineChart(
    data: List<DailyCount>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    val density = LocalDensity.current
    val textSize = with(density) { 9.sp.toPx() }
    val lineColor = MaterialTheme.colorScheme.tertiary
    val maxCount = data.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val padding = 30.dp.toPx()
        val chartWidth = canvasWidth - padding * 2
        val chartHeight = canvasHeight - padding - 20.dp.toPx()
        val pointSpacing = if (data.size > 1) chartWidth / (data.size - 1) else chartWidth

        val path = Path()
        data.forEachIndexed { index, daily ->
            val x = padding + pointSpacing * index
            val y = canvasHeight - 20.dp.toPx() - (chartHeight * daily.count / maxCount)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }

            drawCircle(
                color = lineColor,
                radius = 3.dp.toPx(),
                center = Offset(x, y)
            )

            if (index % 5 == 0 || index == data.size - 1) {
                drawContext.canvas.nativeCanvas.drawText(
                    "${index + 1}",
                    x,
                    canvasHeight - 5.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        this.textSize = textSize
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}