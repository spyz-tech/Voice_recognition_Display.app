package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.DEFAULT_PRESETS
import com.example.model.LedDisplaySettings
import com.example.model.LedShape
import com.example.model.PresetMessage
import com.example.model.ScrollDirection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("led_voice_sign_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<LedDisplaySettings> = _settings.asStateFlow()

    private val _presets = MutableStateFlow(loadPresets())
    val presets: StateFlow<List<PresetMessage>> = _presets.asStateFlow()

    fun updateSettings(newSettings: LedDisplaySettings) {
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    fun saveCustomPreset(preset: PresetMessage) {
        val currentList = _presets.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == preset.id }
        if (index != -1) {
            currentList[index] = preset
        } else {
            currentList.add(preset)
        }
        _presets.value = currentList
        savePresets(currentList)
    }

    fun deletePreset(id: String) {
        val currentList = _presets.value.filterNot { it.id == id }
        _presets.value = currentList
        savePresets(currentList)
    }

    private fun loadSettings(): LedDisplaySettings {
        return LedDisplaySettings(
            selectedColorHex = prefs.getLong("selectedColorHex", 0xFFFF2222),
            speed = prefs.getInt("speed", 5),
            dotDensity = prefs.getInt("dotDensity", 16),
            glowIntensity = prefs.getFloat("glowIntensity", 0.75f),
            brightness = prefs.getFloat("brightness", 1.0f),
            showScanlines = prefs.getBoolean("showScanlines", true),
            showPixelGrid = prefs.getBoolean("showPixelGrid", true),
            mirrorMode = prefs.getBoolean("mirrorMode", false),
            isBlinking = prefs.getBoolean("isBlinking", false),
            soundReactive = prefs.getBoolean("soundReactive", true),
            rainbowMode = prefs.getBoolean("rainbowMode", false),
            scrollDirection = try {
                ScrollDirection.valueOf(prefs.getString("scrollDirection", ScrollDirection.RIGHT_TO_LEFT.name) ?: ScrollDirection.RIGHT_TO_LEFT.name)
            } catch (e: Exception) {
                ScrollDirection.RIGHT_TO_LEFT
            },
            ledShape = try {
                LedShape.valueOf(prefs.getString("ledShape", LedShape.CIRCLE.name) ?: LedShape.CIRCLE.name)
            } catch (e: Exception) {
                LedShape.CIRCLE
            },
            languageCode = prefs.getString("languageCode", "en-IN") ?: "en-IN",
            letterSpacing = prefs.getFloat("letterSpacing", 1.0f)
        )
    }

    private fun saveSettings(s: LedDisplaySettings) {
        prefs.edit().apply {
            putLong("selectedColorHex", s.selectedColorHex)
            putInt("speed", s.speed)
            putInt("dotDensity", s.dotDensity)
            putFloat("glowIntensity", s.glowIntensity)
            putFloat("brightness", s.brightness)
            putBoolean("showScanlines", s.showScanlines)
            putBoolean("showPixelGrid", s.showPixelGrid)
            putBoolean("mirrorMode", s.mirrorMode)
            putBoolean("isBlinking", s.isBlinking)
            putBoolean("soundReactive", s.soundReactive)
            putBoolean("rainbowMode", s.rainbowMode)
            putString("scrollDirection", s.scrollDirection.name)
            putString("ledShape", s.ledShape.name)
            putString("languageCode", s.languageCode)
            putFloat("letterSpacing", s.letterSpacing)
            apply()
        }
    }

    private fun loadPresets(): List<PresetMessage> {
        val jsonString = prefs.getString("custom_presets_json", null) ?: return DEFAULT_PRESETS
        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<PresetMessage>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    PresetMessage(
                        id = obj.optString("id", System.currentTimeMillis().toString()),
                        title = obj.optString("title", "Custom"),
                        message = obj.optString("message", ""),
                        colorHex = obj.optLong("colorHex", 0xFFFF2222),
                        speed = obj.optInt("speed", 5),
                        iconEmoji = obj.optString("iconEmoji", "💬")
                    )
                )
            }
            if (list.isEmpty()) DEFAULT_PRESETS else list
        } catch (e: Exception) {
            DEFAULT_PRESETS
        }
    }

    private fun savePresets(list: List<PresetMessage>) {
        try {
            val jsonArray = JSONArray()
            for (p in list) {
                val obj = JSONObject().apply {
                    put("id", p.id)
                    put("title", p.title)
                    put("message", p.message)
                    put("colorHex", p.colorHex)
                    put("speed", p.speed)
                    put("iconEmoji", p.iconEmoji)
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString("custom_presets_json", jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
