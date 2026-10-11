package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun SmartHomeHubDialog(
    viewModel: JarvisViewModel,
    onDismiss: () -> Unit
) {
    val isConnected by viewModel.smartHomeManager.isConnected.collectAsState()
    val syncStatus by viewModel.smartHomeManager.lastSyncStatus.collectAsState()
    val hubType by viewModel.smartHomeManager.hubType.collectAsState()
    val savedUrl by viewModel.smartHomeManager.hubBaseUrl.collectAsState()
    val savedToken by viewModel.smartHomeManager.apiToken.collectAsState()

    var urlInput by remember { mutableStateOf(savedUrl) }
    var tokenInput by remember { mutableStateOf(savedToken) }

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
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = JarvisCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SMART HOME API HUB",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisCyan
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isConnected) JarvisOnline.copy(alpha = 0.2f) else JarvisAmber.copy(alpha = 0.2f))
                        .border(0.5.dp, if (isConnected) JarvisOnline else JarvisAmber, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isConnected) "CONNECTED" else "LOCAL SIM",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isConnected) JarvisOnline else JarvisAmber
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Status banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = JarvisSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, JarvisCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Cable,
                            contentDescription = null,
                            tint = if (isConnected) JarvisOnline else JarvisAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = syncStatus,
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 12.sp,
                            color = JarvisTextPrimary
                        )
                    }
                }

                Text(
                    text = "Supports Home Assistant REST API or Google Home / Cloud smart home relays. Enter your server host and long-lived access token:",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 12.sp,
                    color = JarvisTextSecondary
                )

                // Server URL input
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("Server URL / Host") },
                    placeholder = { Text("http://homeassistant.local:8123") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Long-Lived Access Token input
                OutlinedTextField(
                    value = tokenInput,
                    onValueChange = { tokenInput = it },
                    label = { Text("Long-Lived Access Token") },
                    placeholder = { Text("Bearer eyJhbGciOi...") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Actions: Save & Test Connection, Sync Entities
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.updateHubCredentials(hubType, urlInput, tokenInput)
                            viewModel.testHubConnection()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                    ) {
                        Text(
                            text = "TEST LINK",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.updateHubCredentials(hubType, urlInput, tokenInput)
                            viewModel.syncHubDevices()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisNeonTeal)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SYNC ENTITIES",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.updateHubCredentials(hubType, urlInput, tokenInput)
                    onDismiss()
                }
            ) {
                Text(
                    text = "SAVE & CLOSE",
                    fontFamily = OrbitronFontFamily,
                    color = JarvisCyan
                )
            }
        },
        containerColor = JarvisSurface,
        shape = RoundedCornerShape(18.dp)
    )
}
