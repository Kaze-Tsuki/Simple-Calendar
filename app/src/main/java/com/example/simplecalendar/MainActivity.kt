package com.example.simplecalendar

import android.graphics.drawable.shapes.Shape
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.Shapes
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.simplecalendar.ui.theme.SimpleCalendarTheme
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.log

val colorMap = mapOf<Color, String>(
    Color.Black to "Black",
    Color.Blue to "Blue",
    Color.Magenta to "Magenta",
    Color.Green to "Green",
    Color.Gray to "Gray",
    Color.White to "White",
    Color.Red to "Red",
    Color.Yellow to "Yellow"
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SimpleCalendarTheme {
                App()
            }
        }
    }
}

@Composable
fun App(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val db = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "task-db"
    ).build()
    val taskDao = db.taskDao()

    val calendarViewModel: CalendarViewModel = viewModel(
        factory = CalendarViewModelFactory(taskDao)
    )


    val calendarUIState by calendarViewModel.calendarState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {BottomBar(navController)}
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "main",
            modifier = modifier.padding(innerPadding)
        ) {
            composable("main") {
                CalendarMonth(
                    navController = navController,
                    calendarViewModel = calendarViewModel
                )
            }

            composable("addTask") {
                AddTask(
                    navController = navController,
                    calendarViewModel
                )
            }
            composable("viewDay") {
                ViewDay(
                    navController = navController,
                    calendarViewModel = calendarViewModel
                )
            }
            composable("setting") {
                Setting(
                    navController = navController,

                )
            }
            composable("updateTask") {
                UpdatePage(
                    task = calendarUIState.selectedTask!!,
                    onDumped = {navController.popBackStack()},
                    onSubmit = {
                        calendarViewModel.updateTask(it)
                    }
                )
            }
        }
    }
}

@Composable
fun AddTask(navController: NavController, calendarViewModel: CalendarViewModel, modifier: Modifier = Modifier) {
    val taskIOState by calendarViewModel.inputTaskState.collectAsState()
    var showDateSelector by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        TextField(
            onValueChange = {
                calendarViewModel.setTitle(it)
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
                calendarViewModel.setContent(it)
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
                    calendarViewModel.setStartDate(startDate?: LocalDate.now())
                    calendarViewModel.setEndDate(endDate?: LocalDate.now())
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
                calendarViewModel.setColor(it)
            },
            taskIOState.color
        )
        Button(
            onClick = {calendarViewModel.submitTask()},
            content = {
                Text("Submit")
            }
        )
    }
}


data class BottomNavItem(val title: String, val route: String, val icon: ImageVector)
@Composable
fun BottomBar(navController: NavController, modifier: Modifier = Modifier) {
    val bottomBarItems = listOf(
        BottomNavItem("首頁", "main", Icons.Default.Home),
        BottomNavItem("任務", "addTask", Icons.Default.Add),
        BottomNavItem("設定", "setting", Icons.Default.Settings),
    )
    BottomNavigation {
        val navBackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackEntry?.destination?.route
        bottomBarItems.forEach { item->
            BottomNavigationItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                selected = currentRoute == item.route,
                label = { Text(item.title) },
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

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

@Composable
fun ColorSelect(onChosen: (Color)->Unit, color: Color) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .padding(8.dp)
    ) {
        Button(
            onClick = {expanded = true},
            content = { ColorSelectItem(colorMap[color]!!, color, Modifier.height(50.dp).fillMaxWidth()) }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            colorMap.toList().forEach { it->
                DropdownMenuItem(
                    text = { ColorSelectItem(it.second, it.first, Modifier.height(50.dp).width(150.dp)) },
                    onClick = {
                        onChosen(it.first)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ColorSelectItem(name: String, color: Color, modifier: Modifier = Modifier){
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Absolute.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, Modifier.weight(1f))
        Box(
            modifier = Modifier.padding(5.dp)
                .background(color = color)
                .size(40.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SimpleCalendarTheme {
        App()
    }
}