package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.EmailMessage
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
import com.example.ui.viewmodel.JarvisViewModel

@Composable
fun EmailScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val emails by viewModel.emails.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    var showComposeDialog by remember { mutableStateOf(false) }
    var activeEmailView by remember { mutableStateOf<EmailMessage?>(null) }

    val filters = listOf("All", "Unread", "Starred", "Sent")
    val filteredEmails = when (selectedFilter) {
        "Unread" -> emails.filter { !it.isRead }
        "Starred" -> emails.filter { it.isStarred }
        "Sent" -> emails.filter { it.isSent }
        else -> emails
    }

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
                    text = "COMMS & INBOX",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = JarvisNeonTeal
                )
                Text(
                    text = "${emails.count { !it.isRead }} unread priority transmissions",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 13.sp,
                    color = JarvisTextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(JarvisNeonTeal.copy(alpha = 0.2f))
                    .border(1.dp, JarvisNeonTeal, RoundedCornerShape(12.dp))
                    .clickable { showComposeDialog = true }
                    .testTag("compose_email_button")
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Compose",
                        tint = JarvisNeonTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "COMPOSE",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisNeonTeal
                    )
                }
            }
        }

        // Voice directive tip
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisSurfaceVariant),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, JarvisNeonTeal.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = JarvisNeonTeal,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Voice command: \"Jarvis, read my unread emails\" or \"Send an email to Pepper\"",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 12.sp,
                    color = JarvisTextPrimary
                )
            }
        }

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) JarvisNeonTeal else JarvisSurface)
                        .border(1.dp, if (isSelected) JarvisNeonTeal else JarvisCardBorder, RoundedCornerShape(10.dp))
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = filter,
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.Black else JarvisTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Email list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredEmails.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No comm transmissions matching filter.",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 14.sp,
                            color = JarvisTextSecondary
                        )
                    }
                }
            } else {
                items(filteredEmails) { email ->
                    EmailMessageCard(
                        email = email,
                        onOpen = {
                            viewModel.markEmailAsRead(email.id, true)
                            activeEmailView = email
                        },
                        onToggleStar = { viewModel.toggleEmailStar(email.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // View Email Detail Dialog
    activeEmailView?.let { email ->
        AlertDialog(
            onDismissRequest = { activeEmailView = null },
            title = {
                Column {
                    Text(
                        text = email.subject,
                        fontFamily = OrbitronFontFamily,
                        fontSize = 15.sp,
                        color = JarvisCyan
                    )
                    Text(
                        text = "From: ${email.senderName} <${email.senderEmail}>",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 12.sp,
                        color = JarvisAmber
                    )
                }
            },
            text = {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = email.body,
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 14.sp,
                        color = JarvisTextPrimary,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Received: ${email.dateFormatted} • Priority: ${email.priority}",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 11.sp,
                        color = JarvisTextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.voiceSpeechManager.speak("Drafting response to ${email.senderName}, Sir.")
                        activeEmailView = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                ) {
                    Text("REPLY VIA JARVIS", color = Color.Black, fontFamily = OrbitronFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { activeEmailView = null }) {
                    Text("CLOSE", color = JarvisTextSecondary, fontFamily = OrbitronFontFamily)
                }
            },
            containerColor = JarvisSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Compose Email Dialog
    if (showComposeDialog) {
        var recipient by remember { mutableStateOf("pepper@stark.industries") }
        var subject by remember { mutableStateOf("") }
        var body by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showComposeDialog = false },
            title = {
                Text(
                    text = "DISPATCH TRANSMISSION",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 16.sp,
                    color = JarvisNeonTeal
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = recipient,
                        onValueChange = { recipient = it },
                        label = { Text("Recipient Email") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisNeonTeal,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisNeonTeal,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text("Message Body") },
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisNeonTeal,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (recipient.isNotBlank() && subject.isNotBlank()) {
                            viewModel.sendEmail(recipient, subject, body)
                            showComposeDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisNeonTeal)
                ) {
                    Text("SEND", color = Color.Black, fontFamily = OrbitronFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showComposeDialog = false }) {
                    Text("CANCEL", color = JarvisTextSecondary, fontFamily = OrbitronFontFamily)
                }
            },
            containerColor = JarvisSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun EmailMessageCard(
    email: EmailMessage,
    onOpen: () -> Unit,
    onToggleStar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .testTag("email_card_${email.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = JarvisSurface),
        border = androidx.compose.foundation.BorderStroke(
            width = if (!email.isRead) 1.5.dp else 1.dp,
            color = if (!email.isRead) JarvisNeonTeal.copy(alpha = 0.6f) else JarvisCardBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!email.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(JarvisNeonTeal)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = email.senderName,
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisTextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = email.dateFormatted,
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 11.sp,
                        color = JarvisTextSecondary
                    )
                    IconButton(
                        onClick = onToggleStar,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (email.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Star",
                            tint = if (email.isStarred) JarvisAmber else JarvisTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = email.subject,
                fontFamily = OrbitronFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (!email.isRead) JarvisNeonTeal else JarvisTextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = email.body,
                fontFamily = RajdhaniFontFamily,
                fontSize = 13.sp,
                color = JarvisTextSecondary,
                maxLines = 2
            )
        }
    }
}
