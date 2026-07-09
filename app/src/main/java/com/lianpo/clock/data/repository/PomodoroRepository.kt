package com.lianpo.clock.data.repository

import com.lianpo.clock.data.database.dao.PomodoroDao
import com.lianpo.clock.data.database.entity.PomodoroRecord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PomodoroRepository @Inject constructor(
    private val pomodoroDao: PomodoroDao
) {
    fun getAllRecords(): Flow<List<PomodoroRecord>> = pomodoroDao.getAllRecords()

    suspend fun getRecordById(recordId: Long): PomodoroRecord? = pomodoroDao.getRecordById(recordId)

    fun getRecordsByTaskId(taskId: Long): Flow<List<PomodoroRecord>> = pomodoroDao.getRecordsByTaskId(taskId)

    fun getRecordsBetween(startTime: Long, endTime: Long): Flow<List<PomodoroRecord>> =
        pomodoroDao.getRecordsBetween(startTime, endTime)

    suspend fun insertRecord(record: PomodoroRecord): Long = pomodoroDao.insertRecord(record)

    suspend fun updateRecord(record: PomodoroRecord) = pomodoroDao.updateRecord(record)

    suspend fun deleteRecord(record: PomodoroRecord) = pomodoroDao.deleteRecord(record)

    suspend fun getTotalWorkDurationBetween(startTime: Long, endTime: Long): Long =
        pomodoroDao.getTotalWorkDurationBetween(startTime, endTime)

    suspend fun getWorkRecordCountBetween(startTime: Long, endTime: Long): Int =
        pomodoroDao.getWorkRecordCountBetween(startTime, endTime)
}