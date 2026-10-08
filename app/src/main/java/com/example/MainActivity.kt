package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.JarvisBottomNav
import com.example.ui.components.JarvisTopBar
import com.example.ui.components.VocalSynthesizerDialog
import com.example.ui.components.VoiceCommandBar
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
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                JarvisMainApp()
            }
        }
    }
}

@Composable
fun JarvisMainApp() {
    val context = LocalContext.current
    val viewModel: JarvisViewModel = viewModel()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isListening by viewModel.voiceSpeechManager.isListening.collectAsState()
    val isSpeaking by viewModel.voiceSpeechManager.isSpeaking.collectAsState()
    val ttsEnabled by viewModel.voiceSpeechManager.ttsEnabled.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()

    var showVoiceSettingsDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Audio recording permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleVoiceListening()
        } else {
            Toast.makeText(context, "Microphone permission needed for voice input", Toast.LENGTH_SHORT).show()
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
                    onDiagnosticsClick = { viewModel.navigateTo(JarvisNavigationScreen.Diagnostics) }
                )
            }
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                VoiceCommandBar(
                    isListening = isListening,
                    isProcessing = isProcessing,
                    onMicClick = {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            viewModel.toggleVoiceListening()
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
                    onOpenVoiceSettings = { showVoiceSettingsDialog = true }
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
        }
    }
}
