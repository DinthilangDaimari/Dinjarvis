package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.service.FloatingJarvisService
import java.io.File

object DirectInstallHelper {

    fun hasOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun requestOverlayPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Toast.makeText(
                context,
                "Enable 'Allow display over other apps' to activate J.A.R.V.I.S. anywhere",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun startFloatingAssistant(context: Context): Boolean {
        if (!hasOverlayPermission(context)) {
            requestOverlayPermission(context)
            return false
        }
        val intent = Intent(context, FloatingJarvisService::class.java).apply {
            action = FloatingJarvisService.ACTION_START_OVERLAY
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        Toast.makeText(context, "J.A.R.V.I.S. Arc Reactor active over other apps!", Toast.LENGTH_SHORT).show()
        return true
    }

    fun stopFloatingAssistant(context: Context) {
        val intent = Intent(context, FloatingJarvisService::class.java).apply {
            action = FloatingJarvisService.ACTION_STOP_OVERLAY
        }
        context.startService(intent)
        Toast.makeText(context, "Floating Arc Reactor dismissed", Toast.LENGTH_SHORT).show()
    }

    fun triggerVoiceFromOverlay(context: Context) {
        if (!FloatingJarvisService.isServiceRunning) {
            startFloatingAssistant(context)
        } else {
            val intent = Intent(context, FloatingJarvisService::class.java).apply {
                action = FloatingJarvisService.ACTION_START_LISTENING
            }
            context.startService(intent)
        }
    }

    fun installApkDirect(context: Context, apkFile: File) {
        try {
            if (!apkFile.exists()) {
                Toast.makeText(context, "APK file not found for direct install", Toast.LENGTH_SHORT).show()
                return
            }
            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Direct installation error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Extracts the base application APK from the device and launches the native Package Installer.
     */
    fun installCurrentAppApk(context: Context) {
        try {
            val sourceDir = context.applicationInfo.sourceDir
            val srcFile = File(sourceDir)
            if (!srcFile.exists()) {
                // If base APK not found on path, trigger browser download from local web server
                downloadApkViaWebServer(context)
                return
            }
            val targetDir = File(context.cacheDir, "shared_apk")
            if (!targetDir.exists()) targetDir.mkdirs()
            val targetFile = File(targetDir, "JARVIS-StarkOS.apk")
            srcFile.copyTo(targetFile, overwrite = true)

            installApkDirect(context, targetFile)
            Toast.makeText(context, "Starting APK installation...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Package installer launch failed: ${e.message}. Downloading via web companion...", Toast.LENGTH_LONG).show()
            downloadApkViaWebServer(context)
        }
    }

    /**
     * Launches the companion web server and opens the PWA directly in Chrome/browser
     * allowing 1-tap installation directly to the home screen.
     */
    fun installWebAppPwa(context: Context) {
        try {
            com.example.service.JarvisWebCompanionServer.openInBrowser(context)
            Toast.makeText(context, "Opening J.A.R.V.I.S. Web App for direct installation...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to open web app: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Triggers the direct APK download via the built-in HTTP server.
     */
    fun downloadApkViaWebServer(context: Context) {
        try {
            if (!com.example.service.JarvisWebCompanionServer.isRunning) {
                com.example.service.JarvisWebCompanionServer.start(context)
            }
            val downloadUrl = "http://127.0.0.1:${com.example.service.JarvisWebCompanionServer.PORT}/download-apk"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Toast.makeText(context, "Downloading J.A.R.V.I.S. APK directly...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Download error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Exports or shares the APK file via system share sheet.
     */
    fun shareApk(context: Context) {
        try {
            val sourceDir = context.applicationInfo.sourceDir
            val srcFile = File(sourceDir)
            val targetDir = File(context.cacheDir, "shared_apk")
            if (!targetDir.exists()) targetDir.mkdirs()
            val targetFile = File(targetDir, "JARVIS-StarkOS.apk")
            srcFile.copyTo(targetFile, overwrite = true)

            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                targetFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, apkUri)
                putExtra(Intent.EXTRA_SUBJECT, "J.A.R.V.I.S. AI Stark OS APK")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(Intent.createChooser(shareIntent, "Save or Export J.A.R.V.I.S. APK").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing APK: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
