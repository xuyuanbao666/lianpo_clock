package com.lianpo.clock.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "private_records")
data class PrivateRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val memo: String = "",
    val mood: String = ""
)