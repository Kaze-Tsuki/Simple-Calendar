package com.example.simplecalendar

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
// DB schema
@Entity(tableName = "taskList")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val content: String = "",
    val color: Color = Color.Black,
    val startDate: String = "",
    val endDate: String = ""
)
// the interface of Task Room DB
@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(task: Task)

    @Delete
    fun delete(task: Task)

    @Update
    fun update(task: Task)

    @Query("SELECT * From taskList " +
            "Where :startOfMonth <= endDate and :startOfNextMonth > startDate Order by startDate ASC")
    fun findMonth(startOfMonth: String, startOfNextMonth: String): Flow<List<Task>>

    @Query("SELECT * From taskList " +
            "Where :date >= startDate And :date <= endDate Order by startDate ASC")
    fun findDay(date: String): Flow<List<Task>>
}

data class GlobalState(
    val selectedDate: LocalDate = LocalDate.now(),
    val displayYear: Int = LocalDate.now().year,
    val displayMonth: Int = LocalDate.now().monthValue,
    val selectedTask: Task? = null,
)

class GlobalViewModel(private val taskDao: TaskDao): ViewModel() {
    private val _globalState = MutableStateFlow<GlobalState>(GlobalState())
    val globalState: StateFlow<GlobalState> = _globalState
    //state value of current browsing month data
    val startOfMonth: LocalDate = LocalDate.of(globalState.value.displayYear, globalState.value.displayMonth,1)
    val monthTaskData: StateFlow<List<Task>> = taskDao.findMonth(
        startOfMonth.toString(),
        startOfMonth.plusMonths(1).toString()
    ).stateIn(scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList())

    // Functions for calendar states
    fun focusDate(date: LocalDate) {
        _globalState.update { it.copy(selectedDate = date) }
    }
    fun focusTask(task: Task) {
        _globalState.update { it.copy(selectedTask = task) }
    }
    fun setYear(year: Int) {
        _globalState.update { it.copy(displayYear = year) }
    }
    fun setMonth(month: Int) {
        _globalState.update { it.copy(displayMonth = month) }
    }

    // quick method to view date data
    fun accessDate(date: LocalDate): Flow<List<Task>> {
        return taskDao.findDay(date.toString())
    }

    fun submitTask(task: Task): Boolean {
        Log.d("input check", "submitTask: $task")
        // 簡單驗證
        if (task.startDate > task.endDate || task.title.isBlank() || task.content.isBlank()) {
            Log.d("Database Op", "submitTask: Data invalid")
            return false
        }

        Log.d("Database Op", "submitTask: Data valid")
        viewModelScope.launch(Dispatchers.IO) {
            taskDao.insert(task)
        }
        return true
    }

    fun updateTask(task: Task): Boolean {
        if (task.startDate > task.endDate || task.title.isBlank() || task.content.isBlank()) {
            Log.d("Database Op", "submitTask: Data invalid")
            return false
        }

        Log.d("Database Op", "updateTask: Data valid")
        viewModelScope.launch(Dispatchers.IO) {
            taskDao.update(task)
        }
        return true
    }
    fun deleteTask(task: Task): Boolean {
        viewModelScope.launch(Dispatchers.IO) {
            taskDao.delete(task)
        }
        return true
    }
}

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

// AI generated code for customized factory viewmodel generator(assigned DB into viewmodel)
class GlobalViewModelFactory(private val taskDao: TaskDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GlobalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GlobalViewModel(taskDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// DB creator(most DB has more than one table)
@Database(entities = [Task::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}

// since color has unstorable type ULong, using ARGB to transform
class Converters {
    @TypeConverter
    fun fromColor(color: Color): Int = color.toArgb() // 存成 ARGB Int

    @TypeConverter
    fun toColor(value: Int): Color = Color(value) // 從 ARGB Int 還原
}
