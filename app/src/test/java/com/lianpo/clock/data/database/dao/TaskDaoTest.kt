package com.lianpo.clock.data.database.dao

import android.os.Build
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.lianpo.clock.data.database.AppDatabase
import com.lianpo.clock.data.database.entity.Task
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P], manifest = Config.NONE)
class TaskDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var taskDao: TaskDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        taskDao = database.taskDao()
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insert and retrieve task by id`() = runTest {
        val task = Task(title = "Test Task", description = "Test Description")
        val id = taskDao.insertTask(task)

        val retrieved = taskDao.getTaskById(id)
        assertNotNull(retrieved)
        assertEquals("Test Task", retrieved!!.title)
        assertEquals("Test Description", retrieved.description)
        assertEquals(false, retrieved.isCompleted)
        assertEquals(0, retrieved.pomodoroCount)
        assertEquals(1, retrieved.targetPomodoroCount)
    }

    @Test
    fun `getTaskById returns null for non-existent id`() = runTest {
        val result = taskDao.getTaskById(999L)
        assertNull(result)
    }

    @Test
    fun `getAllTasks returns all inserted tasks`() = runTest {
        val task1 = Task(title = "Task 1", createdAt = 1000L)
        val task2 = Task(title = "Task 2", createdAt = 2000L)
        val task3 = Task(title = "Task 3", createdAt = 3000L)

        taskDao.insertTask(task1)
        taskDao.insertTask(task2)
        taskDao.insertTask(task3)

        val tasks = taskDao.getAllTasks().first()
        assertEquals(3, tasks.size)
        // Should be ordered by createdAt DESC
        assertEquals("Task 3", tasks[0].title)
        assertEquals("Task 2", tasks[1].title)
        assertEquals("Task 1", tasks[2].title)
    }

    @Test
    fun `getActiveTasks returns only incomplete tasks`() = runTest {
        val activeTask = Task(title = "Active Task", isCompleted = false)
        val completedTask = Task(title = "Completed Task", isCompleted = true)

        taskDao.insertTask(activeTask)
        taskDao.insertTask(completedTask)

        val activeTasks = taskDao.getActiveTasks().first()
        assertEquals(1, activeTasks.size)
        assertEquals("Active Task", activeTasks[0].title)
    }

    @Test
    fun `updateTask updates existing task`() = runTest {
        val task = Task(title = "Original Title")
        val id = taskDao.insertTask(task)

        val inserted = taskDao.getTaskById(id)!!
        val updated = inserted.copy(title = "Updated Title")
        taskDao.updateTask(updated)

        val retrieved = taskDao.getTaskById(id)!!
        assertEquals("Updated Title", retrieved.title)
    }

    @Test
    fun `deleteTask removes task`() = runTest {
        val task = Task(title = "To Delete")
        val id = taskDao.insertTask(task)

        val inserted = taskDao.getTaskById(id)!!
        taskDao.deleteTask(inserted)

        val result = taskDao.getTaskById(id)
        assertNull(result)
    }

    @Test
    fun `markTaskAsCompleted sets isCompleted and completedAt`() = runTest {
        val task = Task(title = "To Complete")
        val id = taskDao.insertTask(task)

        val completedAt = System.currentTimeMillis()
        taskDao.markTaskAsCompleted(id, completedAt)

        val retrieved = taskDao.getTaskById(id)!!
        assertEquals(true, retrieved.isCompleted)
        assertEquals(completedAt, retrieved.completedAt)
    }

    @Test
    fun `incrementPomodoroCount increases count by 1`() = runTest {
        val task = Task(title = "Pomodoro Task", pomodoroCount = 0)
        val id = taskDao.insertTask(task)

        taskDao.incrementPomodoroCount(id)
        val retrieved = taskDao.getTaskById(id)!!
        assertEquals(1, retrieved.pomodoroCount)

        taskDao.incrementPomodoroCount(id)
        val retrieved2 = taskDao.getTaskById(id)!!
        assertEquals(2, retrieved2.pomodoroCount)
    }

    @Test
    fun `insert with REPLACE strategy replaces existing task`() = runTest {
        val task = Task(id = 1L, title = "Original")
        taskDao.insertTask(task)

        val replacement = Task(id = 1L, title = "Replaced")
        taskDao.insertTask(replacement)

        val tasks = taskDao.getAllTasks().first()
        assertEquals(1, tasks.size)
        assertEquals("Replaced", tasks[0].title)
    }

    @Test
    fun `empty database returns empty list`() = runTest {
        val tasks = taskDao.getAllTasks().first()
        assertEquals(0, tasks.size)
    }
}
