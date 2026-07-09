package com.lianpo.clock.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianpo.clock.data.database.entity.PomodoroRecord
import com.lianpo.clock.data.database.entity.PomodoroType
import com.lianpo.clock.data.database.entity.Task
import com.lianpo.clock.data.repository.PomodoroRepository
import com.lianpo.clock.data.repository.TaskRepository
import com.lianpo.clock.util.TimerConfig
import com.lianpo.clock.util.TimerState
import com.lianpo.clock.util.TimerType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val pomodoroRepository: PomodoroRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val config = TimerConfig()

    private val _timerState = MutableStateFlow(TimerState.IDLE)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _timerType = MutableStateFlow(TimerType.WORK)
    val timerType: StateFlow<TimerType> = _timerType.asStateFlow()

    private val _timeRemaining = MutableStateFlow(config.workDuration * 60)
    val timeRemaining: StateFlow<Int> = _timeRemaining.asStateFlow()

    private val _totalTime = MutableStateFlow(config.workDuration * 60)
    val totalTime: StateFlow<Int> = _totalTime.asStateFlow()

    private val _completedPomodoros = MutableStateFlow(0)
    val completedPomodoros: StateFlow<Int> = _completedPomodoros.asStateFlow()

    private val _currentTask = MutableStateFlow<Task?>(null)
    val currentTask: StateFlow<Task?> = _currentTask.asStateFlow()

    private var timerJob: Job? = null
    private var startTime: Long = 0L

    fun startTimer() {
        if (_timerState.value == TimerState.RUNNING) return

        if (_timerState.value == TimerState.IDLE || _timerState.value == TimerState.FINISHED) {
            startTime = System.currentTimeMillis()
        }

        _timerState.value = TimerState.RUNNING
        timerJob = viewModelScope.launch {
            while (_timeRemaining.value > 0 && _timerState.value == TimerState.RUNNING) {
                delay(1000)
                _timeRemaining.value = _timeRemaining.value - 1
            }
            if (_timeRemaining.value == 0) {
                onTimerFinished()
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _timerState.value = TimerState.PAUSED
    }

    fun resetTimer() {
        timerJob?.cancel()
        _timerState.value = TimerState.IDLE
        _timerType.value = TimerType.WORK
        _timeRemaining.value = config.workDuration * 60
        _totalTime.value = config.workDuration * 60
    }

    fun skipToNext() {
        timerJob?.cancel()
        onTimerFinished()
    }

    fun setCurrentTask(task: Task?) {
        _currentTask.value = task
    }

    private fun onTimerFinished() {
        _timerState.value = TimerState.FINISHED
        viewModelScope.launch {
            recordPomodoro()
            moveToNextPhase()
        }
    }

    private suspend fun recordPomodoro() {
        val task = _currentTask.value ?: return
        val pomodoroType = when (_timerType.value) {
            TimerType.WORK -> PomodoroType.WORK
            TimerType.SHORT_BREAK -> PomodoroType.SHORT_BREAK
            TimerType.LONG_BREAK -> PomodoroType.LONG_BREAK
        }

        val record = PomodoroRecord(
            taskId = task.id,
            startTime = startTime,
            endTime = System.currentTimeMillis(),
            duration = (_totalTime.value - _timeRemaining.value).toLong(),
            isCompleted = _timeRemaining.value == 0,
            type = pomodoroType
        )
        pomodoroRepository.insertRecord(record)

        if (_timerType.value == TimerType.WORK && _timeRemaining.value == 0) {
            taskRepository.incrementPomodoroCount(task.id)
            _completedPomodoros.value = _completedPomodoros.value + 1
        }
    }

    private fun moveToNextPhase() {
        when (_timerType.value) {
            TimerType.WORK -> {
                val nextType = if ((_completedPomodoros.value) % config.longBreakInterval == 0 && _completedPomodoros.value > 0) {
                    TimerType.LONG_BREAK
                } else {
                    TimerType.SHORT_BREAK
                }
                _timerType.value = nextType
                val duration = when (nextType) {
                    TimerType.LONG_BREAK -> config.longBreakDuration * 60
                    else -> config.shortBreakDuration * 60
                }
                _timeRemaining.value = duration
                _totalTime.value = duration
            }
            TimerType.SHORT_BREAK, TimerType.LONG_BREAK -> {
                _timerType.value = TimerType.WORK
                _timeRemaining.value = config.workDuration * 60
                _totalTime.value = config.workDuration * 60
            }
        }
        _timerState.value = TimerState.IDLE
    }

    fun getProgress(): Float {
        if (_totalTime.value == 0) return 0f
        return (_totalTime.value - _timeRemaining.value).toFloat() / _totalTime.value.toFloat()
    }

    fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return "%02d:%02d".format(minutes, secs)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
