package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import android.widget.Toast
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.service.FloatingJarvisService
import com.example.service.JarvisWebCompanionServer
import com.example.ui.components.ArcReactorCore
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.WaveformStyle
import com.example.ui.theme.JarvisAlert
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBg
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisNeonTeal
import com.example.ui.theme.JarvisOnline
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.viewmodel.JarvisNavigationScreen
import com.example.ui.viewmodel.JarvisViewModel
import com.example.util.DirectInstallHelper
import com.example.util.HomeScreenHelper
import java.io.File

@Composable
fun DashboardScreen(
    viewModel: JarvisViewModel,
    onOpenVoiceSettings: () -> Unit = {},
    onOpenHomeScreenDialog: () -> Unit = {},
    onOpenWebAppDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isListening by viewModel.voiceSpeechManager.isListening.collectAsState()
    val isSpeaking by viewModel.voiceSpeechManager.isSpeaking.collectAsState()
    val rmsLevel by viewModel.voiceSpeechManager.speechRmsLevel.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val partialSpeechText by viewModel.voiceSpeechManager.partialSpeechText.collectAsState()
    val speechError by viewModel.voiceSpeechManager.speechError.collectAsState()
    val lastRecognizedVoiceInput by viewModel.lastRecognizedVoiceInput.collectAsState()
    val isLastGeminiPowered by viewModel.isLastGeminiPowered.collectAsState()
    val lastActionType by viewModel.lastActionType.collectAsState()
    val lastResponse by viewModel.lastSpokenResponse.collectAsState()
    val lastThought by viewModel.lastInternalThought.collectAsState()

    val context = LocalContext.current
    var isOverlayRunning by remember { mutableStateOf(FloatingJarvisService.isServiceRunning) }
    var hasOverlayPermission by remember { mutableStateOf(DirectInstallHelper.hasOverlayPermission(context)) }

    val events by viewModel.events.collectAsState()
    val emails by viewModel.emails.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val devices by viewModel.devices.collectAsState()
    val routines by viewModel.routines.collectAsState()
    val recentLogs by viewModel.recentLogs.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Arc Reactor Interactive Orb Centerpiece
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isListening) "LISTENING TO VOICE DIRECTIVE..."
                        else if (isSpeaking) "J.A.R.V.I.S. VOCAL TRANSMISSION"
                        else if (isProcessing) "PROCESSING DIRECTIVE..."
                        else "ARC REACTOR CORE // READY",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 13.sp,
                        color = if (isListening) JarvisAmber else if (isSpeaking) JarvisNeonTeal else JarvisCyan,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ArcReactorCore(
                        size = 180.dp,
                        isListening = isListening,
                        isSpeaking = isSpeaking,
                        rmsLevel = rmsLevel,
                        onClick = { viewModel.toggleVoiceListening() }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dedicated central Voice Interaction Action Button with Mic icon & glow
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                if (isListening) JarvisAmber.copy(alpha = 0.25f)
                                else if (isSpeaking) JarvisNeonTeal.copy(alpha = 0.25f)
                                else JarvisCyan.copy(alpha = 0.15f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isListening) JarvisAmber else if (isSpeaking) JarvisNeonTeal else JarvisCyan,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .clickable { viewModel.toggleVoiceListening() }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("central_voice_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Mic else if (isSpeaking) Icons.Default.RecordVoiceOver else Icons.Default.Mic,
                                contentDescription = "Voice Interaction",
                                tint = if (isListening) JarvisAmber else if (isSpeaking) JarvisNeonTeal else JarvisCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isListening) "TAP TO STOP LISTENING"
                                else if (isSpeaking) "J.A.R.V.I.S. VOCAL ACTIVE"
                                else "TAP TO ENGAGE VOICE",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isListening) JarvisAmber else if (isSpeaking) JarvisNeonTeal else JarvisCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Real-time audio waveform / pulse banner when speech recognizer is capturing voice
                    AnimatedVisibility(visible = isListening) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            AudioWaveformVisualizer(
                                isListening = isListening,
                                rmsLevel = rmsLevel,
                                height = 48.dp,
                                style = WaveformStyle.SINE_WAVE,
                                primaryColor = JarvisAmber,
                                secondaryColor = JarvisCyan,
                                showTelemetryInfo = true
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            if (partialSpeechText.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(JarvisAmber.copy(alpha = 0.12f))
                                        .border(1.dp, JarvisAmber.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "DIRECTIVE: \"$partialSpeechText\"",
                                        fontFamily = RajdhaniFontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = JarvisAmber
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }

                    // Processing Intent indicator via Gemini API
                    AnimatedVisibility(visible = isProcessing) {
                        Row(
                            modifier = Modifier
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(JarvisCyan.copy(alpha = 0.15f))
                                .border(1.dp, JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = JarvisCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GEMINI API // ANALYZING INTENT DIRECTIVE...",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisCyan
                            )
                        }
                    }

                    if (speechError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(JarvisAlert.copy(alpha = 0.15f))
                                .border(1.dp, JarvisAlert.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable { viewModel.toggleVoiceListening() }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Retry",
                                    tint = JarvisAlert,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$speechError (Tap to retry)",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 12.sp,
                                    color = JarvisAlert,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isListening) "Listening: real-time voice intensity active" else if (isProcessing) "Dispatched recognized text to Gemini API" else "Tap core or button to capture voice input",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 12.sp,
                        color = JarvisTextSecondary
                    )
                }
            }
        }

        // 1b. Direct Frontend App Installation Hub (PWA, APK, Home Screen Pin)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("frontend_install_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, JarvisNeonTeal)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header with Glowing Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(JarvisNeonTeal.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GetApp,
                                    contentDescription = null,
                                    tint = JarvisNeonTeal,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "INSTALL J.A.R.V.I.S. APP",
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JarvisNeonTeal
                                )
                                Text(
                                    text = "Direct Install • APK • PWA Web App • Home Screen",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 11.sp,
                                    color = JarvisTextSecondary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(JarvisNeonTeal)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "INSTALL READY",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Primary 1-Tap Actions: Web App PWA & APK Package Installer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Direct Web App (PWA) Install Button
                        Button(
                            onClick = {
                                DirectInstallHelper.installWebAppPwa(context)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("frontend_install_webapp_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "INSTALL WEB APP",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        // 2. Direct APK Package Install Button
                        Button(
                            onClick = {
                                DirectInstallHelper.installCurrentAppApk(context)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("frontend_install_apk_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisNeonTeal)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GetApp,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "INSTALL APK",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Secondary Row: Pin to Home Screen & Full Install Guide
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 3. Pin to Home Screen Button
                        Button(
                            onClick = {
                                HomeScreenHelper.pinJarvisToHomeScreen(context)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("frontend_pin_homescreen_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisAmber)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = JarvisAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PIN TO HOME",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisAmber
                            )
                        }

                        // 4. Full Guide & QR Code Dialog Button
                        Button(
                            onClick = onOpenWebAppDialog,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("frontend_install_guide_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = JarvisCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "INSTALL GUIDE / QR",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Share APK button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(JarvisBg)
                            .clickable { DirectInstallHelper.shareApk(context) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share APK",
                            tint = JarvisTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Export or Share APK file to other devices",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 12.sp,
                            color = JarvisTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 2. Jarvis Speech Response Console Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("jarvis_response_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = JarvisSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isSpeaking) JarvisAmber else JarvisCyan)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSpeaking) "SPEAKING AUDIO TRANSMISSION" else "VOCAL SYNTHESIZER",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 11.sp,
                                color = if (isSpeaking) JarvisAmber else JarvisCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onOpenVoiceSettings,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = "Voice Settings",
                                    tint = JarvisTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            if (isSpeaking) {
                                IconButton(
                                    onClick = { viewModel.stopSpeaking() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = "Stop Speech",
                                        tint = JarvisAmber,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = { viewModel.voiceSpeechManager.speak(lastResponse) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Speak Response",
                                        tint = JarvisCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (lastRecognizedVoiceInput.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = JarvisAmber,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CAPTURED INPUT: \"$lastRecognizedVoiceInput\"",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = JarvisAmber
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isLastGeminiPowered) JarvisNeonTeal.copy(alpha = 0.2f) else JarvisCyan.copy(alpha = 0.15f))
                                .border(1.dp, if (isLastGeminiPowered) JarvisNeonTeal else JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isLastGeminiPowered) "GEMINI 3.5 FLASH" else "JARVIS AI ENGINE",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLastGeminiPowered) JarvisNeonTeal else JarvisCyan
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(JarvisBg)
                                .border(1.dp, JarvisAmber.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "INTENT: $lastActionType",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisAmber
                            )
                        }
                    }

                    Text(
                        text = "\"$lastResponse\"",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 17.sp,
                        color = JarvisTextPrimary,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 24.sp
                    )

                    // Audio Wave Equalizer Animation Bar
                    if (isSpeaking) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(JarvisBg)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "AUDIO WAVE:",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 10.sp,
                                color = JarvisAmber,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            // 16 animated waveform bars
                            for (i in 0 until 16) {
                                val barHeight = (4 + ((i * 3 + (rmsLevel * 20)).toInt() % 16)).dp
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(barHeight)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (i % 2 == 0) JarvisAmber else JarvisCyan)
                                )
                            }
                        }
                    }

                    if (lastThought.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(JarvisBg.copy(alpha = 0.6f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Telemetry: $lastThought",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 12.sp,
                                color = JarvisTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // 2b. Upcoming Calendar Events - Scrollable Card Layout
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_upcoming_events_section"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(JarvisCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.EventNote,
                                    contentDescription = "Upcoming Calendar Events",
                                    tint = JarvisCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "UPCOMING CALENDAR EVENTS",
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JarvisTextPrimary
                                )
                                Text(
                                    text = "${events.size} scheduled in Stark Operations database",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 11.sp,
                                    color = JarvisTextSecondary
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.handleScheduleMeetingShortcut() },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Schedule New Event",
                                    tint = JarvisAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Text(
                                text = "View All",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisCyan,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { viewModel.navigateTo(JarvisNavigationScreen.Calendar) }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (events.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(JarvisSurfaceVariant)
                                .padding(vertical = 24.dp, horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "NO UPCOMING EVENTS SCHEDULED",
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = JarvisTextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Say \"Jarvis, schedule a meeting tomorrow at 10 AM\" to register one",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 12.sp,
                                    color = JarvisAmber
                                )
                            }
                        }
                    } else {
                        // Scrollable card layout (horizontal carousel of rich event cards)
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("upcoming_calendar_events_scrollable_row"),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(events) { event ->
                                Card(
                                    modifier = Modifier
                                        .width(240.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { viewModel.navigateTo(JarvisNavigationScreen.Calendar) }
                                        .testTag("dashboard_event_card_${event.id}"),
                                    colors = CardDefaults.cardColors(containerColor = JarvisSurfaceVariant),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 1.dp,
                                        color = when (event.category.lowercase()) {
                                            "tactical" -> JarvisAmber.copy(alpha = 0.6f)
                                            "defense" -> Color(0xFFFF5252).copy(alpha = 0.6f)
                                            else -> JarvisCyan.copy(alpha = 0.45f)
                                        }
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        when (event.category.lowercase()) {
                                                            "tactical" -> JarvisAmber.copy(alpha = 0.15f)
                                                            "defense" -> Color(0xFFFF5252).copy(alpha = 0.15f)
                                                            else -> JarvisCyan.copy(alpha = 0.15f)
                                                        }
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = event.category.uppercase(),
                                                    fontFamily = OrbitronFontFamily,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (event.category.lowercase()) {
                                                        "tactical" -> JarvisAmber
                                                        "defense" -> Color(0xFFFF5252)
                                                        else -> JarvisCyan
                                                    }
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(JarvisOnline)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "SYNCED",
                                                    fontFamily = OrbitronFontFamily,
                                                    fontSize = 8.sp,
                                                    color = JarvisOnline
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = event.title,
                                            fontFamily = RajdhaniFontFamily,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = JarvisTextPrimary,
                                            maxLines = 1
                                        )

                                        if (event.description.isNotBlank()) {
                                            Text(
                                                text = event.description,
                                                fontFamily = RajdhaniFontFamily,
                                                fontSize = 11.sp,
                                                color = JarvisTextSecondary,
                                                maxLines = 1
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Schedule,
                                                contentDescription = null,
                                                tint = JarvisAmber,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${event.date} • ${event.time}",
                                                fontFamily = RajdhaniFontFamily,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = JarvisAmber,
                                                maxLines = 1
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = JarvisTextSecondary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = event.location,
                                                fontFamily = RajdhaniFontFamily,
                                                fontSize = 11.sp,
                                                color = JarvisTextSecondary,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2c. Active in Other Apps (Floating Arc Reactor Overlay)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_in_other_apps_section"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isOverlayRunning) JarvisAmber else JarvisCyan.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(JarvisAmber.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = "Active in Other Apps",
                                    tint = JarvisAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "ACTIVE IN OTHER APPS",
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JarvisAmber
                                )
                                Text(
                                    text = "Floating Arc Reactor Overlay & Quick Tile",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 11.sp,
                                    color = JarvisTextSecondary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isOverlayRunning) JarvisOnline.copy(alpha = 0.2f) else JarvisSurfaceVariant)
                                .border(0.5.dp, if (isOverlayRunning) JarvisOnline else JarvisCardBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isOverlayRunning) "OVERLAY ACTIVE" else "STANDBY",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 9.sp,
                                color = if (isOverlayRunning) JarvisOnline else JarvisTextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Summon J.A.R.V.I.S. voice assistant from anywhere on your phone while using YouTube, Chrome, Instagram, Games, or Home Screen without switching apps.",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 13.sp,
                        color = JarvisTextPrimary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!hasOverlayPermission) {
                        Button(
                            onClick = {
                                DirectInstallHelper.requestOverlayPermission(context)
                                hasOverlayPermission = DirectInstallHelper.hasOverlayPermission(context)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dashboard_grant_overlay_perm_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisAmber)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GRANT 'DISPLAY OVER APPS' PERMISSION",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (isOverlayRunning) {
                                        DirectInstallHelper.stopFloatingAssistant(context)
                                        isOverlayRunning = false
                                    } else {
                                        val started = DirectInstallHelper.startFloatingAssistant(context)
                                        if (started) isOverlayRunning = true
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("dashboard_toggle_overlay_btn"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isOverlayRunning) JarvisSurfaceVariant else JarvisAmber
                                ),
                                border = if (isOverlayRunning) androidx.compose.foundation.BorderStroke(1.dp, JarvisAmber) else null
                            ) {
                                Icon(
                                    imageVector = if (isOverlayRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = if (isOverlayRunning) JarvisAmber else Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isOverlayRunning) "DISMISS OVERLAY" else "START OVERLAY",
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOverlayRunning) JarvisAmber else Color.Black
                                )
                            }

                            Button(
                                onClick = onOpenWebAppDialog,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("dashboard_overlay_settings_btn"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant),
                                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = JarvisCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SETTINGS & TILE",
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JarvisCyan
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2d. Web App Companion & Direct Install
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_webapp_direct_install_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisNeonTeal.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(JarvisNeonTeal.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "Web App Companion",
                                    tint = JarvisNeonTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "WEB APP DIRECT INSTALL",
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JarvisNeonTeal
                                )
                                Text(
                                    text = "PWA Standalone & Direct APK Package",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 11.sp,
                                    color = JarvisTextSecondary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(JarvisNeonTeal.copy(alpha = 0.15f))
                                .border(0.5.dp, JarvisNeonTeal, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "INSTALLABLE PWA",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 9.sp,
                                color = JarvisNeonTeal,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Access J.A.R.V.I.S. from any web browser or install directly as a standalone Web App (PWA) with 1 tap. Complete with Web Speech API and offline shell.",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 13.sp,
                        color = JarvisTextPrimary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                JarvisWebCompanionServer.openInBrowser(context)
                                Toast.makeText(
                                    context,
                                    "Launching Web App! Tap 'Install J.A.R.V.I.S.' in browser.",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dashboard_launch_webapp_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LAUNCH WEB APP",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        Button(
                            onClick = onOpenWebAppDialog,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dashboard_open_webapp_hub_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisNeonTeal)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = JarvisNeonTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "INSTALL HUB",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisNeonTeal
                            )
                        }
                    }
                }
            }
        }

        // 3. Quick Domain Controls Grid
        item {
            Text(
                text = "OPERATIONAL DOMAINS",
                fontFamily = OrbitronFontFamily,
                fontSize = 13.sp,
                color = JarvisTextSecondary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        // Next Calendar Meeting & Unread Emails Glance
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Calendar Glance
                val nextEvent = events.firstOrNull()
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { viewModel.navigateTo(JarvisNavigationScreen.Calendar) }
                        .testTag("dashboard_calendar_card"),
                    colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Calendar",
                                tint = JarvisCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "${events.size} Events",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 12.sp,
                                color = JarvisCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Next Schedule",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 11.sp,
                            color = JarvisTextSecondary
                        )
                        Text(
                            text = nextEvent?.title ?: "No upcoming meetings",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisTextPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = nextEvent?.let { "${it.date} • ${it.time}" } ?: "Schedule clear",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 12.sp,
                            color = JarvisAmber
                        )
                    }
                }

                // Email Glance
                val unreadCount = emails.count { !it.isRead }
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { viewModel.navigateTo(JarvisNavigationScreen.Emails) }
                        .testTag("dashboard_email_card"),
                    colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = JarvisNeonTeal,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "$unreadCount Unread",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 12.sp,
                                color = if (unreadCount > 0) JarvisAmber else JarvisOnline,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Latest Comms",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 11.sp,
                            color = JarvisTextSecondary
                        )
                        val latestEmail = emails.firstOrNull()
                        Text(
                            text = latestEmail?.senderName ?: "No transmissions",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisTextPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = latestEmail?.subject ?: "Inbox current",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 12.sp,
                            color = JarvisTextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Smart Home Quick Controls Row
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home",
                                tint = JarvisCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "HOME AUTOMATION RELAYS",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = JarvisTextPrimary
                            )
                        }

                        Text(
                            text = "View All",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisCyan,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { viewModel.navigateTo(JarvisNavigationScreen.SmartHome) }
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4 Quick toggles: Workshop Lights, Living Room, Penthouse Lock, Shield
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        devices.take(4).forEach { device ->
                            val isOn = device.isOn
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isOn) JarvisCyan.copy(alpha = 0.15f) else JarvisSurfaceVariant)
                                    .border(
                                        width = 1.dp,
                                        color = if (isOn) JarvisCyan else JarvisCardBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.toggleDevicePower(device.id, isOn) }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = when (device.type) {
                                            "LIGHT" -> Icons.Default.FlashOn
                                            "LOCK" -> Icons.Default.Security
                                            else -> Icons.Default.CheckCircle
                                        },
                                        contentDescription = device.name,
                                        tint = if (isOn) JarvisCyan else JarvisTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = device.name.split(" ").firstOrNull() ?: device.name,
                                        fontFamily = RajdhaniFontFamily,
                                        fontSize = 11.sp,
                                        color = JarvisTextPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = if (isOn) "ON" else "OFF",
                                        fontFamily = OrbitronFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOn) JarvisCyan else JarvisTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active Protocols / Routines
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "TACTICAL PROTOCOLS",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = JarvisTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    routines.forEach { routine ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(JarvisSurfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = routine.name,
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JarvisTextPrimary
                                )
                                Text(
                                    text = routine.triggerPhrase,
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 11.sp,
                                    color = JarvisCyan
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (routine.isActive) JarvisCyan else JarvisBg)
                                    .border(1.dp, JarvisCyan, RoundedCornerShape(16.dp))
                                    .clickable { viewModel.toggleRoutine(routine.id, routine.isActive) }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (routine.isActive) "ACTIVE" else "ENGAGE",
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (routine.isActive) Color.Black else JarvisCyan
                                )
                            }
                        }
                    }
                }
            }
        }

        // Speed Dial Frequent Contacts
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SPEED DIAL RELAYS",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = JarvisTextPrimary
                        )
                        Text(
                            text = "View Contacts",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisCyan,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { viewModel.navigateTo(JarvisNavigationScreen.Phone) }
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        contacts.take(3).forEach { contact ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(JarvisSurfaceVariant)
                                    .border(1.dp, JarvisCardBorder, RoundedCornerShape(12.dp))
                                    .clickable { viewModel.dialContact(contact) }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Call ${contact.name}",
                                        tint = JarvisCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = contact.name.split(" ").firstOrNull() ?: contact.name,
                                        fontFamily = RajdhaniFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = JarvisTextPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "DIAL",
                                        fontFamily = OrbitronFontFamily,
                                        fontSize = 10.sp,
                                        color = JarvisCyan
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Directive Log
        item {
            Text(
                text = "RECENT DIRECTIVES",
                fontFamily = OrbitronFontFamily,
                fontSize = 13.sp,
                color = JarvisTextSecondary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        items(recentLogs.take(5)) { log ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, JarvisCardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = log.userInput,
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisAmber
                        )
                        Text(
                            text = log.actionType,
                            fontFamily = OrbitronFontFamily,
                            fontSize = 10.sp,
                            color = JarvisCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = log.jarvisResponse,
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 13.sp,
                        color = JarvisTextPrimary
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
