package com.example.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.widget.Toast
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.MainActivity
import com.example.R

object HomeScreenHelper {

    const val SHORTCUT_SCHEDULE_MEETING = "schedule_meeting"
    const val SHORTCUT_TOGGLE_LIGHTS = "toggle_smart_lights"

    fun isPinShortcutSupported(context: Context): Boolean {
        return ShortcutManagerCompat.isRequestPinShortcutSupported(context)
    }

    /**
     * Initializes dynamic App Shortcuts so that long-pressing the app launcher icon
     * displays 'Schedule Meeting' and 'Toggle Smart Lights' options.
     */
    fun initializeDynamicShortcuts(context: Context) {
        try {
            val meetingIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtra("shortcut_action", SHORTCUT_SCHEDULE_MEETING)
                putExtra("quick_command", SHORTCUT_SCHEDULE_MEETING)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val lightsIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtra("shortcut_action", SHORTCUT_TOGGLE_LIGHTS)
                putExtra("quick_command", SHORTCUT_TOGGLE_LIGHTS)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val meetingShortcut = ShortcutInfoCompat.Builder(context, SHORTCUT_SCHEDULE_MEETING)
                .setShortLabel(context.getString(R.string.shortcut_schedule_meeting_short))
                .setLongLabel(context.getString(R.string.shortcut_schedule_meeting_long))
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_calendar))
                .setIntent(meetingIntent)
                .setRank(1)
                .build()

            val lightsShortcut = ShortcutInfoCompat.Builder(context, SHORTCUT_TOGGLE_LIGHTS)
                .setShortLabel(context.getString(R.string.shortcut_toggle_lights_short))
                .setLongLabel(context.getString(R.string.shortcut_toggle_lights_long))
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_lights))
                .setIntent(lightsIntent)
                .setRank(2)
                .build()

            ShortcutManagerCompat.setDynamicShortcuts(context, listOf(meetingShortcut, lightsShortcut))
        } catch (e: Exception) {
            // Ignore any launcher security restrictions
        }
    }

    fun pinScheduleMeetingShortcut(context: Context): Boolean {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) return false

        val meetingIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("shortcut_action", SHORTCUT_SCHEDULE_MEETING)
            putExtra("quick_command", SHORTCUT_SCHEDULE_MEETING)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val shortcutInfo = ShortcutInfoCompat.Builder(context, "pin_${SHORTCUT_SCHEDULE_MEETING}")
            .setShortLabel(context.getString(R.string.shortcut_schedule_meeting_short))
            .setLongLabel(context.getString(R.string.shortcut_schedule_meeting_long))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_calendar))
            .setIntent(meetingIntent)
            .build()

        val callback = PendingIntent.getActivity(
            context,
            101,
            meetingIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val requested = ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, callback.intentSender)
        if (requested) {
            Toast.makeText(context, "Pinning 'Schedule Meeting' shortcut to Home Screen...", Toast.LENGTH_SHORT).show()
        }
        return requested
    }

    fun pinToggleLightsShortcut(context: Context): Boolean {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) return false

        val lightsIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("shortcut_action", SHORTCUT_TOGGLE_LIGHTS)
            putExtra("quick_command", SHORTCUT_TOGGLE_LIGHTS)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val shortcutInfo = ShortcutInfoCompat.Builder(context, "pin_${SHORTCUT_TOGGLE_LIGHTS}")
            .setShortLabel(context.getString(R.string.shortcut_toggle_lights_short))
            .setLongLabel(context.getString(R.string.shortcut_toggle_lights_long))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_lights))
            .setIntent(lightsIntent)
            .build()

        val callback = PendingIntent.getActivity(
            context,
            102,
            lightsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val requested = ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, callback.intentSender)
        if (requested) {
            Toast.makeText(context, "Pinning 'Toggle Smart Lights' shortcut to Home Screen...", Toast.LENGTH_SHORT).show()
        }
        return requested
    }

    fun pinJarvisToHomeScreen(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
            if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported) {
                val pinIntent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_MAIN
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("auto_listen", true)
                }

                val pinShortcutInfo = ShortcutInfo.Builder(context, "jarvis_voice_home_shortcut")
                    .setShortLabel("J.A.R.V.I.S.")
                    .setLongLabel("J.A.R.V.I.S. Voice Assistant")
                    .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher))
                    .setIntent(pinIntent)
                    .build()

                val successCallback = PendingIntent.getActivity(
                    context,
                    0,
                    pinIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                shortcutManager.requestPinShortcut(pinShortcutInfo, successCallback.intentSender)
                Toast.makeText(context, "Requesting Android to add J.A.R.V.I.S. to Home Screen...", Toast.LENGTH_LONG).show()
                return true
            }
        }
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            val pinIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_MAIN
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val shortcutInfo = ShortcutInfoCompat.Builder(context, "jarvis_main_pin")
                .setShortLabel("J.A.R.V.I.S.")
                .setLongLabel("J.A.R.V.I.S. Stark OS")
                .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
                .setIntent(pinIntent)
                .build()
            val callback = PendingIntent.getActivity(
                context,
                0,
                pinIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val res = ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, callback.intentSender)
            if (res) {
                Toast.makeText(context, "Added J.A.R.V.I.S. shortcut to Home Screen", Toast.LENGTH_SHORT).show()
            }
            return res
        }
        return false
    }
}
