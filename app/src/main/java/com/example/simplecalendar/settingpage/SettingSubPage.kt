package com.example.simplecalendar.settingpage

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.NavController
import com.example.simplecalendar.colorMap
import com.example.simplecalendar.settingpage.DataStoreManager.readColorMap
import kotlinx.coroutines.launch

@Composable
fun ColorSetting(navController: NavController) {
    // load
    val colorMap by readColorMap().collectAsState(colorMap)
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        items(colorMap.toList()) { item->
            ColorSettingDisplay(item.first, item.second)
        }
        item {
            ColorInputBlock(colorMap)
        }
    }
}

@Composable
fun ColorSettingDisplay(color: Color, name: String) {
    Row(
        modifier = Modifier.fillMaxWidth(0.85f),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(Modifier
            .padding(10.dp)
            .background(color)
            .size(30.dp))
        Text(name, Modifier.padding(10.dp))
    }
}

@Composable
fun ColorInputBlock(colorMap: Map<Color, String>) {
    var a by remember { mutableStateOf("255") }
    var r by remember { mutableStateOf("0") }
    var g by remember { mutableStateOf("0") }
    var b by remember { mutableStateOf("0") }
    var name by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxWidth(0.85f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 顏色預覽方塊
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .size(30.dp)
                    .background(
                        Color(
                            a.toIntOrNull() ?: 255,
                            r.toIntOrNull() ?: 0,
                            g.toIntOrNull() ?: 0,
                            b.toIntOrNull() ?: 0
                        )
                    )
            )
            // 四個輸入框 (A R G B)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NoBorderTextField(value = a, onValueChange = { a = it }, label = "A")
                NoBorderTextField(value = r, onValueChange = { r = it }, label = "R")
                NoBorderTextField(value = g, onValueChange = { g = it }, label = "G")
                NoBorderTextField(value = b, onValueChange = { b = it }, label = "B")
            }
        }
        Spacer(Modifier.height(5.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Name") },
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            Button(
                onClick = { scope.launch{ checkAndStore(a, r, g, b, name, colorMap) } },
                content = {Text("Submit")}
            )
        }
    }
}
@Composable
fun NoBorderTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.width(65.dp),
        singleLine = true,
        label = { Text(label) },
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

suspend fun checkAndStore(
    a: String,
    r: String,
    g: String,
    b: String,
    name: String,
    colorMap: Map<Color, String>
): Boolean {
    val colorInt = try {
        Color(a.toInt(), r.toInt(), g.toInt(), b.toInt())
    } catch (e: Exception) {
        return false // 非法顏色
    }

    // 檢查名字或顏色是否已存在
    if (colorMap.containsKey(colorInt) || colorMap.containsValue(name)) {
        return false
    }

    // 新增到 Map
    val newMap = colorMap.toMutableMap()
    newMap[colorInt] = name
    Log.d("Save Color", "checkAndStore: $colorInt and $name")
    DataStoreManager.saveColorMap(newMap)
    return true
}


@Composable
fun ToggleSetting(title: String, content: String, settingKey: Preferences.Key<Boolean>, default: Boolean = false) {
    val checked by DataStoreManager.getValueFlow(settingKey, default).collectAsState(default)
    val scope = rememberCoroutineScope()
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(0.8f)
    ) {
        Column {
            Text(
                title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                content,
                fontSize = 15.sp,
                fontWeight = FontWeight.Light
            )
        }
        Switch(
            checked,
            {
                scope.launch {
                    DataStoreManager.setValue(settingKey, it)
                }
            }
        )
    }
}