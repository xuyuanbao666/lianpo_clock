package com.lianpo.clock.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianpo.clock.data.repository.PomodoroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DailyStats(
    val date: Long,
    val focusMinutes: Int,
    val pomodoroCount: Int
)

data class TotalStats(
    val totalFocusMinutes: Int,
    val totalPomodoros: Int,
    val currentStreak: Int
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val pomodoroRepository: PomodoroRepository
) : ViewModel() {

    private val _todayStats = MutableStateFlow(DailyStats(0L, 0, 0))
    val todayStats: StateFlow<DailyStats> = _todayStats.asStateFlow()

    private val _weeklyStats = MutableStateFlow<List<DailyStats>>(emptyList())
    val weeklyStats: StateFlow<List<DailyStats>> = _weeklyStats.asStateFlow()

    private val _totalStats = MutableStateFlow(TotalStats(0, 0, 0))
    val totalStats: StateFlow<TotalStats> = _totalStats.asStateFlow()

    init {
        loadStatistics()
    }

    fun loadStatistics() {
        viewModelScope.launch {
            loadTodayStats()
            loadWeeklyStats()
            loadTotalStats()
        }
    }

    private suspend fun loadTodayStats() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        val endOfDay = startOfDay + 86400000L

        val focusDuration = pomodoroRepository.getTotalWorkDurationBetween(startOfDay, endOfDay)
        val pomodoroCount = pomodoroRepository.getWorkRecordCountBetween(startOfDay, endOfDay)

        // duration是秒，转换为分钟
        _todayStats.value = DailyStats(
            date = startOfDay,
            focusMinutes = (focusDuration / 60).toInt(),
            pomodoroCount = pomodoroCount
        )
    }

    private suspend fun loadWeeklyStats() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val weeklyData = mutableListOf<DailyStats>()
        for (i in 0 until 7) {
            val startOfDay = calendar.timeInMillis
            val endOfDay = startOfDay + 86400000L

            val focusDuration = pomodoroRepository.getTotalWorkDurationBetween(startOfDay, endOfDay)
            val pomodoroCount = pomodoroRepository.getWorkRecordCountBetween(startOfDay, endOfDay)

            weeklyData.add(
                DailyStats(
                    date = startOfDay,
                    focusMinutes = (focusDuration / 60).toInt(),
                    pomodoroCount = pomodoroCount
                )
            )
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        _weeklyStats.value = weeklyData
    }

    private suspend fun loadTotalStats() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, 2000)
        calendar.set(Calendar.MONTH, 0)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val totalFocusDuration = pomodoroRepository.getTotalWorkDurationBetween(startTime, endTime)
        val totalPomodoros = pomodoroRepository.getWorkRecordCountBetween(startTime, endTime)
        val streak = calculateStreak()

        _totalStats.value = TotalStats(
            totalFocusMinutes = (totalFocusDuration / 60).toInt(),
            totalPomodoros = totalPomodoros,
            currentStreak = streak
        )
    }

    private suspend fun calculateStreak(): Int {
        var streak = 0
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        while (true) {
            val startOfDay = calendar.timeInMillis
            val endOfDay = startOfDay + 86400000L
            val count = pomodoroRepository.getWorkRecordCountBetween(startOfDay, endOfDay)
            if (count > 0) {
                streak++
                calendar.add(Calendar.DAY_OF_MONTH, -1)
            } else {
                break
            }
        }
        return streak
    }
}