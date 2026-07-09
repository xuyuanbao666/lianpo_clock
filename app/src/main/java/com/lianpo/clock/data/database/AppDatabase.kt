package com.lianpo.clock.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lianpo.clock.data.database.dao.PomodoroDao
import com.lianpo.clock.data.database.dao.TaskDao
import com.lianpo.clock.data.database.entity.PomodoroRecord
import com.lianpo.clock.data.database.entity.Task

@Database(
    entities = [Task::class, PomodoroRecord::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun pomodoroDao(): PomodoroDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lianpo_clock_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
