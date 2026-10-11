package com.example

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.example.service.FloatingJarvisService
import com.example.util.DirectInstallHelper

/**
 * Android Assistant entry point. Allows J.A.R.V.I.S. to be invoked as the device digital assistant
 * from any other app or by long-pressing the home/power button.
 */
class AssistActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (DirectInstallHelper.hasOverlayPermission(this)) {
            DirectInstallHelper.triggerVoiceFromOverlay(this)
        } else {
            val mainIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("auto_voice_listen", true)
            }
            startActivity(mainIntent)
        }
        finish()
    }
}
