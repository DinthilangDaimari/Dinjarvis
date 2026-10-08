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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.ArcReactorCore
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

@Composable
fun DashboardScreen(
    viewModel: JarvisViewModel,
    onOpenVoiceSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isListening by viewModel.voiceSpeechManager.isListening.collectAsState()
    val isSpeaking by viewModel.voiceSpeechManager.isSpeaking.collectAsState()
    val rmsLevel by viewModel.voiceSpeechManager.speechRmsLevel.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val lastResponse by viewModel.lastSpokenResponse.collectAsState()
    val lastThought by viewModel.lastInternalThought.collectAsState()

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

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isListening) "Tap core to stop listening" else "Tap core or say 'Good morning Jarvis'",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 12.sp,
                        color = JarvisTextSecondary
                    )
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
