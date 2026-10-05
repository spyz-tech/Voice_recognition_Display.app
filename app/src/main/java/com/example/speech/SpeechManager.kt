package com.example.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class SpeechState {
    object Idle : SpeechState()
    object Initializing : SpeechState()
    object Listening : SpeechState()
    data class Error(val message: String, val isRecoverable: Boolean = true) : SpeechState()
}

class SpeechManager(private val context: Context) {

    private val TAG = "SpeechManager"
    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _speechState = MutableStateFlow<SpeechState>(SpeechState.Idle)
    val speechState: StateFlow<SpeechState> = _speechState.asStateFlow()

    private val _liveTranscript = MutableStateFlow("")
    val liveTranscript: StateFlow<String> = _liveTranscript.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    private var currentLanguageCode: String = "en-IN"
    private var isContinuousListeningRequested = false
    private var restartAttemptCount = 0

    val isAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    fun setLanguage(languageCode: String) {
        currentLanguageCode = languageCode
        if (isContinuousListeningRequested) {
            restartListening()
        }
    }

    fun startListening() {
        if (!isAvailable) {
            _speechState.value = SpeechState.Error("Speech recognition is not available on this device", false)
            return
        }

        isContinuousListeningRequested = true
        restartAttemptCount = 0
        initAndStartRecognizer()
    }

    fun stopListening() {
        isContinuousListeningRequested = false
        mainHandler.removeCallbacksAndMessages(null)
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recognizer", e)
        }
        speechRecognizer = null
        _speechState.value = SpeechState.Idle
        _rmsLevel.value = 0f
    }

    fun clearTranscript() {
        _liveTranscript.value = ""
    }

    fun setManualText(text: String) {
        _liveTranscript.value = text.trim()
    }

    private fun initAndStartRecognizer() {
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(createListener())
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguageCode)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, currentLanguageCode)
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, currentLanguageCode)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                    // Request continuous listening
                    putExtra("android.speech.extra.DICTATION_MODE", true)
                }

                _speechState.value = SpeechState.Initializing
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start listening", e)
                _speechState.value = SpeechState.Error("Failed to initialize microphone: ${e.localizedMessage}")
                handleAutoRestart()
            }
        }
    }

    private fun restartListening() {
        if (!isContinuousListeningRequested) return
        mainHandler.postDelayed({
            if (isContinuousListeningRequested) {
                initAndStartRecognizer()
            }
        }, 200)
    }

    private fun handleAutoRestart() {
        if (!isContinuousListeningRequested) return
        restartAttemptCount++
        val delayMs = if (restartAttemptCount > 5) 1000L else 300L
        mainHandler.postDelayed({
            if (isContinuousListeningRequested) {
                initAndStartRecognizer()
            }
        }, delayMs)
    }

    private fun createListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _speechState.value = SpeechState.Listening
            restartAttemptCount = 0
        }

        override fun onBeginningOfSpeech() {
            _speechState.value = SpeechState.Listening
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Normalized RMS between 0.0 and 1.0 (typical rmsdB ranges from -2 to 10)
            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
            _rmsLevel.value = normalized
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            _rmsLevel.value = 0f
        }

        override fun onError(error: Int) {
            _rmsLevel.value = 0f
            val (message, shouldRestart) = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error" to true
                SpeechRecognizer.ERROR_CLIENT -> "Client error" to true
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required" to false
                SpeechRecognizer.ERROR_NETWORK -> "Network connection issue" to true
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timed out" to true
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected" to true
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Microphone busy" to true
                SpeechRecognizer.ERROR_SERVER -> "Server error" to true
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout, listening..." to true
                else -> "Speech recognition error ($error)" to true
            }

            Log.w(TAG, "SpeechRecognizer error: $error ($message)")
            if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                _speechState.value = SpeechState.Error(message, false)
                isContinuousListeningRequested = false
            } else {
                if (shouldRestart && isContinuousListeningRequested) {
                    handleAutoRestart()
                } else {
                    _speechState.value = SpeechState.Error(message, true)
                }
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val recognizedText = matches[0].uppercase(Locale.getDefault())
                if (recognizedText.isNotBlank()) {
                    _liveTranscript.value = recognizedText
                }
            }
            if (isContinuousListeningRequested) {
                restartListening()
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val partialText = matches[0].uppercase(Locale.getDefault())
                if (partialText.isNotBlank()) {
                    _liveTranscript.value = partialText
                }
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    fun destroy() {
        stopListening()
    }
}
