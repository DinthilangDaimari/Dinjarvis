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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBg
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisOnline
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.RajdhaniFontFamily

@Composable
fun JarvisTopBar(
    isSpeaking: Boolean,
    ttsEnabled: Boolean,
    onToggleTts: () -> Unit,
    onVoiceSettingsClick: () -> Unit,
    onDiagnosticsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(JarvisBg)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Branding & Vocal Status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onVoiceSettingsClick)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isSpeaking) JarvisAmber else JarvisOnline)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "J.A.R.V.I.S.",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisCyan
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(JarvisCyan.copy(alpha = 0.15f))
                            .border(0.5.dp, JarvisCyan.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "v8.5",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisCyan
                        )
                    }
                }
                Text(
                    text = if (isSpeaking) "TTS SPEAKING // LIVE" else "STARK OS // ONLINE",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 11.sp,
                    color = if (isSpeaking) JarvisAmber else JarvisTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Right Actions (Voice Synth Settings, TTS toggle, Diagnostics)
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Vocal Settings icon
            IconButton(
                onClick = onVoiceSettingsClick,
                modifier = Modifier.testTag("voice_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.RecordVoiceOver,
                    contentDescription = "Vocal Settings",
                    tint = if (isSpeaking) JarvisAmber else JarvisCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            // TTS audio toggle
            IconButton(
                onClick = onToggleTts,
                modifier = Modifier.testTag("tts_toggle_button")
            ) {
                Icon(
                    imageVector = if (ttsEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                    contentDescription = "Toggle Voice Audio",
                    tint = if (ttsEnabled) JarvisCyan else JarvisTextSecondary
                )
            }

            // Diagnostics Button
            IconButton(
                onClick = onDiagnosticsClick,
                modifier = Modifier.testTag("diagnostics_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Diagnostics",
                    tint = JarvisTextPrimary
                )
            }
        }
    }
}
