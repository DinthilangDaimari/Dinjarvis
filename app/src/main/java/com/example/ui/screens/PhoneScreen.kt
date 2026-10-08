package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.data.model.CallLogEntry
import com.example.data.model.ContactPerson
import com.example.ui.theme.JarvisAlert
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBg
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisOnline
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.viewmodel.JarvisViewModel

@Composable
fun PhoneScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val contacts by viewModel.contacts.collectAsState()
    val callLogs by viewModel.callLogs.collectAsState()
    var showDialpadDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBg)
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CELLULAR & COMMS",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = JarvisCyan
                )
                Text(
                    text = "Secure satellite frequency link active",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 13.sp,
                    color = JarvisTextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(JarvisCyan.copy(alpha = 0.2f))
                    .border(1.dp, JarvisCyan, RoundedCornerShape(12.dp))
                    .clickable { showDialpadDialog = true }
                    .testTag("keypad_dial_button")
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Dialpad,
                        contentDescription = "Keypad",
                        tint = JarvisCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "DIALPAD",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisCyan
                    )
                }
            }
        }

        // Voice instruction card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisSurfaceVariant),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, JarvisCyan.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = JarvisAmber,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Voice command: \"Jarvis, call Colonel Rhodes\" or \"Call Pepper\"",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 12.sp,
                    color = JarvisTextPrimary
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Speed Dial Contacts
            item {
                Text(
                    text = "PRIORITY CONTACTS",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 12.sp,
                    color = JarvisTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            items(contacts) { contact ->
                ContactCard(
                    contact = contact,
                    onCall = { viewModel.dialContact(contact) }
                )
            }

            // Call Logs Section
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "COMMUNICATION LOGS",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 12.sp,
                    color = JarvisTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            items(callLogs) { log ->
                CallLogCard(log = log)
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Direct Dialpad Dialog
    if (showDialpadDialog) {
        var phoneNumber by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showDialpadDialog = false },
            title = {
                Text(
                    text = "DIRECT FREQUENCY DIAL",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 16.sp,
                    color = JarvisCyan
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number or Extension") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (phoneNumber.isNotBlank()) {
                            viewModel.dialContact(
                                ContactPerson(
                                    name = "Direct Dial",
                                    phone = phoneNumber,
                                    email = "",
                                    role = "Manual Entry"
                                )
                            )
                            showDialpadDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                ) {
                    Text("CONNECT", color = Color.Black, fontFamily = OrbitronFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialpadDialog = false }) {
                    Text("CANCEL", color = JarvisTextSecondary, fontFamily = OrbitronFontFamily)
                }
            },
            containerColor = JarvisSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun ContactCard(
    contact: ContactPerson,
    onCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("contact_card_${contact.name.replace(" ", "_")}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = JarvisSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Avatar circle
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(JarvisCyan.copy(alpha = 0.2f))
                        .border(1.dp, JarvisCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.name.take(2).uppercase(),
                        fontFamily = OrbitronFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisCyan
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = contact.name,
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisTextPrimary
                    )
                    Text(
                        text = contact.role,
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 12.sp,
                        color = JarvisAmber
                    )
                    Text(
                        text = contact.phone,
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 12.sp,
                        color = JarvisTextSecondary
                    )
                }
            }

            // Dial Call Button
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(JarvisOnline.copy(alpha = 0.2f))
                    .border(1.dp, JarvisOnline, CircleShape)
                    .clickable(onClick = onCall)
                    .padding(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Dial ${contact.name}",
                    tint = JarvisOnline,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CallLogCard(
    log: CallLogEntry,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = JarvisSurface),
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
                    imageVector = when (log.type) {
                        "INCOMING" -> Icons.AutoMirrored.Filled.CallReceived
                        "OUTGOING" -> Icons.AutoMirrored.Filled.CallMade
                        else -> Icons.Default.CallEnd
                    },
                    contentDescription = log.type,
                    tint = when (log.type) {
                        "INCOMING" -> JarvisOnline
                        "OUTGOING" -> JarvisCyan
                        else -> JarvisAlert
                    },
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = log.contactName,
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisTextPrimary
                    )
                    Text(
                        text = "${log.type} • ${log.timeFormatted}",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 11.sp,
                        color = JarvisTextSecondary
                    )
                }
            }

            Text(
                text = log.phone,
                fontFamily = RajdhaniFontFamily,
                fontSize = 12.sp,
                color = JarvisTextSecondary
            )
        }
    }
}
