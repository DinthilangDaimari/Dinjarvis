package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.ai.JarvisAiEngine
import com.example.ai.ToolAction
import com.example.ai.VoiceSpeechManager
import com.example.data.database.JarvisDatabase
import com.example.data.repository.JarvisRepository
import com.example.smarthome.service.SmartHomeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Floating Overlay Service that allows J.A.R.V.I.S. to stay active over ANY other application.
 * Features:
 * - Draggable floating Arc Reactor orb on top of all apps
 * - 1-tap voice interaction HUD overlay over other apps
 * - SpeechRecognizer & Gemini API directive processing
 * - Text-To-Speech responses
 * - Device actions (calls, smart home toggles, navigation, timer, alarms)
 */
class FloatingJarvisService : Service() {

    companion object {
        const val ACTION_START_OVERLAY = "com.example.action.START_FLOATING_OVERLAY"
        const val ACTION_STOP_OVERLAY = "com.example.action.STOP_FLOATING_OVERLAY"
        const val ACTION_START_LISTENING = "com.example.action.START_FLOATING_LISTENING"
        const val CHANNEL_ID = "jarvis_floating_overlay_channel"
        const val NOTIFICATION_ID = 2024

        var isServiceRunning: Boolean = false
            private set
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var windowManager: WindowManager? = null

    // Overlay root and window layout parameters
    private var overlayContainer: FrameLayout? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    // Sub-views
    private var miniOrbView: FrameLayout? = null
    private var expandedHudView: LinearLayout? = null
    private var statusTextView: TextView? = null
    private var transcriptTextView: TextView? = null
    private var responseTextView: TextView? = null
    private var micButton: FrameLayout? = null
    private var orbGlowRing: ImageView? = null

    private var isExpanded: Boolean = false

    // AI & Voice Engine
    private lateinit var voiceSpeechManager: VoiceSpeechManager
    private lateinit var aiEngine: JarvisAiEngine
    private lateinit var repository: JarvisRepository

    // Drag touch tracking
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var touchStartTime = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true

        val database = JarvisDatabase.getDatabase(applicationContext, serviceScope)
        val smartHomeManager = SmartHomeManager(applicationContext, database.homeDao())
        repository = JarvisRepository(
            calendarDao = database.calendarDao(),
            scheduleHistoryDao = database.scheduleHistoryDao(),
            emailDao = database.emailDao(),
            contactDao = database.contactDao(),
            callLogDao = database.callLogDao(),
            homeDao = database.homeDao(),
            assistantLogDao = database.assistantLogDao(),
            smartHomeManager = smartHomeManager
        )
        aiEngine = JarvisAiEngine(repository)

        voiceSpeechManager = VoiceSpeechManager(applicationContext) { spokenInput ->
            handleVoiceDirective(spokenInput)
        }

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildForegroundNotification())

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        if (checkOverlayPermission()) {
            buildOverlayView()
        }

        observeVoiceState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_OVERLAY -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START_LISTENING -> {
                expandHud()
                voiceSpeechManager.startListening()
            }
        }
        return START_STICKY
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun buildOverlayView() {
        val wm = windowManager ?: return

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 260
        }
        this.layoutParams = params

        val root = FrameLayout(this)
        this.overlayContainer = root

        // 1. Mini Draggable Orb (Arc Reactor)
        val orb = createMiniOrbView()
        this.miniOrbView = orb
        root.addView(orb)

        // 2. Expanded Floating HUD
        val hud = createExpandedHudView()
        this.expandedHudView = hud
        hud.visibility = View.GONE
        root.addView(hud)

        setupTouchEvents(orb)

        try {
            wm.addView(root, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createMiniOrbView(): FrameLayout {
        val sizePx = dpToPx(64)
        val orb = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(sizePx, sizePx)
        }

        // Glowing outer background
        val bgDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor("#060A12"))
            setStroke(dpToPx(2), Color.parseColor("#00E5FF"))
        }
        orb.background = bgDrawable

        // Core Arc Reactor icon
        val coreIcon = ImageView(this).apply {
            setImageResource(R.drawable.ic_shortcut_lights)
            setColorFilter(Color.parseColor("#00E5FF"))
            val pad = dpToPx(14)
            setPadding(pad, pad, pad, pad)
        }
        this.orbGlowRing = coreIcon

        orb.addView(coreIcon)
        return orb
    }

    private fun createExpandedHudView(): LinearLayout {
        val hud = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(dpToPx(320), FrameLayout.LayoutParams.WRAP_CONTENT)
            val pad = dpToPx(14)
            setPadding(pad, pad, pad, pad)
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(18).toFloat()
                setColor("#F00A1322".toLong(16).toInt())
                setStroke(dpToPx(1.5f), Color.parseColor("#00E5FF"))
            }
            background = bg
            elevation = dpToPx(12).toFloat()
        }

        // Header with Title & Action Buttons
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val titleCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val hudTitle = TextView(this).apply {
            text = "J.A.R.V.I.S. OVERLAY"
            textSize = 13f
            setTextColor(Color.parseColor("#00E5FF"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val hudSub = TextView(this).apply {
            text = "ACTIVE OVER ALL APPS"
            textSize = 10f
            setTextColor(Color.parseColor("#7F9EB2"))
        }
        titleCol.addView(hudTitle)
        titleCol.addView(hudSub)
        header.addView(titleCol)

        // Minimize / Collapse Button
        val minimizeBtn = TextView(this).apply {
            text = "—"
            textSize = 18f
            setTextColor(Color.parseColor("#00E5FF"))
            gravity = Gravity.CENTER
            val p = dpToPx(8)
            setPadding(p, 0, p, 0)
            setOnClickListener { collapseHud() }
        }
        header.addView(minimizeBtn)

        // Close Overlay Button
        val closeBtn = TextView(this).apply {
            text = "✕"
            textSize = 14f
            setTextColor(Color.parseColor("#FF3B30"))
            gravity = Gravity.CENTER
            val p = dpToPx(8)
            setPadding(p, 0, p, 0)
            setOnClickListener { stopSelf() }
        }
        header.addView(closeBtn)

        hud.addView(header)

        // Status text (Listening / Processing)
        val status = TextView(this).apply {
            text = "READY FOR DIRECTIVE"
            textSize = 11f
            setTextColor(Color.parseColor("#FFB300"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            val topP = dpToPx(8)
            setPadding(0, topP, 0, 0)
        }
        this.statusTextView = status
        hud.addView(status)

        // User speech transcript
        val transcript = TextView(this).apply {
            text = "Tap microphone or speak directive..."
            textSize = 13f
            setTextColor(Color.parseColor("#E6F7FF"))
            val p = dpToPx(6)
            setPadding(0, p, 0, p)
        }
        this.transcriptTextView = transcript
        hud.addView(transcript)

        // Response text
        val response = TextView(this).apply {
            text = "\"All systems nominal, Sir. Ready to assist over any application.\""
            textSize = 13f
            setTextColor(Color.parseColor("#00E5FF"))
            val p = dpToPx(6)
            setPadding(0, 0, 0, p)
        }
        this.responseTextView = response
        hud.addView(response)

        // Bottom Action Bar: Mic Button & Open App Button
        val bottomBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val topP = dpToPx(6)
            setPadding(0, topP, 0, 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Mic Button Frame
        val micFrame = FrameLayout(this).apply {
            val s = dpToPx(42)
            layoutParams = LinearLayout.LayoutParams(s, s)
            val d = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#00E5FF"))
            }
            background = d
            val ic = ImageView(context).apply {
                setImageResource(R.drawable.ic_shortcut_calendar)
                setColorFilter(Color.BLACK)
                val p = dpToPx(10)
                setPadding(p, p, p, p)
            }
            addView(ic)
            setOnClickListener {
                if (voiceSpeechManager.isListening.value) {
                    voiceSpeechManager.stopListening()
                } else {
                    voiceSpeechManager.stopSpeaking()
                    voiceSpeechManager.startListening()
                }
            }
        }
        this.micButton = micFrame
        bottomBar.addView(micFrame)

        // Launch Main App Button
        val openAppBtn = TextView(this).apply {
            text = "OPEN FULL APP ↗"
            textSize = 11f
            setTextColor(Color.parseColor("#00E5FF"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            val p = dpToPx(8)
            setPadding(p * 2, p, p * 2, p)
            val d = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(8).toFloat()
                setColor(Color.parseColor("#101E33"))
                setStroke(dpToPx(1), Color.parseColor("#00E5FF"))
            }
            background = d
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = dpToPx(12)
            }
            layoutParams = lp
            setOnClickListener {
                val appIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                context.startActivity(appIntent)
                collapseHud()
            }
        }
        bottomBar.addView(openAppBtn)

        hud.addView(bottomBar)
        return hud
    }

    private fun setupTouchEvents(view: View) {
        view.setOnTouchListener { _, event ->
            val lp = layoutParams ?: return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = lp.x
                    initialY = lp.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    touchStartTime = System.currentTimeMillis()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    lp.x = initialX + (event.rawX - initialTouchX).toInt()
                    lp.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(overlayContainer, lp)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val diffX = Math.abs(event.rawX - initialTouchX)
                    val diffY = Math.abs(event.rawY - initialTouchY)
                    val timeDiff = System.currentTimeMillis() - touchStartTime
                    if (diffX < 15 && diffY < 15 && timeDiff < 300) {
                        // Click detected!
                        toggleExpandedHud()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun toggleExpandedHud() {
        if (isExpanded) {
            collapseHud()
        } else {
            expandHud()
            voiceSpeechManager.startListening()
        }
    }

    private fun expandHud() {
        isExpanded = true
        miniOrbView?.visibility = View.GONE
        expandedHudView?.visibility = View.VISIBLE
    }

    private fun collapseHud() {
        isExpanded = false
        expandedHudView?.visibility = View.GONE
        miniOrbView?.visibility = View.VISIBLE
    }

    private fun observeVoiceState() {
        serviceScope.launch {
            voiceSpeechManager.isListening.collectLatest { listening ->
                if (listening) {
                    statusTextView?.text = "LISTENING FOR DIRECTIVE..."
                    statusTextView?.setTextColor(Color.parseColor("#FFB300"))
                    orbGlowRing?.setColorFilter(Color.parseColor("#FFB300"))
                } else {
                    if (!voiceSpeechManager.isSpeaking.value) {
                        statusTextView?.text = "READY FOR DIRECTIVE"
                        statusTextView?.setTextColor(Color.parseColor("#00E5FF"))
                        orbGlowRing?.setColorFilter(Color.parseColor("#00E5FF"))
                    }
                }
            }
        }

        serviceScope.launch {
            voiceSpeechManager.isSpeaking.collectLatest { speaking ->
                if (speaking) {
                    statusTextView?.text = "SPEAKING // VOCAL HARMONIZER"
                    statusTextView?.setTextColor(Color.parseColor("#00FFCC"))
                    orbGlowRing?.setColorFilter(Color.parseColor("#00FFCC"))
                }
            }
        }

        serviceScope.launch {
            voiceSpeechManager.partialSpeechText.collectLatest { partial ->
                if (partial.isNotBlank()) {
                    transcriptTextView?.text = "\"$partial\""
                }
            }
        }
    }

    private fun handleVoiceDirective(userQuery: String) {
        if (userQuery.isBlank()) return
        transcriptTextView?.text = "\"$userQuery\""
        statusTextView?.text = "PROCESSING VIA GEMINI..."
        statusTextView?.setTextColor(Color.parseColor("#FFB300"))

        serviceScope.launch {
            val result = aiEngine.processCommand(userQuery)
            responseTextView?.text = "\"${result.spokenResponse}\""
            statusTextView?.text = "DIRECTIVE COMPLETE"
            statusTextView?.setTextColor(Color.parseColor("#00E5FF"))

            // Execute any automated tool actions over apps
            withContext(Dispatchers.Main) {
                result.actions.forEach { action ->
                    executeToolAction(action)
                }
                voiceSpeechManager.speak(result.spokenResponse)
            }
        }
    }

    private fun executeToolAction(action: ToolAction) {
        when (action.actionType) {
            "DIAL_CONTACT", "CALL_PHONE" -> {
                val number = action.phoneNumber
                if (!number.isNullOrBlank()) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(intent)
                }
            }
            "SEND_EMAIL" -> {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
                    action.recipient?.let { putExtra(Intent.EXTRA_EMAIL, arrayOf(it)) }
                    action.subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
                    action.body?.let { putExtra(Intent.EXTRA_TEXT, it) }
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
            }
            "OPEN_APP" -> {
                action.packageName?.let { pkg ->
                    packageManager.getLaunchIntentForPackage(pkg)?.let { intent ->
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                }
            }
            "SET_TIMER" -> {
                val seconds = action.numericValue?.toInt() ?: 300
                val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                    putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                    putExtra(AlarmClock.EXTRA_MESSAGE, action.title ?: "J.A.R.V.I.S. Timer")
                    putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try { startActivity(intent) } catch (_: Exception) {}
            }
            "CAPTURE_IMAGE" -> {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try { startActivity(intent) } catch (_: Exception) {}
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "J.A.R.V.I.S. Floating Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps J.A.R.V.I.S. floating Arc Reactor active over all other apps"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            1,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val listenIntent = Intent(this, FloatingJarvisService::class.java).apply {
            action = ACTION_START_LISTENING
        }
        val listenPendingIntent = PendingIntent.getService(
            this,
            2,
            listenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(this, FloatingJarvisService::class.java).apply {
            action = ACTION_STOP_OVERLAY
        }
        val dismissPendingIntent = PendingIntent.getService(
            this,
            3,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("J.A.R.V.I.S. Active Over Apps")
            .setContentText("Tap to speak directive or tap floating Arc Reactor")
            .setSmallIcon(R.drawable.ic_shortcut_lights)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .addAction(R.drawable.ic_shortcut_calendar, "Talk Now", listenPendingIntent)
            .addAction(R.drawable.ic_shortcut_lights, "Dismiss", dismissPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        voiceSpeechManager.destroy()

        overlayContainer?.let { root ->
            try {
                windowManager?.removeView(root)
            } catch (e: Exception) {
                // ignore
            }
        }
        overlayContainer = null
    }

    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        ).toInt()
    }

    private fun dpToPx(dp: Int): Int = dpToPx(dp.toFloat())
}
