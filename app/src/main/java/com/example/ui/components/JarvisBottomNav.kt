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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBg
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.viewmodel.JarvisNavigationScreen

data class NavItem(
    val title: String,
    val icon: ImageVector,
    val screen: JarvisNavigationScreen,
    val tag: String
)

@Composable
fun JarvisBottomNav(
    currentScreen: JarvisNavigationScreen,
    onSelectScreen: (JarvisNavigationScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem("Console", Icons.Default.Radio, JarvisNavigationScreen.Dashboard, "nav_console"),
        NavItem("Schedule", Icons.Default.DateRange, JarvisNavigationScreen.Calendar, "nav_calendar"),
        NavItem("Comms", Icons.Default.Email, JarvisNavigationScreen.Emails, "nav_email"),
        NavItem("Phone", Icons.Default.Phone, JarvisNavigationScreen.Phone, "nav_phone"),
        NavItem("Apps", Icons.Default.Apps, JarvisNavigationScreen.Apps, "nav_apps"),
        NavItem("Home", Icons.Default.Home, JarvisNavigationScreen.SmartHome, "nav_home")
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(JarvisSurface)
            .border(0.5.dp, JarvisCyan.copy(alpha = 0.25f))
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val isSelected = currentScreen::class == item.screen::class

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) JarvisCyan.copy(alpha = 0.15f)
                        else androidx.compose.ui.graphics.Color.Transparent
                    )
                    .clickable { onSelectScreen(item.screen) }
                    .testTag(item.tag)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (isSelected) JarvisCyan else JarvisTextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.title,
                        color = if (isSelected) JarvisCyan else JarvisTextSecondary,
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
