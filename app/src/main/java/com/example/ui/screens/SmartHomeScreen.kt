package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SmartDevice
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
fun SmartHomeScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val devices by viewModel.devices.collectAsState()
    val routines by viewModel.routines.collectAsState()
    val hubType by viewModel.smartHomeManager.hubType.collectAsState()
    val hubBaseUrl by viewModel.smartHomeManager.hubBaseUrl.collectAsState()
    val apiToken by viewModel.smartHomeManager.apiToken.collectAsState()
    val isConnected by viewModel.smartHomeManager.isConnected.collectAsState()
    val lastSyncStatus by viewModel.smartHomeManager.lastSyncStatus.collectAsState()

    var showHubConfig by remember { mutableStateOf(false) }
    var inputHubType by remember(hubType) { mutableStateOf(hubType) }
    var inputUrl by remember(hubBaseUrl) { mutableStateOf(hubBaseUrl) }
    var inputToken by remember(apiToken) { mutableStateOf(apiToken) }

    var selectedRoom by remember { mutableStateOf("All") }

    val rooms = listOf("All", "Living Room", "Workshop Lab", "Penthouse", "Arc Reactor Core", "Perimeter")
    val filteredDevices = if (selectedRoom == "All") devices else devices.filter { it.room == selectedRoom }

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
                    text = "HOME AUTOMATION",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = JarvisCyan
                )
                Text(
                    text = "Smart Home IoT Relays (Home Assistant / Google Home)",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 13.sp,
                    color = JarvisTextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(JarvisSurfaceVariant)
                    .border(1.dp, JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .clickable { showHubConfig = !showHubConfig }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configure Hub",
                        tint = JarvisCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showHubConfig) "CLOSE" else "HUB CONFIG",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 10.sp,
                        color = JarvisCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Hub Integration Status Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .testTag("hub_status_card"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isConnected) JarvisCyan.copy(alpha = 0.6f) else JarvisCardBorder
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) JarvisOnline else JarvisAmber)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (hubType == "GOOGLE_HOME") "GOOGLE HOME RELAY" else "HOME ASSISTANT CORE",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisTextPrimary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isConnected) "ONLINE" else "LOCAL READY",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 11.sp,
                            color = if (isConnected) JarvisOnline else JarvisAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lastSyncStatus,
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 12.sp,
                    color = JarvisTextSecondary
                )
            }
        }

        // Expandable Hub Configuration Panel
        AnimatedVisibility(visible = showHubConfig) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("hub_config_panel"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = JarvisSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "SMART HOME SERVICE CONFIGURATION",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisCyan
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Hub Type Selector Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("HOME_ASSISTANT" to "Home Assistant", "GOOGLE_HOME" to "Google Home").forEach { (typeKey, label) ->
                            val isSel = inputHubType == typeKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) JarvisCyan else JarvisSurface)
                                    .border(1.dp, if (isSel) JarvisCyan else JarvisCardBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        inputHubType = typeKey
                                        if (typeKey == "GOOGLE_HOME" && inputUrl.contains("8123")) {
                                            inputUrl = "https://smartdevicemanagement.googleapis.com/"
                                        } else if (typeKey == "HOME_ASSISTANT" && inputUrl.contains("googleapis")) {
                                            inputUrl = "http://homeassistant.local:8123"
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.Black else JarvisTextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        label = { Text("Hub Endpoint URL") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hub_url_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisCardBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inputToken,
                        onValueChange = { inputToken = it },
                        label = { Text(if (inputHubType == "GOOGLE_HOME") "OAuth / Webhook Key" else "Long-Lived Access Token") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hub_token_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisCardBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.updateHubCredentials(inputHubType, inputUrl, inputToken)
                                viewModel.testHubConnection()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_hub_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                        ) {
                            Icon(imageVector = Icons.Default.Wifi, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("TEST LINK", fontFamily = OrbitronFontFamily, fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.updateHubCredentials(inputHubType, inputUrl, inputToken)
                                viewModel.syncHubDevices()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sync_hub_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SYNC ENTITIES", fontFamily = OrbitronFontFamily, fontSize = 11.sp, color = JarvisCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Voice Command Suggestions Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "Turn on living room lights" to "Lights On",
                "Set workshop lights to 80%" to "Workshop 80%",
                "Set thermostat to 72 degrees" to "Temp 72°F",
                "Lockdown the vault" to "Lock Vault",
                "Activate Protocol Night Watch" to "Night Watch",
                "Engage Protocol House Party" to "Party Mode"
            ).forEach { (command, label) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(JarvisSurfaceVariant)
                        .border(0.5.dp, JarvisAmber.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable { viewModel.executeCommand(command) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🎤 $label",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = JarvisAmber
                    )
                }
            }
        }

        // Room filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rooms.forEach { room ->
                val isSelected = selectedRoom == room
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) JarvisCyan else JarvisSurface)
                        .border(1.dp, if (isSelected) JarvisCyan else JarvisCardBorder, RoundedCornerShape(10.dp))
                        .clickable { selectedRoom = room }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = room,
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.Black else JarvisTextSecondary
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Protocols & Routines section
            item {
                Text(
                    text = "AUTOMATED PROTOCOLS",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 12.sp,
                    color = JarvisTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            items(routines) { routine ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("routine_${routine.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (routine.isActive) JarvisCyan else JarvisCardBorder
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = routine.name,
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (routine.isActive) JarvisCyan else JarvisTextPrimary
                                )
                                if (routine.isActive) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(JarvisOnline)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = routine.description,
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 12.sp,
                                color = JarvisTextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Trigger: \"${routine.triggerPhrase}\"",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 11.sp,
                                color = JarvisAmber
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (routine.isActive) JarvisCyan else JarvisSurfaceVariant)
                                .border(1.dp, JarvisCyan, RoundedCornerShape(14.dp))
                                .clickable { viewModel.toggleRoutine(routine.id, routine.isActive) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
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

            // Devices Section
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "DEVICES & SENSORS (${filteredDevices.size})",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 12.sp,
                    color = JarvisTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            items(filteredDevices) { device ->
                SmartDeviceCard(
                    device = device,
                    onPowerToggle = { viewModel.toggleDevicePower(device.id, device.isOn) },
                    onValueChange = { viewModel.setDeviceValue(device.id, it) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun SmartDeviceCard(
    device: SmartDevice,
    onPowerToggle: () -> Unit,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("device_card_${device.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = JarvisSurface),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (device.isOn) JarvisCyan.copy(alpha = 0.6f) else JarvisCardBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (device.isOn) JarvisCyan.copy(alpha = 0.2f) else JarvisSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (device.type) {
                                "LIGHT" -> Icons.Default.FlashOn
                                "THERMOSTAT" -> Icons.Default.Thermostat
                                "LOCK" -> if (device.isOn) Icons.Default.Lock else Icons.Default.LockOpen
                                "SHIELD" -> Icons.Default.Security
                                else -> Icons.Default.AcUnit
                            },
                            contentDescription = device.name,
                            tint = if (device.isOn) JarvisCyan else JarvisTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = device.name,
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisTextPrimary
                        )
                        Text(
                            text = "${device.room} • Mode: ${device.mode}",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 12.sp,
                            color = JarvisTextSecondary
                        )
                    }
                }

                Switch(
                    checked = device.isOn,
                    onCheckedChange = { onPowerToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = JarvisCyan,
                        uncheckedThumbColor = JarvisTextSecondary,
                        uncheckedTrackColor = JarvisSurfaceVariant
                    )
                )
            }

            // Slider control for numeric value (brightness, temperature, shield)
            if (device.type == "LIGHT" || device.type == "THERMOSTAT" || device.type == "SHIELD" || device.type == "AC") {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (device.type) {
                            "LIGHT" -> "Brightness Level"
                            "THERMOSTAT" -> "Target Temperature"
                            "SHIELD" -> "Shield Harmonic Yield"
                            else -> "Aperture Open"
                        },
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 12.sp,
                        color = JarvisTextSecondary
                    )
                    Text(
                        text = when (device.type) {
                            "THERMOSTAT" -> "${device.numericValue.toInt()}°F"
                            else -> "${device.numericValue.toInt()}%"
                        },
                        fontFamily = OrbitronFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (device.isOn) JarvisCyan else JarvisTextSecondary
                    )
                }

                val valueRange = if (device.type == "THERMOSTAT") 60f..85f else 0f..100f
                Slider(
                    value = device.numericValue.coerceIn(valueRange.start, valueRange.endInclusive),
                    onValueChange = onValueChange,
                    valueRange = valueRange,
                    enabled = device.isOn,
                    colors = SliderDefaults.colors(
                        thumbColor = JarvisCyan,
                        activeTrackColor = JarvisCyan,
                        inactiveTrackColor = JarvisSurfaceVariant
                    )
                )
            }
        }
    }
}
