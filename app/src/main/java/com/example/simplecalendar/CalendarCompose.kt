package com.example.simplecalendar

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

// monthly calendar display
@Composable
fun CalendarMonth(modifier: Modifier = Modifier, navController: NavController, GlobalViewModel: GlobalViewModel) {
    // 日->六
    val calendarUIState by GlobalViewModel.globalState.collectAsState()
    val year = calendarUIState.displayYear
    val month = calendarUIState.displayMonth
    val firstDayOfWeek: Int = LocalDate.of(year,month,1).dayOfWeek.value % 7 //with sunday=0
    val dayInMonth: Int = YearMonth.of(year,month).lengthOfMonth()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NumSelect(
                current = year,
                onChosen = { GlobalViewModel.setYear(it) },
                min = year - 5,
                max = year + 5
            )
            NumSelect(
                current = month,
                onChosen = { GlobalViewModel.setMonth(it) },
                min = 1,
                max = 12
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // 星期標題
            items(listOf("日","一","二","三","四","五","六")) { dayName ->
                Text(
                    text = dayName,
                    modifier = Modifier.padding(8.dp),
                    textAlign = TextAlign.Center
                )
            }
            // 空白格子
            items(firstDayOfWeek) {
                DayCell(0)
            }
            // 日期格子
            items(dayInMonth) { day ->
                DayCell(day) {
                    GlobalViewModel.focusDate(LocalDate.of(year, month, day))
                    navController.navigate("viewDay") {
                        launchSingleTop = true
                    }
                }
            }
        }
    }

}
// for year and month of calendar
@Composable
fun NumSelect(current: Int, onChosen:(Int)->Unit, min: Int, max: Int) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .padding(8.dp)
    ) {
        Button(
            onClick = {expanded = true},
            content = { Text("$current") }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            for (i in min..max) {
                DropdownMenuItem(
                    text = { Text("$i") },
                    onClick = {
                        onChosen(i)
                        expanded = false
                    }
                )
            }
        }
    }
}
// fill the calendar with day cells
@Composable
fun DayCell(day: Int, onCLick: ()->Unit = {}) {
    if (day == 0) {
        Box(modifier = Modifier.size(48.dp))
    }
    else {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable { onCLick() },
            contentAlignment = Alignment.Center
        ) {
            Text("$day")
        }
    }
}
// insight into day to know tasks of a day
@Composable
fun ViewDay(modifier: Modifier = Modifier, navController: NavController, GlobalViewModel: GlobalViewModel) {
    val calendarUIState by GlobalViewModel.globalState.collectAsState()
    val tasks by GlobalViewModel
        .accessDate(calendarUIState.selectedDate)
        .collectAsState(emptyList())
    LazyColumn {
        item {
            Text("${calendarUIState.selectedDate}")
        }
        items(tasks) { it
            TaskDisplayCard(
                it,
                {updatedTask->
                    GlobalViewModel.focusTask(updatedTask)
                    navController.navigate("taskInputPage/true") {
                        launchSingleTop = true
                    }
                },
                {GlobalViewModel.deleteTask(it)},
            )
        }
    }
}
// in day display, using expanding card to toggle more info
@Composable
fun TaskDisplayCard(
    task: Task,
    onUpdate: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    var folded by remember { mutableStateOf(true) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { folded = !folded } // 點擊展開 / 收合
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 標題列
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 顏色條
                Box(
                    modifier = Modifier
                        .size(16.dp, 40.dp)
                        .background(task.color, shape = RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.width(8.dp))

                // 標題
                Text(
                    text = task.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            // 展開後的內容
            if (!folded) {
                Spacer(modifier = Modifier.height(8.dp))

                Text("內容：${task.content}")
                Text("開始：${task.startDate}")
                Text("結束：${task.endDate}")

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onUpdate(task) }) {
                        Text("編輯")
                    }
                    TextButton(onClick = { onDelete(task) }) {
                        Text("刪除")
                    }
                }
            }
        }
    }
}

// setting part is not designed yet
@Composable
fun Setting(modifier: Modifier = Modifier, navController: NavController) {

}