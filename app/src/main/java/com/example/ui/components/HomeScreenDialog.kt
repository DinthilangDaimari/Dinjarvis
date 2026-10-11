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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.RajdhaniFontFamily
import com.example.util.HomeScreenHelper

@Composable
fun HomeScreenDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isPinSupported = HomeScreenHelper.isPinShortcutSupported(context)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("home_screen_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, JarvisCyan)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(JarvisCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = JarvisCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "HOME SCREEN SETUP",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisCyan
                            )
                            Text(
                                text = "Add J.A.R.V.I.S. to phone launcher",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 12.sp,
                                color = JarvisTextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = JarvisTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // One-Tap Pin Button (if supported on Android 8+)
                if (isPinSupported) {
                    Button(
                        onClick = {
                            HomeScreenHelper.pinJarvisToHomeScreen(context)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pin_to_home_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PIN MAIN ICON TO HOME SCREEN",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                HomeScreenHelper.pinScheduleMeetingShortcut(context)
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pin_schedule_meeting_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan)
                        ) {
                            Text(
                                text = "PIN MEETING",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisCyan
                            )
                        }

                        Button(
                            onClick = {
                                HomeScreenHelper.pinToggleLightsShortcut(context)
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pin_toggle_lights_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisAmber)
                        ) {
                            Text(
                                text = "PIN LIGHTS",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisAmber
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Step-by-Step Instructions
                Text(
                    text = "4 WAYS TO ACCESS J.A.R.V.I.S. ON HOME SCREEN:",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 11.sp,
                    color = JarvisAmber,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Step 1: App Shortcuts (Long-press)
                HomeScreenGuideStep(
                    number = "1",
                    title = "App Shortcuts (Long-Press Icon)",
                    description = "Long-press the J.A.R.V.I.S. icon on your home screen or app drawer to instantly trigger 'Schedule Meeting' or 'Toggle Smart Lights' without opening the main menu!"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Step 2: App Drawer
                HomeScreenGuideStep(
                    number = "2",
                    title = "App Drawer Drag & Drop",
                    description = "Swipe up on your phone to open the App Drawer. Find 'J.A.R.V.I.S.', long-press the icon, and drag it anywhere on your home screen."
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Step 3: Android Home Screen Widget
                HomeScreenGuideStep(
                    number = "3",
                    title = "Arc Reactor Home Widget",
                    description = "Long-press an empty area of your Android home screen -> Tap 'Widgets' -> Search for 'J.A.R.V.I.S.' -> Drag the futuristic Arc Reactor widget to your screen for instant 1-tap voice directives!"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Step 4: Chrome / Browser Shortcut
                HomeScreenGuideStep(
                    number = "4",
                    title = "Mobile Web / PWA Shortcut",
                    description = "When opening the app link in Chrome or your phone browser: Tap the 3 dots menu (⋮) in the top-right corner -> Select 'Add to Home screen' or 'Install app'."
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder)
                ) {
                    Text(
                        text = "UNDERSTOOD, SIR",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 12.sp,
                        color = JarvisCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeScreenGuideStep(
    number: String,
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(JarvisSurfaceVariant)
            .border(0.5.dp, JarvisCardBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(JarvisCyan.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number,
                    fontFamily = OrbitronFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = JarvisCyan
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = title,
                    fontFamily = OrbitronFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = JarvisTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 12.sp,
                    color = JarvisTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
