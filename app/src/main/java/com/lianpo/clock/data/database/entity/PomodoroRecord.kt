package com.lianpo.clock.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class PomodoroType {
    WORK,
    SHORT_BREAK,
    LONG_BREAK
}

@Entity(
    tableName = "pomodoro_records",
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["taskId"])]
)
data class PomodoroRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long? = null,
    val startTime: Long,
    val endTime: Long? = null,
    val duration: Long = 0,
    val isCompleted: Boolean = false,
    val type: PomodoroType = PomodoroType.WORK
)
