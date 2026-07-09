package com.lianpo.clock.data.database.dao

import android.os.Build
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.lianpo.clock.data.database.AppDatabase
import com.lianpo.clock.data.database.entity.PomodoroRecord
import com.lianpo.clock.data.database.entity.PomodoroType
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
class PomodoroDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var pomodoroDao: PomodoroDao
    private lateinit var taskDao: TaskDao
    private var taskId: Long = 0L

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        pomodoroDao = database.pomodoroDao()
        taskDao = database.taskDao()

        // Insert a parent task first (required by foreign key)
        taskId = runTest {
            taskDao.insertTask(Task(title = "Parent Task"))
        }
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insert and retrieve record by id`() = runTest {
        val record = PomodoroRecord(
            taskId = taskId,
            startTime = 1000L,
            endTime = 2000L,
            duration = 1000L,
            isCompleted = true,
            type = PomodoroType.WORK
        )
        val id = pomodoroDao.insertRecord(record)

        val retrieved = pomodoroDao.getRecordById(id)
        assertNotNull(retrieved)
        assertEquals(taskId, retrieved!!.taskId)
        assertEquals(1000L, retrieved.startTime)
        assertEquals(2000L, retrieved.endTime)
        assertEquals(1000L, retrieved.duration)
        assertEquals(true, retrieved.isCompleted)
        assertEquals(PomodoroType.WORK, retrieved.type)
    }

    @Test
    fun `getRecordById returns null for non-existent id`() = runTest {
        val result = pomodoroDao.getRecordById(999L)
        assertNull(result)
    }

    @Test
    fun `getAllRecords returns all inserted records`() = runTest {
        val record1 = PomodoroRecord(taskId = taskId, startTime = 1000L, type = PomodoroType.WORK)
        val record2 = PomodoroRecord(taskId = taskId, startTime = 2000L, type = PomodoroType.SHORT_BREAK)
        val record3 = PomodoroRecord(taskId = taskId, startTime = 3000L, type = PomodoroType.WORK)

        pomodoroDao.insertRecord(record1)
        pomodoroDao.insertRecord(record2)
        pomodoroDao.insertRecord(record3)

        val records = pomodoroDao.getAllRecords().first()
        assertEquals(3, records.size)
        // Should be ordered by startTime DESC
        assertEquals(3000L, records[0].startTime)
        assertEquals(2000L, records[1].startTime)
        assertEquals(1000L, records[2].startTime)
    }

    @Test
    fun `getRecordsByTaskId returns records for specific task`() = runTest {
        val otherTaskId = taskDao.insertTask(Task(title = "Other Task"))

        val record1 = PomodoroRecord(taskId = taskId, startTime = 1000L)
        val record2 = PomodoroRecord(taskId = otherTaskId, startTime = 2000L)
        val record3 = PomodoroRecord(taskId = taskId, startTime = 3000L)

        pomodoroDao.insertRecord(record1)
        pomodoroDao.insertRecord(record2)
        pomodoroDao.insertRecord(record3)

        val records = pomodoroDao.getRecordsByTaskId(taskId).first()
        assertEquals(2, records.size)
    }

    @Test
    fun `getRecordsBetween returns records within time range`() = runTest {
        val record1 = PomodoroRecord(taskId = taskId, startTime = 1000L)
        val record2 = PomodoroRecord(taskId = taskId, startTime = 5000L)
        val record3 = PomodoroRecord(taskId = taskId, startTime = 10000L)
        val record4 = PomodoroRecord(taskId = taskId, startTime = 15000L)

        pomodoroDao.insertRecord(record1)
        pomodoroDao.insertRecord(record2)
        pomodoroDao.insertRecord(record3)
        pomodoroDao.insertRecord(record4)

        val records = pomodoroDao.getRecordsBetween(3000L, 12000L).first()
        assertEquals(2, records.size)
        assertEquals(10000L, records[0].startTime)
        assertEquals(5000L, records[1].startTime)
    }

    @Test
    fun `getRecordsBetween returns empty list when no records in range`() = runTest {
        val record = PomodoroRecord(taskId = taskId, startTime = 1000L)
        pomodoroDao.insertRecord(record)

        val records = pomodoroDao.getRecordsBetween(5000L, 10000L).first()
        assertEquals(0, records.size)
    }

    @Test
    fun `updateRecord updates existing record`() = runTest {
        val record = PomodoroRecord(
            taskId = taskId,
            startTime = 1000L,
            duration = 500L
        )
        val id = pomodoroDao.insertRecord(record)

        val inserted = pomodoroDao.getRecordById(id)!!
        val updated = inserted.copy(duration = 1500L, isCompleted = true)
        pomodoroDao.updateRecord(updated)

        val retrieved = pomodoroDao.getRecordById(id)!!
        assertEquals(1500L, retrieved.duration)
        assertEquals(true, retrieved.isCompleted)
    }

    @Test
    fun `deleteRecord removes record`() = runTest {
        val record = PomodoroRecord(taskId = taskId, startTime = 1000L)
        val id = pomodoroDao.insertRecord(record)

        val inserted = pomodoroDao.getRecordById(id)!!
        pomodoroDao.deleteRecord(inserted)

        val result = pomodoroDao.getRecordById(id)
        assertNull(result)
    }

    @Test
    fun `getTotalWorkDurationBetween returns sum of work durations`() = runTest {
        val record1 = PomodoroRecord(
            taskId = taskId, startTime = 1000L,
            duration = 1500L, type = PomodoroType.WORK
        )
        val record2 = PomodoroRecord(
            taskId = taskId, startTime = 2000L,
            duration = 300L, type = PomodoroType.SHORT_BREAK
        )
        val record3 = PomodoroRecord(
            taskId = taskId, startTime = 3000L,
            duration = 1500L, type = PomodoroType.WORK
        )

        pomodoroDao.insertRecord(record1)
        pomodoroDao.insertRecord(record2)
        pomodoroDao.insertRecord(record3)

        val total = pomodoroDao.getTotalWorkDurationBetween(0L, 5000L)
        assertEquals(3000L, total)
    }

    @Test
    fun `getWorkRecordCountBetween returns count of completed work records`() = runTest {
        val record1 = PomodoroRecord(
            taskId = taskId, startTime = 1000L,
            isCompleted = true, type = PomodoroType.WORK
        )
        val record2 = PomodoroRecord(
            taskId = taskId, startTime = 2000L,
            isCompleted = false, type = PomodoroType.WORK
        )
        val record3 = PomodoroRecord(
            taskId = taskId, startTime = 3000L,
            isCompleted = true, type = PomodoroType.WORK
        )
        val record4 = PomodoroRecord(
            taskId = taskId, startTime = 4000L,
            isCompleted = true, type = PomodoroType.SHORT_BREAK
        )

        pomodoroDao.insertRecord(record1)
        pomodoroDao.insertRecord(record2)
        pomodoroDao.insertRecord(record3)
        pomodoroDao.insertRecord(record4)

        val count = pomodoroDao.getWorkRecordCountBetween(0L, 5000L)
        assertEquals(2, count)
    }

    @Test
    fun `insert with REPLACE strategy replaces existing record`() = runTest {
        val record = PomodoroRecord(id = 1L, taskId = taskId, startTime = 1000L, duration = 500L)
        pomodoroDao.insertRecord(record)

        val replacement = PomodoroRecord(id = 1L, taskId = taskId, startTime = 1000L, duration = 1500L)
        pomodoroDao.insertRecord(replacement)

        val records = pomodoroDao.getAllRecords().first()
        assertEquals(1, records.size)
        assertEquals(1500L, records[0].duration)
    }

    @Test
    fun `empty database returns empty list`() = runTest {
        val records = pomodoroDao.getAllRecords().first()
        assertEquals(0, records.size)
    }
}
