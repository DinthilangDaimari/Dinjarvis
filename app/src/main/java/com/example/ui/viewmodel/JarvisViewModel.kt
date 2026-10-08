package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.JarvisAiEngine
import com.example.ai.ToolAction
import com.example.ai.VoiceSpeechManager
import com.example.data.database.JarvisDatabase
import com.example.data.model.AssistantLog
import com.example.data.model.CalendarEvent
import com.example.data.model.CallLogEntry
import com.example.data.model.ContactPerson
import com.example.data.model.EmailMessage
import com.example.data.model.SmartDevice
import com.example.data.model.SmartRoutine
import com.example.data.repository.JarvisRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class JarvisNavigationScreen {
    object Dashboard : JarvisNavigationScreen()
    object Calendar : JarvisNavigationScreen()
    object Emails : JarvisNavigationScreen()
    object Phone : JarvisNavigationScreen()
    object Apps : JarvisNavigationScreen()
    object SmartHome : JarvisNavigationScreen()
    object Diagnostics : JarvisNavigationScreen()
}

sealed class JarvisUiEvent {
    data class ShowToast(val message: String) : JarvisUiEvent()
    data class LaunchIntent(val intent: Intent) : JarvisUiEvent()
    data class RequestRecordAudio(val reason: String) : JarvisUiEvent()
}

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val database = JarvisDatabase.getDatabase(application, viewModelScope)
    val repository = JarvisRepository(
        calendarDao = database.calendarDao(),
        emailDao = database.emailDao(),
        contactDao = database.contactDao(),
        callLogDao = database.callLogDao(),
        homeDao = database.homeDao(),
        assistantLogDao = database.assistantLogDao()
    )

    private val aiEngine = JarvisAiEngine(repository)

    // Navigation
    private val _currentScreen = MutableStateFlow<JarvisNavigationScreen>(JarvisNavigationScreen.Dashboard)
    val currentScreen: StateFlow<JarvisNavigationScreen> = _currentScreen.asStateFlow()

    // Voice & Speech Manager
    val voiceSpeechManager = VoiceSpeechManager(application) { spokenText ->
        executeCommand(spokenText)
    }

    // Assistant State
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _lastSpokenResponse = MutableStateFlow("All systems nominal, Sir. How may I assist you?")
    val lastSpokenResponse: StateFlow<String> = _lastSpokenResponse.asStateFlow()

    private val _lastInternalThought = MutableStateFlow("Core telemetry active. Awaiting voice input.")
    val lastInternalThought: StateFlow<String> = _lastInternalThought.asStateFlow()

    private val _reactorOutput = MutableStateFlow(99.4f)
    val reactorOutput: StateFlow<Float> = _reactorOutput.asStateFlow()

    // Events Flow
    private val _uiEvents = MutableSharedFlow<JarvisUiEvent>()
    val uiEvents: SharedFlow<JarvisUiEvent> = _uiEvents.asSharedFlow()

    // Data streams from repository
    val events: StateFlow<List<CalendarEvent>> = repository.events.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val emails: StateFlow<List<EmailMessage>> = repository.emails.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val contacts: StateFlow<List<ContactPerson>> = repository.contacts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val callLogs: StateFlow<List<CallLogEntry>> = repository.callLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val devices: StateFlow<List<SmartDevice>> = repository.devices.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val routines: StateFlow<List<SmartRoutine>> = repository.routines.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recentLogs: StateFlow<List<AssistantLog>> = repository.recentLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun navigateTo(screen: JarvisNavigationScreen) {
        _currentScreen.value = screen
    }

    fun executeCommand(command: String) {
        if (command.isBlank()) return
        viewModelScope.launch {
            _isProcessing.value = true
            _lastInternalThought.value = "Interpreting: '$command'..."

            val result = aiEngine.processCommand(command)

            _lastSpokenResponse.value = result.spokenResponse
            _lastInternalThought.value = if (result.thought.isNotEmpty()) result.thought else "Executing directives."

            // Execute dispatched tool actions
            for (action in result.actions) {
                applyAction(action)
            }

            // Speak response via Android TextToSpeech
            voiceSpeechManager.speak(result.spokenResponse)

            // Log to database
            repository.logAssistantAction(
                input = command,
                response = result.spokenResponse,
                actionType = result.actions.firstOrNull()?.actionType ?: "VOICE_COMMAND"
            )

            _isProcessing.value = false
        }
    }

    private suspend fun applyAction(action: ToolAction) {
        when (action.actionType) {
            "SCHEDULE_EVENT" -> {
                val newEvent = CalendarEvent(
                    title = action.title ?: "General Briefing",
                    date = action.date ?: "2026-10-10",
                    time = action.time ?: "03:00 PM",
                    location = action.location ?: "Avengers Tower Ops"
                )
                repository.addEvent(newEvent)
                _uiEvents.emit(JarvisUiEvent.ShowToast("Event scheduled: ${newEvent.title}"))
            }

            "DELETE_EVENT" -> {
                val current = events.value.firstOrNull()
                if (current != null) {
                    repository.deleteEvent(current)
                    _uiEvents.emit(JarvisUiEvent.ShowToast("Event removed from schedule."))
                }
            }

            "SEND_EMAIL" -> {
                val newEmail = EmailMessage(
                    senderName = "Tony Stark",
                    senderEmail = "tony@stark.industries",
                    recipientEmail = action.recipient ?: "pepper@stark.industries",
                    subject = action.subject ?: "Stark Directive",
                    body = action.body ?: "Dispatched via J.A.R.V.I.S. voice relay.",
                    dateFormatted = "Just now",
                    isRead = true,
                    isSent = true,
                    priority = "High"
                )
                repository.addEmail(newEmail)
                _uiEvents.emit(JarvisUiEvent.ShowToast("Transmission sent to ${newEmail.recipientEmail}"))
            }

            "READ_EMAILS" -> {
                _currentScreen.value = JarvisNavigationScreen.Emails
            }

            "CALL_PHONE" -> {
                val phone = action.phoneNumber ?: "+15550192831"
                val contactName = action.contactName ?: "Contact"
                repository.addCallLog(
                    CallLogEntry(
                        contactName = contactName,
                        phone = phone,
                        type = "OUTGOING",
                        timeFormatted = "Just now",
                        durationSeconds = 0
                    )
                )
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phone")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                _uiEvents.emit(JarvisUiEvent.LaunchIntent(dialIntent))
            }

            "LAUNCH_APP" -> {
                val intent = resolveAppIntent(action.appName, action.packageName)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    _uiEvents.emit(JarvisUiEvent.LaunchIntent(intent))
                } else {
                    _uiEvents.emit(JarvisUiEvent.ShowToast("Initializing interface for ${action.appName}"))
                }
            }

            "SET_HOME_DEVICE" -> {
                val id = action.deviceId ?: "light_living_room"
                if (action.turnOn != null) {
                    repository.setDevicePower(id, action.turnOn)
                }
                if (action.numericValue != null) {
                    repository.setDeviceValue(id, action.numericValue)
                }
                _uiEvents.emit(JarvisUiEvent.ShowToast("Device state updated."))
            }

            "RUN_ROUTINE" -> {
                val routineId = action.routineId ?: "routine_lab_focus"
                repository.toggleRoutine(routineId, true)
                _uiEvents.emit(JarvisUiEvent.ShowToast("Protocol sequence activated."))
            }

            "DIAGNOSTICS" -> {
                _currentScreen.value = JarvisNavigationScreen.Diagnostics
            }

            else -> {
                // SPEAK_ONLY
            }
        }
    }

    private fun resolveAppIntent(appName: String?, packageName: String?): Intent? {
        val pm = getApplication<Application>().packageManager
        if (!packageName.isNullOrBlank()) {
            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) return launchIntent
        }

        val name = (appName ?: "").lowercase()
        return when {
            name.contains("youtube") -> pm.getLaunchIntentForPackage("com.google.android.youtube")
                ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com"))
            name.contains("map") -> pm.getLaunchIntentForPackage("com.google.android.apps.maps")
                ?: Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=Avengers+Tower"))
            name.contains("chrome") || name.contains("browser") -> pm.getLaunchIntentForPackage("com.android.chrome")
                ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com"))
            name.contains("camera") -> Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            name.contains("clock") || name.contains("alarm") -> Intent(AlarmClock.ACTION_SHOW_ALARMS)
            name.contains("calc") -> pm.getLaunchIntentForPackage("com.google.android.calculator")
            else -> null
        }
    }

    // Direct UI interactions
    fun toggleDevicePower(id: String, currentState: Boolean) {
        viewModelScope.launch {
            repository.setDevicePower(id, !currentState)
        }
    }

    fun setDeviceValue(id: String, value: Float) {
        viewModelScope.launch {
            repository.setDeviceValue(id, value)
        }
    }

    fun toggleRoutine(id: String, currentState: Boolean) {
        viewModelScope.launch {
            repository.toggleRoutine(id, !currentState)
            if (!currentState) {
                voiceSpeechManager.speak("Protocol sequence engaged, Sir.")
            }
        }
    }

    fun addNewEvent(title: String, date: String, time: String, location: String, category: String) {
        viewModelScope.launch {
            repository.addEvent(
                CalendarEvent(
                    title = title,
                    date = date,
                    time = time,
                    location = location,
                    category = category
                )
            )
            voiceSpeechManager.speak("Event '$title' registered to your calendar schedule.")
        }
    }

    fun deleteEvent(event: CalendarEvent) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }

    fun sendEmail(recipient: String, subject: String, body: String) {
        viewModelScope.launch {
            repository.addEmail(
                EmailMessage(
                    senderName = "Tony Stark",
                    senderEmail = "tony@stark.industries",
                    recipientEmail = recipient,
                    subject = subject,
                    body = body,
                    dateFormatted = "Just now",
                    isRead = true,
                    isSent = true,
                    priority = "High"
                )
            )
            voiceSpeechManager.speak("Transmission dispatched to $recipient.")
        }
    }

    fun toggleEmailStar(id: Long) {
        viewModelScope.launch {
            repository.toggleEmailStar(id)
        }
    }

    fun markEmailAsRead(id: Long, isRead: Boolean) {
        viewModelScope.launch {
            repository.setReadStatus(id, isRead)
        }
    }

    fun dialContact(contact: ContactPerson) {
        viewModelScope.launch {
            repository.addCallLog(
                CallLogEntry(
                    contactName = contact.name,
                    phone = contact.phone,
                    type = "OUTGOING",
                    timeFormatted = "Just now",
                    durationSeconds = 0
                )
            )
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${contact.phone}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            _uiEvents.emit(JarvisUiEvent.LaunchIntent(dialIntent))
        }
    }

    fun launchAppByName(name: String, pkg: String? = null) {
        val intent = resolveAppIntent(name, pkg)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            viewModelScope.launch {
                _uiEvents.emit(JarvisUiEvent.LaunchIntent(intent))
            }
        } else {
            viewModelScope.launch {
                _uiEvents.emit(JarvisUiEvent.ShowToast("Launching $name..."))
            }
        }
    }

    fun toggleVoiceListening() {
        if (voiceSpeechManager.isListening.value) {
            voiceSpeechManager.stopListening()
        } else {
            voiceSpeechManager.stopSpeaking()
            voiceSpeechManager.startListening()
        }
    }

    fun toggleVoiceSpeech() {
        val enabled = voiceSpeechManager.toggleTts()
        viewModelScope.launch {
            _uiEvents.emit(JarvisUiEvent.ShowToast(if (enabled) "Audio response voice enabled" else "Audio response voice muted"))
        }
    }

    fun speakText(text: String) {
        voiceSpeechManager.speak(text)
    }

    fun stopSpeaking() {
        voiceSpeechManager.stopSpeaking()
    }

    fun testTtsVoice() {
        voiceSpeechManager.speak("Testing audio synthesis. All vocal drivers operational, Sir. Text-to-Speech audio synthesizer online.")
    }

    fun setTtsPitch(pitch: Float) {
        voiceSpeechManager.setPitch(pitch)
    }

    fun setTtsSpeedRate(rate: Float) {
        voiceSpeechManager.setSpeedRate(rate)
    }

    fun setTtsVoiceLanguage(language: String) {
        voiceSpeechManager.setVoiceLanguage(language)
    }

    override fun onCleared() {
        super.onCleared()
        voiceSpeechManager.destroy()
    }
}
