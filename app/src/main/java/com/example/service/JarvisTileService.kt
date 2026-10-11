package com.example.service

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.example.MainActivity
import com.example.util.DirectInstallHelper

/**
 * Quick Settings Tile enabling 1-tap J.A.R.V.I.S. activation directly from Android's
 * notification shade while inside ANY other application.
 */
class JarvisTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val hasOverlay = DirectInstallHelper.hasOverlayPermission(this)

        if (!hasOverlay) {
            // Bring user to main app with instruction
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("open_overlay_permission", true)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Android 14+ startActivityAndCollapse
                startActivityAndCollapse(
                    android.app.PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
            return
        }

        if (FloatingJarvisService.isServiceRunning) {
            // Trigger voice listening over current app
            DirectInstallHelper.triggerVoiceFromOverlay(this)
        } else {
            // Start the floating overlay
            DirectInstallHelper.startFloatingAssistant(this)
        }
        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val isRunning = FloatingJarvisService.isServiceRunning

        tile.state = if (isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = "J.A.R.V.I.S."
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = if (isRunning) "Active Over Apps" else "Tap to Activate"
        }
        tile.updateTile()
    }
}
