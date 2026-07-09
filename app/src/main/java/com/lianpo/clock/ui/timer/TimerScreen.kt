package com.lianpo.clock.ui.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@Composable
fun TimerScreen(
    viewModel: TimerViewModel = hiltViewModel()
) {
    val timerState by viewModel.timerState.collectAsState()
    val timerType by viewModel.timerType.collectAsState()
    val timeRemaining by viewModel.timeRemaining.collectAsState()
    val completedPomodoros by viewModel.completedPomodoros.collectAsState()

    val progressColor = when (timerType) {
        TimerType.WORK -> TomatoRed
        TimerType.SHORT_BREAK, TimerType.LONG_BREAK -> BreakGreen
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
