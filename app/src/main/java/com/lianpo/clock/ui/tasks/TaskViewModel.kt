package com.lianpo.clock.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianpo.clock.data.database.entity.Task
import com.lianpo.clock.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _filterMode = MutableStateFlow(FilterMode.ALL)
    val filterMode: StateFlow<FilterMode> = _filterMode.asStateFlow()

    val tasks: StateFlow<List<Task>> = taskRepository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    fun getFilteredTasks(taskList: List<Task>): List<Task> {
        val cal = Calendar.getInstance()
        cal.set(_selectedYear.value, _selectedMonth.value, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val monthStart = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val monthEnd = cal.timeInMillis

        return when (_filterMode.value) {
            FilterMode.ALL -> taskList
            FilterMode.THIS_MONTH -> taskList.filter { it.createdAt in monthStart until monthEnd }
            FilterMode.COMPLETED -> taskList.filter { it.isCompleted }
            FilterMode.ACTIVE -> taskList.filter { !it.isCompleted }
        }
    }

    fun setFilterMode(mode: FilterMode) {
        _filterMode.value = mode
    }

    fun previousMonth() {
        val cal = Calendar.getInstance()
        cal.set(_selectedYear.value, _selectedMonth.value, 1)
        cal.add(Calendar.MONTH, -1)
        _selectedYear.value = cal.get(Calendar.YEAR)
        _selectedMonth.value = cal.get(Calendar.MONTH)
    }

    fun nextMonth() {
        val cal = Calendar.getInstance()
        cal.set(_selectedYear.value, _selectedMonth.value, 1)
        cal.add(Calendar.MONTH, 1)
        _selectedYear.value = cal.get(Calendar.YEAR)
        _selectedMonth.value = cal.get(Calendar.MONTH)
    }

    fun getMonthName(): String {
        val months = listOf("一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月")
        return "${_selectedYear.value}年 ${months[_selectedMonth.value]}"
    }

    fun addTask(title: String, description: String, targetPomodoroCount: Int) {
        viewModelScope.launch {
            val task = Task(
                title = title,
                description = description,
                targetPomodoroCount = targetPomodoroCount
            )
            taskRepository.insertTask(task)
            _showAddDialog.value = false
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
            if (_selectedTask.value?.id == task.id) {
                _selectedTask.value = null
            }
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            if (task.isCompleted) {
                val updated = task.copy(isCompleted = false, completedAt = null)
                taskRepository.updateTask(updated)
            } else {
                taskRepository.markTaskAsCompleted(task.id)
            }
        }
    }

    fun selectTask(task: Task?) {
        _selectedTask.value = task
    }

    fun showAddDialog() {
        _showAddDialog.value = true
    }

    fun dismissAddDialog() {
        _showAddDialog.value = false
    }
}

enum class FilterMode {
    ALL,
    THIS_MONTH,
    COMPLETED,
    ACTIVE
}