package com.lianpo.clock.ui.statistics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lianpo.clock.ui.statistics.DailyStats
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DailyChart(
    weeklyStats: List<DailyStats>,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val textSize = with(density) { 10.sp.toPx() }
    val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val padding = 40.dp.toPx()
        val chartWidth = canvasWidth - padding * 2
        val chartHeight = canvasHeight - padding * 2

        val maxMinutes = weeklyStats.maxOfOrNull { it.focusMinutes } ?: 1
        val barWidth = chartWidth / 7 * 0.6f
        val barSpacing = chartWidth / 7

        drawLine(
            color = Color.Gray,
            start = Offset(padding, padding),
            end = Offset(padding, canvasHeight - padding),
            strokeWidth = 1.dp.toPx()
        )

        drawLine(
            color = Color.Gray,
            start = Offset(padding, canvasHeight - padding),
            end = Offset(canvasWidth - padding, canvasHeight - padding),
            strokeWidth = 1.dp.toPx()
        )

        for (i in 0..4) {
            val y = padding + chartHeight * (1 - i / 4f)
            drawLine(
                color = Color.LightGray,
                start = Offset(padding, y),
                end = Offset(canvasWidth - padding, y),
                strokeWidth = 0.5.dp.toPx()
            )
            drawContext.canvas.nativeCanvas.drawText(
                "${maxMinutes * i / 4}",
                padding - 5.dp.toPx(),
                y + textSize / 3,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    this.textSize = textSize
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
            )
        }

        weeklyStats.forEachIndexed { index, stats ->
            val barHeight = if (maxMinutes > 0) {
                chartHeight * stats.focusMinutes / maxMinutes
            } else {
                0f
            }
            val x = padding + barSpacing * index + (barSpacing - barWidth) / 2
            val y = canvasHeight - padding - barHeight

            drawRect(
                color = Color(0xFF6750A4),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight)
            )

            val date = Date(stats.date)
            val dayLabel = dateFormat.format(date)
            drawContext.canvas.nativeCanvas.drawText(
                dayLabel,
                x + barWidth / 2,
                canvasHeight - padding + 15.dp.toPx(),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    this.textSize = textSize
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}