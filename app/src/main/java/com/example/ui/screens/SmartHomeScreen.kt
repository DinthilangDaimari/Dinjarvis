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
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Text(
                text = "HOME AUTOMATION",
                fontFamily = OrbitronFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = JarvisCyan
            )
            Text(
                text = "Environmental and security relays active",
                fontFamily = RajdhaniFontFamily,
                fontSize = 13.sp,
                color = JarvisTextSecondary
            )
        }

        // Voice directive tip
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisSurfaceVariant),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, JarvisCyan.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = JarvisAmber,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Voice command: \"Jarvis, set workshop lights to 100%\" or \"Lockdown the tower\"",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 12.sp,
                    color = JarvisTextPrimary
                )
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
