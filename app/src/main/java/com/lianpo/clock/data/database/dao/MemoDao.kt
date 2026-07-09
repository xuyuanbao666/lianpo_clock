package com.lianpo.clock.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.lianpo.clock.data.database.entity.Memo
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDao {
    @Query("SELECT * FROM memos ORDER BY isPinned DESC, timestamp DESC")
    fun getAllMemos(): Flow<List<Memo>>

    @Insert
    suspend fun insert(memo: Memo): Long

    @Update
    suspend fun update(memo: Memo)

    @Delete
    suspend fun delete(memo: Memo)

    @Query("DELETE FROM memos WHERE id = :id")
    suspend fun deleteById(id: Long)
}