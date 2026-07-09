package com.lianpo.clock.ui.privatetracker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

private val MOODS = listOf(
    "" to "无",
    "😌" to "平静",
    "😊" to "开心",
    "😏" to "满足",
    "🔥" to "兴奋",
    "😴" to "疲惫",
    "😤" to "烦躁",
    "🥰" to "幸福"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PrivateScreen(
    onBack: () -> Unit,
    viewModel: PrivateViewModel = hiltViewModel()
) {
    val todayCount by viewModel.todayCount.collectAsState()
    val weekCount by viewModel.weekCount.collectAsState()
    val monthCount by viewModel.monthCount.collectAsState()
    val yearCount by viewModel.yearCount.collectAsState()
    val selectedMonthRecords by viewModel.selectedMonthRecords.collectAsState()
    val selectedMonthCount by viewModel.selectedMonthCount.collectAsState()
    val selectedDayRecords by viewModel.selectedDayRecords.collectAsState()
    val weeklyData by viewModel.weeklyData.collectAsState()
    val monthlyData by viewModel.monthlyData.collectAsState()
    val memos by viewModel.memos.collectAsState()
    val showAddMemo by viewModel.showAddMemo.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val dateFormat = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }
    val fullDateFormat = remember { SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault()) }
    val dayTimeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    var memoText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedRecord by remember { mutableStateOf<com.lianpo.clock.data.database.entity.PrivateRecord?>(null) }
    var showMemoInput by remember { mutableStateOf(false) }
    var recordMemoText by remember { mutableStateOf("") }

    // 添加备忘录对话框
    if (showAddMemo) {
        AlertDialog(
            onDismissRequest = { viewModel.hideAddMemoDialog() },
            title = { Text("添加备忘录") },
            text = {
                OutlinedTextField(
                    value = memoText,
                    onValueChange = { memoText = it },
                    placeholder = { Text("记录你的想法...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (memoText.isNotBlank()) {
                        viewModel.addMemo(memoText)
                        memoText = ""
                    }
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideAddMemoDialog() }) { Text("取消") }
            }
        )
    }

    // 记录详情弹窗
    selectedRecord?.let { record ->
        AlertDialog(
            onDismissRequest = { selectedRecord = null },
            title = {
                Column {
                    Text(text = fullDateFormat.format(Date(record.timestamp)), style = MaterialTheme.typography.titleMedium)
                    if (record.mood.isNotEmpty()) {
                        Text(text = "心情：${record.mood}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            text = {
                Column {
                    Text(text = "选择心情", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        MOODS.drop(1).forEach { (emoji, name) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.updateRecordMood(record, emoji)
                                        selectedRecord = record.copy(mood = emoji)
                                    }
                                    .padding(4.dp)
                            ) {
                                Text(text = emoji, fontSize = 24.sp)
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (record.mood == emoji) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "备忘录", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 4.dp))
                    OutlinedTextField(
                        value = if (showMemoInput) recordMemoText else record.memo,
                        onValueChange = { recordMemoText = it },
                        placeholder = { Text("添加备注...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        readOnly = !showMemoInput
                    )
                    if (!showMemoInput) {
                        TextButton(
                            onClick = { recordMemoText = record.memo; showMemoInput = true },
                            modifier = Modifier.align(Alignment.End)
                        ) { Text(if (record.memo.isEmpty()) "添加备注" else "编辑备注") }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { showMemoInput = false }) { Text("取消") }
                            TextButton(onClick = {
                                viewModel.updateRecordMemo(record, recordMemoText)
                                selectedRecord = record.copy(memo = recordMemoText)
                                showMemoInput = false
                            }) { Text("保存") }
                        }
                    }
                }
            },
            confirmButton = { },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.deleteRecord(record); selectedRecord = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("删除记录")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🦌管统计") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "返回") } },
                actions = { IconButton(onClick = { viewModel.showAddMemoDialog() }) { Icon(Icons.Default.Add, contentDescription = "添加备忘录") } }
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "频率分析", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = viewModel.getFrequency(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("今日", todayCount, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    StatCard("本周", weekCount, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                    StatCard("本月", monthCount, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("本年", yearCount, MaterialTheme.colorScheme.error, Modifier.weight(1f))
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            item {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("日历") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("图表") })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("备忘录") })
                }
            }

            when (selectedTab) {
                0 -> {
                    // 月份选择器 + 日历
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // 月份切换
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { viewModel.previousMonth() }) {
                                        Icon(Icons.Default.ChevronLeft, contentDescription = "上个月")
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = viewModel.getMonthName(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text(text = "共 $selectedMonthCount 次", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(onClick = { viewModel.nextMonth() }) {
                                        Icon(Icons.Default.ChevronRight, contentDescription = "下个月")
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // 日历头部
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    listOf("一", "二", "三", "四", "五", "六", "日").forEach { day ->
                                        Text(
                                            text = day,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // 日历网格
                                CalendarGrid(
                                    year = viewModel.selectedYear.collectAsState().value,
                                    month = viewModel.selectedMonth.collectAsState().value,
                                    records = selectedMonthRecords,
                                    selectedDay = selectedDay,
                                    onDayClick = { day -> viewModel.selectDay(day) }
                                )
                            }
                        }
                    }

                    // 选中日期的记录
                    if (selectedDay != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${viewModel.selectedYear.collectAsState().value}年${viewModel.selectedMonth.collectAsState().value + 1}月${selectedDay}日",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${selectedDayRecords.size} 次",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (selectedDayRecords.isEmpty()) {
                                        Text(
                                            text = "当天暂无记录",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                        )
                                    } else {
                                        selectedDayRecords.forEach { record ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { selectedRecord = record }
                                                    .padding(vertical = 6.dp, horizontal = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (record.mood.isNotEmpty()) {
                                                        Text(text = record.mood, fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
                                                    }
                                                    Text(
                                                        text = dayTimeFormat.format(Date(record.timestamp)),
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                                if (record.memo.isNotEmpty()) {
                                                    Text(
                                                        text = record.memo,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.widthIn(max = 120.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "本周趋势", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                WeeklyBarChart(data = weeklyData, modifier = Modifier.fillMaxWidth().height(150.dp))
                            }
                        }
                    }
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "本月趋势", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                MonthlyLineChart(data = monthlyData, modifier = Modifier.fillMaxWidth().height(150.dp))
                            }
                        }
                    }
                }
                2 -> {
                    if (memos.isEmpty()) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text(text = "点击右上角 + 添加备忘录", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else {
                        items(memos) { memo ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (memo.isPinned) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.Top) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = memo.content, style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = dateFormat.format(Date(memo.timestamp)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(onClick = { viewModel.togglePinMemo(memo) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.PushPin, contentDescription = "置顶", tint = if (memo.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = { viewModel.deleteMemo(memo) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun CalendarGrid(
    year: Int,
    month: Int,
    records: List<com.lianpo.clock.data.database.entity.PrivateRecord>,
    selectedDay: Int?,
    onDayClick: (Int) -> Unit
) {
    val cal = Calendar.getInstance()
    cal.set(year, month, 1)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // 转换为周一开始

    // 统计每天的记录数和心情
    val dayRecordsMap = records.groupBy { record ->
        val c = Calendar.getInstance().apply { timeInMillis = record.timestamp }
        c.get(Calendar.DAY_OF_MONTH)
    }

    val today = Calendar.getInstance()
    val isCurrentMonth = today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month
    val todayDay = today.get(Calendar.DAY_OF_MONTH)

    val primaryColor = MaterialTheme.colorScheme.primary
    val selectedBgColor = MaterialTheme.colorScheme.primaryContainer
    val todayBorderColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    Column {
        var dayCounter = 1
        for (week in 0 until 6) {
            if (dayCounter > daysInMonth) break
            Row(modifier = Modifier.fillMaxWidth()) {
                for (dayOfWeek in 0 until 7) {
                    if (week == 0 && dayOfWeek < firstDayOfWeek) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else if (dayCounter <= daysInMonth) {
                        val day = dayCounter
                        val dayRecordList = dayRecordsMap[day] ?: emptyList()
                        val count = dayRecordList.size
                        val mainMood = dayRecordList.firstOrNull { it.mood.isNotEmpty() }?.mood
                        val isSelected = selectedDay == day
                        val isToday = isCurrentMonth && day == todayDay

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        isSelected -> selectedBgColor
                                        count > 0 -> surfaceVariantColor.copy(alpha = 0.5f)
                                        else -> Color.Transparent
                                    }
                                )
                                .then(
                                    if (isToday) Modifier.background(Color.Transparent, CircleShape)
                                        .padding(1.dp)
                                        .background(Color.Transparent, CircleShape)
                                    else Modifier
                                )
                                .clickable { onDayClick(day) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "$day",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                        isToday -> primaryColor
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                if (mainMood != null) {
                                    Text(text = mainMood, fontSize = 10.sp)
                                } else if (count > 0) {
                                    Text(
                                        text = "$count",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        dayCounter++
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "$count", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun WeeklyBarChart(data: List<DailyCount>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) return
    val density = LocalDensity.current
    val textSize = with(density) { 10.sp.toPx() }
    val primaryColor = MaterialTheme.colorScheme.primary
    val maxCount = data.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    val dayLabels = listOf("一", "二", "三", "四", "五", "六", "日")

    Canvas(modifier = modifier) {
        val padding = 30.dp.toPx()
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding - 20.dp.toPx()
        val barWidth = chartWidth / data.size * 0.6f
        val barSpacing = chartWidth / data.size

        data.forEachIndexed { index, daily ->
            val barHeight = chartHeight * daily.count / maxCount
            val x = padding + barSpacing * index + (barSpacing - barWidth) / 2
            val y = size.height - 20.dp.toPx() - barHeight

            drawRect(color = primaryColor, topLeft = Offset(x, y), size = Size(barWidth, barHeight))
            drawContext.canvas.nativeCanvas.drawText(
                if (index < dayLabels.size) dayLabels[index] else "",
                x + barWidth / 2, size.height - 5.dp.toPx(),
                android.graphics.Paint().apply { color = android.graphics.Color.GRAY; this.textSize = textSize; textAlign = android.graphics.Paint.Align.CENTER }
            )
            if (daily.count > 0) {
                drawContext.canvas.nativeCanvas.drawText(
                    "${daily.count}", x + barWidth / 2, y - 5.dp.toPx(),
                    android.graphics.Paint().apply { color = primaryColor.hashCode(); this.textSize = textSize; textAlign = android.graphics.Paint.Align.CENTER }
                )
            }
        }
    }
}

@Composable
private fun MonthlyLineChart(data: List<DailyCount>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) return
    val density = LocalDensity.current
    val textSize = with(density) { 9.sp.toPx() }
    val lineColor = MaterialTheme.colorScheme.tertiary
    val maxCount = data.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1

    Canvas(modifier = modifier) {
        val padding = 30.dp.toPx()
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding - 20.dp.toPx()
        val pointSpacing = if (data.size > 1) chartWidth / (data.size - 1) else chartWidth

        val path = Path()
        data.forEachIndexed { index, daily ->
            val x = padding + pointSpacing * index
            val y = size.height - 20.dp.toPx() - (chartHeight * daily.count / maxCount)

            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawCircle(color = lineColor, radius = 3.dp.toPx(), center = Offset(x, y))

            if (index % 5 == 0 || index == data.size - 1) {
                drawContext.canvas.nativeCanvas.drawText(
                    "${index + 1}", x, size.height - 5.dp.toPx(),
                    android.graphics.Paint().apply { color = android.graphics.Color.GRAY; this.textSize = textSize; textAlign = android.graphics.Paint.Align.CENTER }
                )
            }
        }
        drawPath(path = path, color = lineColor, style = Stroke(width = 2.dp.toPx()))
    }
}