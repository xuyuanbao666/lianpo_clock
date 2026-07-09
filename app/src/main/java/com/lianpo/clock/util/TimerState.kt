package com.lianpo.clock.util

enum class TimerState {
    IDLE,
    RUNNING,
    PAUSED,
    FINISHED
}

enum class TimerType {
    WORK,
    SHORT_BREAK,
    LONG_BREAK
}

data class TimerConfig(
    val workDuration: Int = 25,
    val shortBreakDuration: Int = 5,
    val longBreakDuration: Int = 15,
    val longBreakInterval: Int = 4
)
