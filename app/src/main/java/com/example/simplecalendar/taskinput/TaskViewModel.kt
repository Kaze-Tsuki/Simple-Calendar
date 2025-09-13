package com.example.simplecalendar.taskinput

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.simplecalendar.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class TaskInputVM: ViewModel() {
    private val _taskInputState = MutableStateFlow<Task>(Task())
    val inputTaskState: StateFlow<Task> = _taskInputState

    // Functions for taskInput states
    fun setup(task: Task) {
        _taskInputState.update{ task }
    }
    fun setStartDate(date: LocalDate) {
        _taskInputState.update { it.copy(startDate = date.toString()) }
    }
    fun setEndDate(date: LocalDate) {
        _taskInputState.update { it.copy(endDate = date.toString()) }
    }
    fun setTitle(title: String) {
        _taskInputState.update { it.copy(title = title) }
    }
    fun setContent(content: String) {
        _taskInputState.update { it.copy(content = content) }
    }
    fun setColor(color: Color) {
        _taskInputState.update { it.copy(color = color) }
    }
}
