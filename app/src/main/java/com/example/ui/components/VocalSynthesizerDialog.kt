package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.TtsInitState
import com.example.ui.theme.JarvisAmber
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
import com.example.ui.viewmodel.JarvisViewModel

@Composable
fun VocalSynthesizerDialog(
    viewModel: JarvisViewModel,
    onDismiss: () -> Unit
) {
    val ttsEnabled by viewModel.voiceSpeechManager.ttsEnabled.collectAsState()
    val isSpeaking by viewModel.voiceSpeechManager.isSpeaking.collectAsState()
    val ttsState by viewModel.voiceSpeechManager.ttsState.collectAsState()
    val pitch by viewModel.voiceSpeechManager.pitch.collectAsState()
    val speedRate by viewModel.voiceSpeechManager.speedRate.collectAsState()
    val selectedVoiceLanguage by viewModel.voiceSpeechManager.selectedVoiceLanguage.collectAsState()

    val languageOptions = listOf(
        "British English (UK)",
        "American English (US)",
        "Australian English (AU)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(JarvisCyan.copy(alpha = 0.2f))
                            .border(1.dp, JarvisCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = JarvisCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VOCAL SYNTHESIZER",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisCyan
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (ttsState) {
                                TtsInitState.READY -> JarvisOnline.copy(alpha = 0.2f)
                                TtsInitState.INITIALIZING -> JarvisAmber.copy(alpha = 0.2f)
                                TtsInitState.ERROR -> Color.Red.copy(alpha = 0.2f)
                            }
                        )
                        .border(
                            0.5.dp,
                            when (ttsState) {
                                TtsInitState.READY -> JarvisOnline
                                TtsInitState.INITIALIZING -> JarvisAmber
                                TtsInitState.ERROR -> Color.Red
                            },
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = when (ttsState) {
                            TtsInitState.READY -> "ONLINE"
                            TtsInitState.INITIALIZING -> "INITIALIZING"
                            TtsInitState.ERROR -> "OFFLINE"
                        },
                        fontFamily = OrbitronFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (ttsState) {
                            TtsInitState.READY -> JarvisOnline
                            TtsInitState.INITIALIZING -> JarvisAmber
                            TtsInitState.ERROR -> Color.Red
                        }
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Audio response toggle
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = JarvisSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, JarvisCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (ttsEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                contentDescription = null,
                                tint = if (ttsEnabled) JarvisCyan else JarvisTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Spoken Audio Output",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JarvisTextPrimary
                                )
                                Text(
                                    text = if (ttsEnabled) "Voice synthesis active" else "Voice muted",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 11.sp,
                                    color = JarvisTextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = ttsEnabled,
                            onCheckedChange = { viewModel.toggleVoiceSpeech() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = JarvisCyan,
                                uncheckedThumbColor = JarvisTextSecondary,
                                uncheckedTrackColor = JarvisSurface
                            ),
                            modifier = Modifier.testTag("tts_modal_switch")
                        )
                    }
                }

                // Accent selector
                Column {
                    Text(
                        text = "ACCENT / LOCALE",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 11.sp,
                        color = JarvisTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        languageOptions.forEach { lang ->
                            val isSelected = selectedVoiceLanguage == lang
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) JarvisCyan.copy(alpha = 0.2f) else JarvisSurfaceVariant)
                                    .border(1.dp, if (isSelected) JarvisCyan else JarvisCardBorder, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setTtsVoiceLanguage(lang) }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = lang.substringBefore(" "),
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) JarvisCyan else JarvisTextSecondary
                                )
                            }
                        }
                    }
                }

                // Pitch slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "VOICE PITCH",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 11.sp,
                            color = JarvisTextSecondary
                        )
                        Text(
                            text = String.format("%.2fx", pitch),
                            fontFamily = OrbitronFontFamily,
                            fontSize = 11.sp,
                            color = JarvisCyan
                        )
                    }
                    Slider(
                        value = pitch,
                        onValueChange = { viewModel.setTtsPitch(it) },
                        valueRange = 0.6f..1.4f,
                        colors = SliderDefaults.colors(
                            thumbColor = JarvisCyan,
                            activeTrackColor = JarvisCyan,
                            inactiveTrackColor = JarvisSurfaceVariant
                        )
                    )
                }

                // Speech cadence / speed rate slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "SPEECH CADENCE",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 11.sp,
                            color = JarvisTextSecondary
                        )
                        Text(
                            text = String.format("%.2fx", speedRate),
                            fontFamily = OrbitronFontFamily,
                            fontSize = 11.sp,
                            color = JarvisAmber
                        )
                    }
                    Slider(
                        value = speedRate,
                        onValueChange = { viewModel.setTtsSpeedRate(it) },
                        valueRange = 0.7f..1.4f,
                        colors = SliderDefaults.colors(
                            thumbColor = JarvisAmber,
                            activeTrackColor = JarvisAmber,
                            inactiveTrackColor = JarvisSurfaceVariant
                        )
                    )
                }

                // Test vocal transmission button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.testTtsVoice() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("test_voice_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "TEST VOICE",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    if (isSpeaking) {
                        Button(
                            onClick = { viewModel.stopSpeaking() },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisAmber)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "CLOSE",
                    fontFamily = OrbitronFontFamily,
                    color = JarvisCyan
                )
            }
        },
        containerColor = JarvisSurface,
        shape = RoundedCornerShape(18.dp)
    )
}
