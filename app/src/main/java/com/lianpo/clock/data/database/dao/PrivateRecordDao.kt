package com.lianpo.clock.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.lianpo.clock.data.database.entity.PrivateRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PrivateRecordDao {
    @Query("SELECT * FROM private_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<PrivateRecord>>

    @Query("SELECT * FROM private_records WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getRecordsBetween(startTime: Long, endTime: Long): Flow<List<PrivateRecord>>

    @Query("SELECT COUNT(*) FROM private_records WHERE timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getCountBetween(startTime: Long, endTime: Long): Int

    @Insert
    suspend fun insert(record: PrivateRecord): Long

    @Update
    suspend fun update(record: PrivateRecord)

    @Delete
    suspend fun delete(record: PrivateRecord)

    @Query("DELETE FROM private_records WHERE id = :id")
    suspend fun deleteById(id: Long)
}