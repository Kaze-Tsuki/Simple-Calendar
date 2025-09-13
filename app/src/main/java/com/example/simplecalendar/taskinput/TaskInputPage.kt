package com.example.simplecalendar.taskinput

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.simplecalendar.AlertDialog
import com.example.simplecalendar.GlobalViewModel
import com.example.simplecalendar.Screens
import com.example.simplecalendar.Task
import com.example.simplecalendar.colorMap
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun TaskInputPage(navController: NavController, globalViewModel: GlobalViewModel, taskInputVM: TaskInputVM = viewModel(), isUpdate: Boolean, modifier: Modifier = Modifier) {
    val taskIOState by taskInputVM.inputTaskState.collectAsState()
    val globalUiState by globalViewModel.globalState.collectAsState()
    var showDateSelector by remember { mutableStateOf(false) }
    var finishNotify by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if(isUpdate) taskInputVM.setup(globalViewModel.globalState.value.selectedTask?:
        Task(startDate = globalUiState.selectedDate.toString(), endDate = globalUiState.selectedDate.toString()))
        else taskInputVM.setup(Task(startDate = globalUiState.selectedDate.toString(), endDate = globalUiState.selectedDate.toString()))
        Log.d("Type check", "TaskInputPage: isUpdate=$isUpdate, task=$taskIOState")
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        TextField(
            onValueChange = {
                taskInputVM.setTitle(it)
            },
            value = taskIOState.title,
            maxLines = 1,
            label = { Text("Title") },
            modifier = Modifier
                .fillMaxWidth()
        )
        Spacer(Modifier.height(30.dp))
        TextField(
            onValueChange = {
                taskInputVM.setContent(it)
            },
            value = taskIOState.content,
            maxLines = 1,
            label = { Text("Content") },
            modifier = Modifier
                .fillMaxWidth()
        )
        if(showDateSelector)
            DateInputDialog(
                onDismiss = {showDateSelector = false},
                onDateSelected = {start, end->
                    val startDate = start?.let {millis ->
                        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    val endDate = end?.let {millis ->
                        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    taskInputVM.setStartDate(startDate?: LocalDate.now())
                    taskInputVM.setEndDate(endDate?: LocalDate.now())
                }
            )
        Button(
            onClick = {showDateSelector = true},
            content = {Text("Select Date")}
        )
        Row {
            Text("Start: ${taskIOState.startDate}")
            Spacer(Modifier.width(50.dp))
            Text("End: ${taskIOState.endDate}")
        }
        ColorSelect(
            onChosen = { it->
                taskInputVM.setColor(it)
            },
            taskIOState.color
        )
        Button(
            onClick = {
                if (isUpdate) globalViewModel.updateTask(taskIOState)
                else globalViewModel.submitTask(taskIOState)
                finishNotify = true
            },
            content = {
                Text("Submit")
            }
        )
    }
    if (finishNotify) {
        if(isUpdate)
            navController.popBackStack()
        else
            taskInputVM.setup(Task(startDate = globalUiState.selectedDate.toString(), endDate = globalUiState.selectedDate.toString()))
    }
}

// date range input
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateInputDialog(onDateSelected: (Long?, Long?) -> Unit, onDismiss: ()->Unit) {
    val dateRangePickerState = rememberDateRangePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(
                        dateRangePickerState.selectedStartDateMillis,
                        dateRangePickerState.selectedEndDateMillis
                    )
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            title = {
                Text(
                    text = "Select date range"
                )
            },
            showModeToggle = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(16.dp)
        )
    }
}
// color selector
@Composable
fun ColorSelect(onChosen: (Color)->Unit, color: Color) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .padding(8.dp)
    ) {
        Button(
            onClick = {expanded = true},
            content = { ColorSelectItem(colorMap[color]!!, color, Modifier
                .height(50.dp)
                .fillMaxWidth()) }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            colorMap.toList().forEach { it->
                DropdownMenuItem(
                    text = { ColorSelectItem(it.second, it.first, Modifier
                        .height(50.dp)
                        .width(150.dp)) },
                    onClick = {
                        onChosen(it.first)
                        expanded = false
                    }
                )
            }
        }
    }
}
// color selector's display component
@Composable
fun ColorSelectItem(name: String, color: Color, modifier: Modifier = Modifier){
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Absolute.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, Modifier.weight(1f))
        Box(
            modifier = Modifier
                .padding(5.dp)
                .background(color = color)
                .size(40.dp),
        )
    }
}
