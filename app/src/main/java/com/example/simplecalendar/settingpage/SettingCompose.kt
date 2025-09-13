package com.example.simplecalendar.settingpage

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

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
                    IconButton(onClick = { childNavController.popBackStack() }) {
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
                ColorSetting(childNavController)
            }
        }
    }
}

@Composable
fun SettingMain(navController: NavController) {
    Spacer(Modifier.height(15.dp))
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            SettingDirectionItem("Colors", "Set your preferred colors") {
                navController.navigate("setting/color") { launchSingleTop = true }
            }
        }
        items(allToggleSettings) { item->
            ToggleSetting(item.title,item.content, item.settingKeyName)
        }
    }
}

data class ToggleSettingData(val title: String, val content: String, val settingKeyName: Preferences.Key<Boolean>)

val allToggleSettings = listOf(
    ToggleSettingData("Double Tap", "Double tap on date to take insights",
        SettingsKeys.DOUBLETAP_TO_VIEW),
    ToggleSettingData("Auto Delete", "Auto delete expired tasks",
        SettingsKeys.AUTO_DELETE),
)

@Preview
@Composable
fun SettingMainPreview() {
    val navController = rememberNavController() // fake NavController
    DataStoreManager.dataStore = LocalContext.current.dataStore
    SettingMain(navController)
}