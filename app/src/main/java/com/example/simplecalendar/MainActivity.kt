package com.example.simplecalendar

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.room.Room
import com.example.simplecalendar.calendarpage.CalendarMonth
import com.example.simplecalendar.calendarpage.ViewDay
import com.example.simplecalendar.settingpage.Setting
import com.example.simplecalendar.taskinput.TaskInputPage
import com.example.simplecalendar.ui.theme.SimpleCalendarTheme

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

sealed class Screens(val route: String) {
    object Main: Screens("main") {
        object Calendar: Screens("calendar")
        object ViewDay: Screens("viewDay")
    }
    object Setting: Screens("setting") {
        object AutoDeletion: Screens("setting/autodelete")
        object ColorMap: Screens("setting/color")
    }
    object TaskInputPlaceholder : Screens("taskInputPage/{isUpdate}")

    data class TaskInput(val isUpdate: Boolean) : Screens("taskInputPage/$isUpdate")
}

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
// main application
@Composable
fun App(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
//    navController.addOnDestinationChangedListener { controller, _, _ ->
//        val routes = controller
//            .currentBackStack.value
//            .map { it.destination.route }
//            .joinToString(", ")
//
//        Log.d("BackStackLog", "BackStack: $routes")
//    }

    val context = LocalContext.current
    val db = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "task-db"
    ).build()
    val taskDao = db.taskDao()

    val globalViewModel: GlobalViewModel = viewModel(
        factory = GlobalViewModelFactory(taskDao)
    )

    val globalUIState by globalViewModel.globalState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {BottomBar(navController)}
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screens.Main.route,
            modifier = modifier.padding(innerPadding)
        ) {
            navigation(
                startDestination = Screens.Main.Calendar.route,
                route = Screens.Main.route
            ) {
                composable(Screens.Main.Calendar.route) {
                    CalendarMonth(
                        navController = navController,
                        globalViewModel = globalViewModel
                    )
                }
                composable(Screens.Main.ViewDay.route) {
                    ViewDay(
                        navController = navController,
                        globalViewModel = globalViewModel
                    )
                }
            }

            composable(Screens.TaskInputPlaceholder.route) { navBackStackEntry ->
                val isUpdate = navBackStackEntry.arguments?.getString("isUpdate").toBoolean()
                TaskInputPage(
                    navController = navController,
                    globalViewModel,
                    isUpdate = isUpdate
                )
            }

            composable(Screens.Setting.route) {
                Setting(
                    navController = navController,
                )
            }
        }
    }
}

// class to store info efficiently and Bottom Bar
data class BottomNavItem(val title: String, val route: String, val icon: ImageVector)
@Composable
fun BottomBar(navController: NavController, modifier: Modifier = Modifier) {
    val bottomBarItems = listOf(
        BottomNavItem("首頁", Screens.Main.route, Icons.Default.Home),
        BottomNavItem("任務", Screens.TaskInput(false).route, Icons.Default.Add),
        BottomNavItem("設定", Screens.Setting.route, Icons.Default.Settings),
    )
    NavigationBar {
        val navBackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackEntry?.destination?.route
        bottomBarItems.forEach { item->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                selected = currentRoute == item.route,
                label = { Text(item.title) },
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {  // 回到 graph root
                            saveState = true
                        }
                        launchSingleTop = true
                        if(item.route != Screens.Main.route)
                            restoreState = true
                    }
                }
            )
        }
    }
}
