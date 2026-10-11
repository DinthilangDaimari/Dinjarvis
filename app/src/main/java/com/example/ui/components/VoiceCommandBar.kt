package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisAlert
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisNeonTeal
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.RajdhaniFontFamily

@Composable
fun VoiceCommandBar(
    isListening: Boolean,
    isProcessing: Boolean,
    partialSpeechText: String = "",
    speechError: String? = null,
    rmsLevel: Float = 0f,
    onMicClick: () -> Unit,
    onSubmitCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }

    val promptSuggestions = listOf(
        "Good morning Jarvis",
        "What's on my calendar today?",
        "Read unread emails",
        "Call Colonel Rhodes",
        "Workshop lights 100%",
        "Set thermostat to 70°F",
        "Initiate Protocol Night Watch",
        "System diagnostics status",
        "Open YouTube",
        "Lock all entrance doors"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(JarvisSurface.copy(alpha = 0.95f))
            .border(
                width = 1.dp,
                color = if (isListening) JarvisAmber.copy(alpha = 0.8f) else JarvisCyan.copy(alpha = 0.3f),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // Live Speech Recognition Status Banner
        AnimatedVisibility(visible = isListening) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(JarvisAmber.copy(alpha = 0.12f))
                    .border(1.dp, JarvisAmber.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
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
                                .background(JarvisAmber)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SPEECH RECOGNIZER ACTIVE",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisAmber
                        )
                    }

                    // Live mic input intensity percentage
                    Text(
                        text = "${(rmsLevel * 100).toInt()}% LEVEL",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (rmsLevel > 0.5f) JarvisAmber else JarvisCyan
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Real-time audio waveform visualizer responding directly to speech input
                AudioWaveformVisualizer(
                    isListening = isListening,
                    rmsLevel = rmsLevel,
                    height = 42.dp,
                    style = WaveformStyle.BARS,
                    primaryColor = JarvisAmber,
                    secondaryColor = JarvisCyan,
                    showTelemetryInfo = false
                )

                if (partialSpeechText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "\"$partialSpeechText\"",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisCyan
                    )
                }
            }
        }

        // Speech Error notice
        AnimatedVisibility(visible = !isListening && speechError != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(JarvisAlert.copy(alpha = 0.15f))
                    .border(0.5.dp, JarvisAlert.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = speechError ?: "",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 12.sp,
                    color = JarvisAmber
                )
            }
        }

        // Quick suggestions carousel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            promptSuggestions.forEach { suggestion ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(JarvisSurfaceVariant)
                        .border(1.dp, JarvisCyan.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .clickable { onSubmitCommand(suggestion) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = suggestion,
                        color = JarvisCyan,
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input row with Mic and Send
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pulse Mic Button with SpeechRecognizer trigger and dynamic volume scaling
            val dynamicScale = if (isListening) 1f + (rmsLevel * 0.16f) else 1f
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clickable(onClick = onMicClick)
                    .testTag("voice_mic_button"),
                contentAlignment = Alignment.Center
            ) {
                // Radiating pulse ring on audio input
                if (isListening && rmsLevel > 0.05f) {
                    val pulseRingBorderWidth = (1.5f + rmsLevel * 1.5f).dp
                    Box(
                        modifier = Modifier
                            .size((50.dp * (1f + rmsLevel * 0.35f)))
                            .border(
                                width = pulseRingBorderWidth,
                                color = JarvisAmber.copy(alpha = (rmsLevel * 0.7f).coerceIn(0.2f, 0.8f)),
                                shape = CircleShape
                            )
                    )
                }

                val micBorderWidth = if (isListening) (1.5f + rmsLevel * 1.5f).dp else 1.5.dp
                Box(
                    modifier = Modifier
                        .size(50.dp * dynamicScale)
                        .clip(CircleShape)
                        .background(
                            if (isListening) {
                                if (rmsLevel > 0.5f) Color(0xFFFF6D00) else JarvisAmber
                            } else JarvisCyan.copy(alpha = 0.15f)
                        )
                        .border(
                            width = micBorderWidth,
                            color = if (isListening) Color.White else JarvisCyan,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (isListening) "Stop Listening" else "Start Speech Recognition",
                        tint = if (isListening) Color.Black else JarvisCyan,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Text input field
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("command_input_field"),
                placeholder = {
                    Text(
                        text = if (isListening) "Listening: speak to J.A.R.V.I.S..." else "Enter command or speak...",
                        color = JarvisTextSecondary,
                        fontSize = 14.sp,
                        fontFamily = RajdhaniFontFamily
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JarvisCyan,
                    unfocusedBorderColor = JarvisCyan.copy(alpha = 0.35f),
                    focusedTextColor = JarvisTextPrimary,
                    unfocusedTextColor = JarvisTextPrimary,
                    cursorColor = JarvisCyan
                ),
                trailingIcon = {
                    AnimatedVisibility(visible = textInput.isNotBlank()) {
                        IconButton(
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    val command = textInput
                                    textInput = ""
                                    onSubmitCommand(command)
                                }
                            },
                            modifier = Modifier.testTag("send_command_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = JarvisCyan
                            )
                        }
                    }
                }
            )
        }
    }
}
