package com.lianpo.clock.data.repository

import com.lianpo.clock.data.database.dao.TaskDao
import com.lianpo.clock.data.database.entity.Task
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {
    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    suspend fun getTaskById(taskId: Long): Task? = taskDao.getTaskById(taskId)

    fun getActiveTasks(): Flow<List<Task>> = taskDao.getActiveTasks()

    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun markTaskAsCompleted(taskId: Long, completedAt: Long = System.currentTimeMillis()) {
        taskDao.markTaskAsCompleted(taskId, completedAt)
    }

    suspend fun incrementPomodoroCount(taskId: Long) {
        taskDao.incrementPomodoroCount(taskId)
    }
}