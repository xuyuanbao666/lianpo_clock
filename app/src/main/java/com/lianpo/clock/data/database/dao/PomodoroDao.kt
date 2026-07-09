package com.lianpo.clock.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lianpo.clock.data.database.entity.PomodoroRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroDao {

    @Query("SELECT * FROM pomodoro_records ORDER BY startTime DESC")
    fun getAllRecords(): Flow<List<PomodoroRecord>>

    @Query("SELECT * FROM pomodoro_records WHERE id = :recordId")
    suspend fun getRecordById(recordId: Long): PomodoroRecord?

    @Query("SELECT * FROM pomodoro_records WHERE taskId = :taskId ORDER BY startTime DESC")
    fun getRecordsByTaskId(taskId: Long): Flow<List<PomodoroRecord>>

    @Query("SELECT * FROM pomodoro_records WHERE startTime BETWEEN :startTime AND :endTime ORDER BY startTime DESC")
    fun getRecordsBetween(startTime: Long, endTime: Long): Flow<List<PomodoroRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: PomodoroRecord): Long

    @Update
    suspend fun updateRecord(record: PomodoroRecord)

    @Delete
    suspend fun deleteRecord(record: PomodoroRecord)

    @Query("SELECT COALESCE(SUM(duration), 0) FROM pomodoro_records WHERE type = 'WORK' AND startTime BETWEEN :startTime AND :endTime")
    suspend fun getTotalWorkDurationBetween(startTime: Long, endTime: Long): Long

    @Query("SELECT COUNT(*) FROM pomodoro_records WHERE type = 'WORK' AND isCompleted = 1 AND startTime BETWEEN :startTime AND :endTime")
    suspend fun getWorkRecordCountBetween(startTime: Long, endTime: Long): Int
}
