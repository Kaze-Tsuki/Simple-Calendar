package com.example.simplecalendar.settingpage

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Data Store for settings
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object SettingsKeys {
    val COLOR_MAP = stringPreferencesKey("color_map")
}

suspend fun saveColorMap(context: Context, colorMap: Map<Color, String>) {
    val stringMap = colorMap.mapKeys { it.key.toArgbString() }
    val json = Json.encodeToString(stringMap)
    context.dataStore.edit { prefs ->
        prefs[SettingsKeys.COLOR_MAP] = json
    }
}

fun readColorMap(context: Context): Flow<Map<Color, String>> {
    return context.dataStore.data.map { prefs ->
        val json = prefs[SettingsKeys.COLOR_MAP]
        if (json != null) {
            val stringMap = Json.decodeFromString<Map<String, String>>(json)
            stringMap.mapKeys { it.key.toColor() }
        } else {
            emptyMap()
        }
    }
}

// type transfer
fun Color.toArgbString(): String {
    val alpha = (alpha * 255).toInt()
    val red = (red * 255).toInt()
    val green = (green * 255).toInt()
    val blue = (blue * 255).toInt()
    return String.format("#%02X%02X%02X%02X", alpha, red, green, blue)
}

fun String.toColor(): Color {
    return try {
        val argb = this.removePrefix("#").toLong(16)
        Color(
            red = ((argb shr 16) and 0xFF) / 255f,
            green = ((argb shr 8) and 0xFF) / 255f,
            blue = (argb and 0xFF) / 255f,
            alpha = ((argb shr 24) and 0xFF) / 255f
        )
    } catch (e: Exception) {
        Color.Black // fallback 顏色
    }
}