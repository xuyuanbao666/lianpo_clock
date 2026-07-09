package com.lianpo.clock.di

import android.content.Context
import androidx.room.Room
import com.lianpo.clock.data.database.AppDatabase
import com.lianpo.clock.data.database.dao.PomodoroDao
import com.lianpo.clock.data.database.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "lianpo_clock_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideTaskDao(appDatabase: AppDatabase): TaskDao {
        return appDatabase.taskDao()
    }

    @Provides
    @Singleton
    fun providePomodoroDao(appDatabase: AppDatabase): PomodoroDao {
        return appDatabase.pomodoroDao()
    }
}