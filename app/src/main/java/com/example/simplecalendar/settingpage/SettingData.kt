package com.example.simplecalendar.settingpage

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.text.removePrefix

// Data Store for settings
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// Setting Key and Value definition
object SettingsKeys {
    val COLOR_MAP = stringPreferencesKey("color_map")
    val DOUBLETAP_TO_VIEW = booleanPreferencesKey("double_tap_to_view")
    val AUTO_DELETE = booleanPreferencesKey("auto_delete")
}

// Flow repository
object SettingData{
    private val _colorMap = MutableStateFlow(emptyMap<Color, String>())
    val colorMap: StateFlow<Map<Color, String>> = _colorMap
    private val _doubleTap = MutableStateFlow(false)
    val doubleTap: StateFlow<Boolean> = _doubleTap
    private val _autoDelete = MutableStateFlow(false)
    val autoDelete: StateFlow<Boolean> = _autoDelete

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val dt = DataStoreManager.getValueFlow(SettingsKeys.DOUBLETAP_TO_VIEW, false).first()
            val ad = DataStoreManager.getValueFlow(SettingsKeys.AUTO_DELETE, true).first()
            val cm = DataStoreManager.readColorMap().first()

            _doubleTap.update { dt }
            _autoDelete.update { ad }
            _colorMap.update { cm }
        }
    }

    suspend fun setColorMap(m: Map<Color, String>) {
        _colorMap.update { m }
        DataStoreManager.saveColorMap(m)
    }
    suspend fun setDoubleTap(m: Boolean) {
        _doubleTap.update { m }
        DataStoreManager.setValue(SettingsKeys.DOUBLETAP_TO_VIEW, m)
    }
    suspend fun setAutoDelete(m: Boolean) {
        _autoDelete.update { m }
        DataStoreManager.setValue(SettingsKeys.AUTO_DELETE, m)
    }
}

// Setting operation interface
object DataStoreManager {
    lateinit var dataStore: DataStore<Preferences>

    fun init(context: Context) {
        dataStore = context.dataStore
        CoroutineScope(Dispatchers.IO).launch {
            initPreferences() // dataStore 已經被指派
        }
    }

    fun <T> getValueFlow(key: Preferences.Key<T>, default: T): Flow<T> =
        dataStore.data.map { it[key] ?: default }

    suspend fun <T> setValue(key: Preferences.Key<T>, value: T) {
        dataStore.edit { it[key] = value }
    }

    suspend fun saveColorMap(colorMap: Map<Color, String>) {
        val stringMap = colorMap.mapKeys { it.key.toArgbString() }
        val json = Json.encodeToString(stringMap)
        dataStore.edit { prefs ->
            prefs[SettingsKeys.COLOR_MAP] = json
        }
    }

    fun readColorMap(): Flow<Map<Color, String>> {
        return dataStore.data.map { prefs ->
            val json = prefs[SettingsKeys.COLOR_MAP]
//            Log.d("Setting", "readColorMap: $json")
            if (json != null) {
                val stringMap = Json.decodeFromString<Map<String, String>>(json)
                stringMap.mapKeys { it.key.toColor() }
            } else {
                emptyMap()
            }
        }
    }

    suspend fun initPreferences() {
        dataStore.edit { prefs ->
            if (prefs.asMap().isEmpty()) {
                saveColorMap(mapOf<Color, String>(
                    Color.Black to "Black",
                    Color.Blue to "Blue",
                    Color.Magenta to "Magenta",
                    Color.Green to "Green",
                    Color.Gray to "Gray",
                    Color.White to "White",
                    Color.Red to "Red",
                    Color.Yellow to "Yellow"
                ))
            }
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
        this.removePrefix("#").toLong(16).toColor()
    } catch (e: Exception) {
        Color.Black // fallback 顏色
    }
}

fun Long.toColor(): Color {
    return try {
        val argb = this
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
