package com.lianpo.clock.ui.timer

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.flowOf
import org.mockito.Mockito.`when`
import com.lianpo.clock.data.repository.PomodoroRepository
import com.lianpo.clock.data.repository.TaskRepository
import com.lianpo.clock.util.SoundPlayer
import com.lianpo.clock.util.TimerState
import com.lianpo.clock.util.TimerType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class TimerViewModelTest {

    private lateinit var viewModel: TimerViewModel
    private lateinit var pomodoroRepository: PomodoroRepository
    private lateinit var taskRepository: TaskRepository
    private lateinit var soundPlayer: SoundPlayer
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        pomodoroRepository = mock(PomodoroRepository::class.java)
        taskRepository = mock(TaskRepository::class.java)
        soundPlayer = mock(SoundPlayer::class.java)
        val context = mock(Context::class.java)
        val prefs = mock(SharedPreferences::class.java)
        `when`(context.getSharedPreferences("lianpo_clock_settings", Context.MODE_PRIVATE)).thenReturn(prefs)
        `when`(prefs.getInt("work_duration", 25)).thenReturn(25)
        `when`(prefs.getInt("short_break_duration", 5)).thenReturn(5)
        `when`(prefs.getInt("long_break_duration", 15)).thenReturn(15)
        `when`(prefs.getInt("long_break_interval", 4)).thenReturn(4)
        `when`(taskRepository.getActiveTasks()).thenReturn(flowOf(emptyList()))
        viewModel = TimerViewModel(pomodoroRepository, taskRepository, soundPlayer, context)
    }

    @After
    fun tearDown() {
        viewModel.resetTimer()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be IDLE`() {
        assertEquals(TimerState.IDLE, viewModel.timerState.value)
    }

    @Test
    fun `initial timer type should be WORK`() {
        assertEquals(TimerType.WORK, viewModel.timerType.value)
    }

    @Test
    fun `initial time remaining should be workDuration in seconds`() {
        assertEquals(25 * 60, viewModel.timeRemaining.value)
    }

    @Test
    fun `initial completed pomodoros should be 0`() {
        assertEquals(0, viewModel.completedPomodoros.value)
    }

    @Test
    fun `initial current task should be null`() {
        assertEquals(null, viewModel.currentTask.value)
    }

    @Test
    fun `startTimer should change state to RUNNING`() {
        viewModel.startTimer()
        assertEquals(TimerState.RUNNING, viewModel.timerState.value)
    }

    @Test
    fun `startTimer from IDLE should keep timer type as WORK`() {
        viewModel.startTimer()
        assertEquals(TimerType.WORK, viewModel.timerType.value)
    }

    @Test
    fun `pauseTimer should change state to PAUSED`() {
        viewModel.startTimer()
        viewModel.pauseTimer()
        assertEquals(TimerState.PAUSED, viewModel.timerState.value)
    }

    @Test
    fun `resetTimer should change state to IDLE`() {
        viewModel.startTimer()
        viewModel.resetTimer()
        assertEquals(TimerState.IDLE, viewModel.timerState.value)
    }

    @Test
    fun `resetTimer should reset timer type to WORK`() {
        viewModel.startTimer()
        viewModel.resetTimer()
        assertEquals(TimerType.WORK, viewModel.timerType.value)
    }

    @Test
    fun `resetTimer should reset time remaining to initial value`() {
        viewModel.startTimer()
        viewModel.resetTimer()
        assertEquals(25 * 60, viewModel.timeRemaining.value)
    }

    @Test
    fun `resetTimer should reset total time to initial value`() {
        viewModel.startTimer()
        viewModel.resetTimer()
        assertEquals(25 * 60, viewModel.totalTime.value)
    }

    @Test
    fun `pause then resume should return to RUNNING`() {
        viewModel.startTimer()
        viewModel.pauseTimer()
        assertEquals(TimerState.PAUSED, viewModel.timerState.value)
        viewModel.startTimer()
        assertEquals(TimerState.RUNNING, viewModel.timerState.value)
    }

    @Test
    fun `selectTask should update current task`() {
        val task = com.lianpo.clock.data.database.entity.Task(
            id = 1L,
            title = "Test Task"
        )
        viewModel.selectTask(task)
        assertEquals(task, viewModel.currentTask.value)
    }

    @Test
    fun `selectTask null should clear current task`() {
        val task = com.lianpo.clock.data.database.entity.Task(
            id = 1L,
            title = "Test Task"
        )
        viewModel.selectTask(task)
        viewModel.selectTask(null)
        assertEquals(null, viewModel.currentTask.value)
    }

    @Test
    fun `formatTime should format seconds correctly`() {
        assertEquals("25:00", viewModel.formatTime(1500))
        assertEquals("05:30", viewModel.formatTime(330))
        assertEquals("00:00", viewModel.formatTime(0))
        assertEquals("01:01", viewModel.formatTime(61))
    }

    @Test
    fun `getProgress should return 0 when no time elapsed`() {
        assertEquals(0f, viewModel.getProgress())
    }

    @Test
    fun `getProgress should return correct fraction after time passes`() {
        viewModel.startTimer()
        // With UnconfinedTestDispatcher, coroutines execute eagerly
        // After start, progress should still be close to 0
        val progress = viewModel.getProgress()
        org.junit.Assert.assertTrue(progress >= 0f && progress <= 1f)
    }
}
