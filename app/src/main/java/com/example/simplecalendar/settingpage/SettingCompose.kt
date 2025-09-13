package com.example.simplecalendar.settingpage

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.simplecalendar.calendarpage.SettingDirectionItem

// setting part is not designed yet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Setting(modifier: Modifier = Modifier, navController: NavController) {
    val childNavController = rememberNavController()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("設定") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {innerPadding->
        NavHost(
            navController = childNavController,
            startDestination = "setting/Main",
            modifier = Modifier.padding(innerPadding),
        ){
            composable("setting/Main") {
                SettingMain(childNavController)
            }
            composable("setting/color") {

            }
        }
    }
}

@Composable
fun SettingMain(navController: NavController) {
    LazyColumn {
        item {
            SettingDirectionItem("Colors", "Set your preferred colors") {
                navController.navigate("setting/color") {
                    launchSingleTop = true
                }
            }
        }
    }
}

data class SettingRoute(
    val currentRoute: String,
    val entry: String,
    val title: String,
    val description: String
)

@Preview
@Composable
fun SettingMainPreview() {
    val navController = rememberNavController() // fake NavController
    SettingMain(navController)
}