package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SettingsRepository
import com.example.model.DEFAULT_COLOR_PRESETS
import com.example.model.LedDisplaySettings
import com.example.model.PresetMessage
import com.example.model.SUPPORTED_LANGUAGES
import com.example.model.SpeechLanguage
import com.example.speech.SpeechManager
import com.example.speech.SpeechState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUiState(
    val currentMessage: String = "SPEAK SOMETHING...",
    val isFullscreen: Boolean = false,
    val showLanguageDialog: Boolean = false,
    val showPresetsDialog: Boolean = false,
    val selectedTab: Int = 0 // 0: Controls & Voice, 1: LED Customization, 2: Presets
)

class LedVoiceSignViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)
    val speechManager = SpeechManager(application)

    val settings: StateFlow<LedDisplaySettings> = repository.settings
    val presets: StateFlow<List<PresetMessage>> = repository.presets
    val speechState: StateFlow<SpeechState> = speechManager.speechState
    val liveTranscript: StateFlow<String> = speechManager.liveTranscript
    val rmsLevel: StateFlow<Float> = speechManager.rmsLevel

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // Combined active display message (live transcript if not blank, else manual/last saved message)
    val displayMessage: StateFlow<String> = combine(liveTranscript, _uiState) { transcript, ui ->
        if (transcript.isNotBlank()) transcript else ui.currentMessage
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "SPEAK SOMETHING...")

    init {
        val currentLang = settings.value.languageCode
        speechManager.setLanguage(currentLang)
    }

    fun startListening() {
        speechManager.startListening()
    }

    fun stopListening() {
        speechManager.stopListening()
    }

    fun clearMessage() {
        speechManager.clearTranscript()
        _uiState.value = _uiState.value.copy(currentMessage = "")
    }

    fun setManualMessage(text: String) {
        val upper = text.uppercase().trim()
        _uiState.value = _uiState.value.copy(currentMessage = upper)
        speechManager.setManualText(upper)
    }

    fun selectPreset(preset: PresetMessage) {
        val upper = preset.message.uppercase().trim()
        _uiState.value = _uiState.value.copy(currentMessage = upper)
        speechManager.setManualText(upper)
        updateSettings(
            settings.value.copy(
                selectedColorHex = preset.colorHex,
                speed = preset.speed
            )
        )
    }

    fun updateSettings(newSettings: LedDisplaySettings) {
        repository.updateSettings(newSettings)
        speechManager.setLanguage(newSettings.languageCode)
    }

    fun setLanguage(language: SpeechLanguage) {
        val updated = settings.value.copy(languageCode = language.code)
        repository.updateSettings(updated)
        speechManager.setLanguage(language.code)
    }

    fun savePreset(preset: PresetMessage) {
        repository.saveCustomPreset(preset)
    }

    fun deletePreset(id: String) {
        repository.deletePreset(id)
    }

    fun setFullscreen(fullscreen: Boolean) {
        _uiState.value = _uiState.value.copy(isFullscreen = fullscreen)
    }

    fun setSelectedTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun showLanguageDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showLanguageDialog = show)
    }

    fun showPresetsDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showPresetsDialog = show)
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
    }
}
