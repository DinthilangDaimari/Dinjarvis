package com.example.ai

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

enum class TtsInitState {
    INITIALIZING,
    READY,
    ERROR
}

class VoiceSpeechManager(
    private val context: Context,
    private val onVoiceInputFinal: (String) -> Unit
) : TextToSpeech.OnInitListener {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var textToSpeech: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var pendingSpeechText: String? = null
    private var waveAnimationJob: Job? = null

    private val _ttsState = MutableStateFlow(TtsInitState.INITIALIZING)
    val ttsState: StateFlow<TtsInitState> = _ttsState.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speechRmsLevel = MutableStateFlow(0f)
    val speechRmsLevel: StateFlow<Float> = _speechRmsLevel.asStateFlow()

    private val _ttsEnabled = MutableStateFlow(true)
    val ttsEnabled: StateFlow<Boolean> = _ttsEnabled.asStateFlow()

    private val _pitch = MutableStateFlow(0.95f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val _speedRate = MutableStateFlow(1.02f)
    val speedRate: StateFlow<Float> = _speedRate.asStateFlow()

    private val _selectedVoiceLanguage = MutableStateFlow("British English (UK)")
    val selectedVoiceLanguage: StateFlow<String> = _selectedVoiceLanguage.asStateFlow()

    init {
        initTtsEngine()
        initSpeechRecognizer()
    }

    private fun initTtsEngine() {
        try {
            _ttsState.value = TtsInitState.INITIALIZING
            textToSpeech = TextToSpeech(context.applicationContext, this)
        } catch (_: Exception) {
            _ttsState.value = TtsInitState.ERROR
        }
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _isListening.value = true
                        }

                        override fun onBeginningOfSpeech() {
                            _isListening.value = true
                        }

                        override fun onRmsChanged(rmsdB: Float) {
                            _speechRmsLevel.value = ((rmsdB + 2f).coerceIn(0f, 10f) / 10f)
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            _isListening.value = false
                            _speechRmsLevel.value = 0f
                        }

                        override fun onError(error: Int) {
                            _isListening.value = false
                            _speechRmsLevel.value = 0f
                        }

                        override fun onResults(results: Bundle?) {
                            _isListening.value = false
                            _speechRmsLevel.value = 0f
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val text = matches?.firstOrNull()?.trim()
                            if (!text.isNullOrEmpty()) {
                                onVoiceInputFinal(text)
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {}

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }
            } catch (_: Exception) {}
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            configureTtsParameters()
            _ttsState.value = TtsInitState.READY

            // If an utterance was queued while initializing, speak it now!
            val pending = pendingSpeechText
            if (!pending.isNullOrBlank()) {
                pendingSpeechText = null
                speak(pending)
            }
        } else {
            _ttsState.value = TtsInitState.ERROR
        }
    }

    private fun configureTtsParameters() {
        val tts = textToSpeech ?: return

        // Set Audio Attributes for clear assistant vocal channel
        try {
            val usage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AudioAttributes.USAGE_ASSISTANT
            } else {
                AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE
            }
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(usage)
                .build()
            tts.setAudioAttributes(audioAttributes)
        } catch (_: Exception) {}

        // Apply selected language
        val locale = when (_selectedVoiceLanguage.value) {
            "American English (US)" -> Locale.US
            "Australian English (AU)" -> Locale.Builder().setLanguage("en").setRegion("AU").build()
            else -> Locale.UK
        }

        val result = tts.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts.setLanguage(Locale.US)
        }

        // Apply pitch and speed
        tts.setPitch(_pitch.value)
        tts.setSpeechRate(_speedRate.value)

        // Progress listener
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
                startSpeakingWaveSimulation()
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
                stopSpeakingWaveSimulation()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                stopSpeakingWaveSimulation()
            }
        })
    }

    private fun startSpeakingWaveSimulation() {
        waveAnimationJob?.cancel()
        waveAnimationJob = scope.launch {
            var phase = 0f
            while (isActive && _isSpeaking.value) {
                phase += 0.35f
                val simulatedRms = (0.35f + 0.55f * kotlin.math.abs(kotlin.math.sin(phase))).coerceIn(0.1f, 1f)
                _speechRmsLevel.value = simulatedRms
                delay(60)
            }
            _speechRmsLevel.value = 0f
        }
    }

    private fun stopSpeakingWaveSimulation() {
        waveAnimationJob?.cancel()
        waveAnimationJob = null
        _speechRmsLevel.value = 0f
    }

    fun speak(text: String) {
        if (!_ttsEnabled.value || text.isBlank()) return

        stopListening()

        if (_ttsState.value != TtsInitState.READY) {
            // Queue utterance to speak as soon as initialization concludes
            pendingSpeechText = text
            if (_ttsState.value == TtsInitState.ERROR) {
                initTtsEngine()
            }
            return
        }

        try {
            val utteranceId = "jarvis_utterance_${System.currentTimeMillis()}"
            val cleanText = sanitizeForSpeech(text)
            textToSpeech?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (_: Exception) {
            _isSpeaking.value = false
        }
    }

    private fun sanitizeForSpeech(text: String): String {
        return text
            .replace(Regex("\\[ID:[0-9]+\\]"), "")
            .replace(Regex("[*#_~`]"), "")
            .replace("//", " ")
            .trim()
    }

    fun stopSpeaking() {
        try {
            textToSpeech?.stop()
        } catch (_: Exception) {}
        _isSpeaking.value = false
        stopSpeakingWaveSimulation()
    }

    fun toggleTts(): Boolean {
        val newState = !_ttsEnabled.value
        _ttsEnabled.value = newState
        if (!newState) {
            stopSpeaking()
        }
        return newState
    }

    fun setPitch(newPitch: Float) {
        _pitch.value = newPitch.coerceIn(0.5f, 1.8f)
        textToSpeech?.setPitch(_pitch.value)
    }

    fun setSpeedRate(newSpeed: Float) {
        _speedRate.value = newSpeed.coerceIn(0.5f, 1.8f)
        textToSpeech?.setSpeechRate(_speedRate.value)
    }

    fun setVoiceLanguage(language: String) {
        _selectedVoiceLanguage.value = language
        val locale = when (language) {
            "American English (US)" -> Locale.US
            "Australian English (AU)" -> Locale.Builder().setLanguage("en").setRegion("AU").build()
            else -> Locale.UK
        }
        textToSpeech?.setLanguage(locale)
    }

    fun startListening() {
        stopSpeaking()
        if (speechRecognizer == null) {
            initSpeechRecognizer()
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Jarvis...")
        }
        try {
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (_: Exception) {
            _isListening.value = false
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
        _isListening.value = false
        _speechRmsLevel.value = 0f
    }

    fun destroy() {
        waveAnimationJob?.cancel()
        try {
            speechRecognizer?.destroy()
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (_: Exception) {}
    }
}
