package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.HomeScreenDialog
import com.example.ui.components.JarvisBottomNav
import com.example.ui.components.JarvisTopBar
import com.example.ui.components.VocalSynthesizerDialog
import com.example.ui.components.VoiceCommandBar
import com.example.ui.components.WebAppDirectInstallDialog
import com.example.service.JarvisWebCompanionServer
import com.example.ui.screens.AppLauncherScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.EmailScreen
import com.example.ui.screens.PhoneScreen
import com.example.ui.screens.SmartHomeScreen
import com.example.ui.theme.JarvisBg
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.JarvisNavigationScreen
import com.example.ui.viewmodel.JarvisUiEvent
import com.example.ui.viewmodel.JarvisViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val currentIntentFlow = mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentIntentFlow.value = intent
        com.example.util.HomeScreenHelper.initializeDynamicShortcuts(this)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                JarvisMainApp(incomingIntent = currentIntentFlow.value)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentIntentFlow.value = intent
    }
}

@Composable
fun JarvisMainApp(incomingIntent: Intent? = null) {
    val context = LocalContext.current
    val viewModel: JarvisViewModel = viewModel()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isListening by viewModel.voiceSpeechManager.isListening.collectAsState()
    val isSpeaking by viewModel.voiceSpeechManager.isSpeaking.collectAsState()
    val ttsEnabled by viewModel.voiceSpeechManager.ttsEnabled.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()

    val partialSpeechText by viewModel.voiceSpeechManager.partialSpeechText.collectAsState()
    val speechError by viewModel.voiceSpeechManager.speechError.collectAsState()
    val speechRmsLevel by viewModel.voiceSpeechManager.speechRmsLevel.collectAsState()

    var showVoiceSettingsDialog by remember { mutableStateOf(false) }
    var showHomeScreenDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Fallback system speech recognition activity launcher
    val speechActivityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        val text = matches?.firstOrNull()?.trim()
        if (!text.isNullOrEmpty()) {
            viewModel.voiceSpeechManager.processDirectSpeechResult(text)
        }
    }

    // Audio recording permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                viewModel.toggleVoiceListening()
            } else {
                speechActivityLauncher.launch(viewModel.voiceSpeechManager.createRecognizerIntent())
            }
        } else {
            Toast.makeText(context, "Microphone permission needed for speech recognition", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle incoming Home Screen widget or shortcut intents
    LaunchedEffect(incomingIntent) {
        incomingIntent?.let { intent ->
            val autoListen = intent.getBooleanExtra("auto_listen", false)
            val shortcutAction = intent.getStringExtra("shortcut_action") ?: intent.getStringExtra("quick_command")
            if (autoListen) {
                delay(800)
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                if (hasPermission) {
                    viewModel.toggleVoiceListening()
                } else {
                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            } else if (!shortcutAction.isNullOrBlank()) {
                delay(400)
                when (shortcutAction) {
                    "schedule_meeting" -> {
                        viewModel.handleScheduleMeetingShortcut()
                    }
                    "toggle_smart_lights", "toggle_lights" -> {
                        viewModel.handleToggleLightsShortcut()
                    }
                    "set_climate_72" -> viewModel.executeCommand("set thermostat to 72 degrees")
                    "engage_shield" -> viewModel.executeCommand("activate perimeter defense shield")
                    else -> viewModel.executeCommand(shortcutAction)
                }
            }
        }
    }

    // Startup vocal greeting from Jarvis via Android TextToSpeech
    LaunchedEffect(Unit) {
        delay(600)
        viewModel.voiceSpeechManager.speak("All systems nominal, Sir. Vocal synthesizer online. How may I assist you?")
    }

    // Handle back press
    BackHandler(enabled = currentScreen !is JarvisNavigationScreen.Dashboard) {
        viewModel.navigateTo(JarvisNavigationScreen.Dashboard)
    }

    // Observe UI events (Toast, Launch Intent)
    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is JarvisUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is JarvisUiEvent.LaunchIntent -> {
                    try {
                        context.startActivity(event.intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Unable to launch: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
                is JarvisUiEvent.RequestRecordAudio -> {
                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBg),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Box(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
                JarvisTopBar(
                    isSpeaking = isSpeaking,
                    ttsEnabled = ttsEnabled,
                    onToggleTts = { viewModel.toggleVoiceSpeech() },
                    onVoiceSettingsClick = { showVoiceSettingsDialog = true },
                    onDiagnosticsClick = { viewModel.navigateTo(JarvisNavigationScreen.Diagnostics) },
                    onHomeScreenClick = { showHomeScreenDialog = true }
                )
            }
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                VoiceCommandBar(
                    isListening = isListening,
                    isProcessing = isProcessing,
                    partialSpeechText = partialSpeechText,
                    speechError = speechError,
                    rmsLevel = speechRmsLevel,
                    onMicClick = {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                                viewModel.toggleVoiceListening()
                            } else {
                                speechActivityLauncher.launch(viewModel.voiceSpeechManager.createRecognizerIntent())
                            }
                        } else {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onSubmitCommand = { command ->
                        viewModel.executeCommand(command)
                    }
                )

                JarvisBottomNav(
                    currentScreen = currentScreen,
                    onSelectScreen = { screen ->
                        viewModel.navigateTo(screen)
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(JarvisBg)
        ) {
            when (currentScreen) {
                is JarvisNavigationScreen.Dashboard -> DashboardScreen(
                    viewModel = viewModel,
                    onOpenVoiceSettings = { showVoiceSettingsDialog = true },
                    onOpenHomeScreenDialog = { showHomeScreenDialog = true }
                )
                is JarvisNavigationScreen.Calendar -> CalendarScreen(viewModel = viewModel)
                is JarvisNavigationScreen.Emails -> EmailScreen(viewModel = viewModel)
                is JarvisNavigationScreen.Phone -> PhoneScreen(viewModel = viewModel)
                is JarvisNavigationScreen.Apps -> AppLauncherScreen(viewModel = viewModel)
                is JarvisNavigationScreen.SmartHome -> SmartHomeScreen(viewModel = viewModel)
                is JarvisNavigationScreen.Diagnostics -> DiagnosticsScreen(viewModel = viewModel)
            }

            // Voice Synthesizer Configuration Dialog
            if (showVoiceSettingsDialog) {
                VocalSynthesizerDialog(
                    viewModel = viewModel,
                    onDismiss = { showVoiceSettingsDialog = false }
                )
            }

            // Home Screen Setup & Pinning Dialog
            if (showHomeScreenDialog) {
                HomeScreenDialog(
                    onDismiss = { showHomeScreenDialog = false }
                )
            }
        }
    }
}
