package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.service.FloatingJarvisService
import com.example.service.JarvisWebCompanionServer
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
import com.example.util.DirectInstallHelper
import java.io.File

@Composable
fun WebAppDirectInstallDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isOverlayRunning by remember { mutableStateOf(FloatingJarvisService.isServiceRunning) }
    var hasOverlayPermission by remember { mutableStateOf(DirectInstallHelper.hasOverlayPermission(context)) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("webapp_direct_install_dialog"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, JarvisCyan)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = JarvisCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "WEB APP & DIRECT INSTALL",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisCyan
                            )
                            Text(
                                text = "Install Direct from Web App & Run Over Other Apps",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 11.sp,
                                color = JarvisTextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = JarvisTextSecondary)
                    }
                }

                // 1. Direct Web App (PWA) Launch & 1-Tap Install
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = JarvisSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisNeonTeal)
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
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(JarvisOnline)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "WEB APP COMPANION (PWA)",
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 11.sp,
                                    color = JarvisNeonTeal,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "PORT 8888",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 11.sp,
                                color = JarvisTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Opens J.A.R.V.I.S. in your phone browser with Web Speech API and the 'Install J.A.R.V.I.S. Direct to Home Screen' prompt.",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 12.sp,
                            color = JarvisTextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Launch Web App Button
                        Button(
                            onClick = {
                                JarvisWebCompanionServer.openInBrowser(context)
                                Toast.makeText(
                                    context,
                                    "Launching Web App! Tap 'Install J.A.R.V.I.S.' in browser.",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("launch_webapp_in_browser_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LAUNCH WEB APP TO INSTALL DIRECT",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Copy Link Button
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    if (!JarvisWebCompanionServer.isRunning) {
                                        JarvisWebCompanionServer.start(context)
                                    }
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("JARVIS Web App", JarvisWebCompanionServer.getLocalUrl()))
                                    Toast.makeText(context, "Copied ${JarvisWebCompanionServer.getLocalUrl()} to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("copy_webapp_link_btn"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = JarvisBg),
                                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "COPY URL",
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 10.sp,
                                    color = JarvisCyan
                                )
                            }

                            // Direct APK install
                            Button(
                                onClick = {
                                    val sourceApk = File(context.applicationInfo.sourceDir)
                                    DirectInstallHelper.installApkDirect(context, sourceApk)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("install_direct_apk_btn"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = JarvisBg),
                                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisAmber)
                            ) {
                                Icon(imageVector = Icons.Default.GetApp, contentDescription = null, tint = JarvisAmber, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "DIRECT APK",
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 10.sp,
                                    color = JarvisAmber
                                )
                            }
                        }
                    }
                }

                // 2. Active Over Other Apps (Floating Arc Reactor)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = JarvisSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isOverlayRunning) JarvisAmber else JarvisCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ACTIVE OVER OTHER APPS",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 11.sp,
                                color = JarvisAmber,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isOverlayRunning) JarvisOnline.copy(alpha = 0.2f) else JarvisSurface)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isOverlayRunning) "FLOATING ACTIVE" else "INACTIVE",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 10.sp,
                                    color = if (isOverlayRunning) JarvisOnline else JarvisTextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Summon J.A.R.V.I.S. via a floating Arc Reactor or Quick Settings Tile while using ANY other app (YouTube, Chrome, Games, etc.).",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 12.sp,
                            color = JarvisTextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (!hasOverlayPermission) {
                            Button(
                                onClick = {
                                    DirectInstallHelper.requestOverlayPermission(context)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("grant_overlay_perm_btn"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = JarvisAmber)
                            ) {
                                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "GRANT 'DISPLAY OVER APPS' PERMISSION",
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 10.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (isOverlayRunning) {
                                        DirectInstallHelper.stopFloatingAssistant(context)
                                        isOverlayRunning = false
                                    } else {
                                        val success = DirectInstallHelper.startFloatingAssistant(context)
                                        if (success) isOverlayRunning = true
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("toggle_floating_overlay_btn"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isOverlayRunning) JarvisSurface else JarvisAmber
                                ),
                                border = if (isOverlayRunning) androidx.compose.foundation.BorderStroke(1.dp, JarvisAmber) else null
                            ) {
                                Icon(
                                    imageVector = if (isOverlayRunning) Icons.Default.Close else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = if (isOverlayRunning) JarvisAmber else Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isOverlayRunning) "DISMISS FLOATING ARC REACTOR" else "START FLOATING ARC REACTOR OVER APPS",
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOverlayRunning) JarvisAmber else Color.Black
                                )
                            }
                        }
                    }
                }

                // 3. Quick 3-step Instructions
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "INSTALLATION & USAGE DIRECTIVES:",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 10.sp,
                        color = JarvisCyan,
                        fontWeight = FontWeight.Bold
                    )

                    WebGuideStep(
                        step = "1",
                        title = "Install Direct from Web Browser",
                        desc = "Tap 'Launch Web App' &rarr; Tap 'INSTALL J.A.R.V.I.S. DIRECT TO HOME SCREEN' or browser menu (⋮) -> 'Install app'."
                    )

                    WebGuideStep(
                        step = "2",
                        title = "Use in Other Apps (Overlay)",
                        desc = "Enable 'Start Floating Arc Reactor' above. The glowing orb floats over all apps. Tap it anytime to dictate directives."
                    )

                    WebGuideStep(
                        step = "3",
                        title = "Quick Settings Notification Tile",
                        desc = "Swipe down your Android notification panel in any app &rarr; Tap the 'J.A.R.V.I.S.' tile to speak without leaving your app!"
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder)
                ) {
                    Text(
                        text = "CLOSE PANEL",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 11.sp,
                        color = JarvisCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun WebGuideStep(
    step: String,
    title: String,
    desc: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(JarvisSurfaceVariant)
            .border(0.5.dp, JarvisCardBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(JarvisCyan.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = step,
                    fontFamily = OrbitronFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = JarvisCyan
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    fontFamily = OrbitronFontFamily,
                    fontSize = 11.sp,
                    color = JarvisTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = desc,
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 11.sp,
                    color = JarvisTextSecondary
                )
            }
        }
    }
}
