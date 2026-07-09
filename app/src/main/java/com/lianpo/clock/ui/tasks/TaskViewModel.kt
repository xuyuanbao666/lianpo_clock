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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    val tasks: StateFlow<List<Task>> = taskRepository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

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
