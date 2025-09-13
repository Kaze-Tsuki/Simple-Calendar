package com.example.simplecalendar.settingpage

import android.content.Context
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

@Composable
fun ColorSetting(navController: NavController) {
    // load
    val colorMap = readColorMap(LocalContext.current).collectAsState(emptyMap())
    LazyColumn {

    }
}