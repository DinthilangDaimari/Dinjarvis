package com.example.ai

import com.example.BuildConfig
import com.example.data.model.CalendarEvent
import com.example.data.model.EmailMessage
import com.example.data.model.SmartDevice
import com.example.data.model.UserScheduleReminder
import com.example.data.repository.JarvisRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ToolAction(
    val actionType: String,
    val title: String? = null,
    val date: String? = null,
    val time: String? = null,
    val location: String? = null,
    val recipient: String? = null,
    val subject: String? = null,
    val body: String? = null,
    val contactName: String? = null,
    val phoneNumber: String? = null,
    val appName: String? = null,
    val packageName: String? = null,
    val deviceId: String? = null,
    val turnOn: Boolean? = null,
    val numericValue: Float? = null,
    val routineId: String? = null,
    val reminderPriority: String? = null,
    val reminderNotes: String? = null
)

data class JarvisAiResult(
    val spokenResponse: String,
    val thought: String = "",
    val actions: List<ToolAction> = emptyList(),
    val isGeminiPowered: Boolean = true
)

class JarvisAiEngine(
    private val repository: JarvisRepository
) {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun processCommand(userQuery: String): JarvisAiResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Instant smart fallback parser when key is default placeholder
            return@withContext parseWithLocalIntelligence(userQuery)
        }

        try {
            val systemSnapshot = repository.getSystemSnapshot()
            val requestJson = buildGeminiRequestJson(userQuery, systemSnapshot)
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestJson.toRequestBody(mediaType)

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext parseWithLocalIntelligence(userQuery, "API status: ${response.code}")
            }

            val responseBody = response.body?.string() ?: ""
            parseGeminiResponse(responseBody, userQuery)
        } catch (e: Exception) {
            parseWithLocalIntelligence(userQuery, "Network note: ${e.localizedMessage}")
        }
    }

    private fun buildGeminiRequestJson(userQuery: String, systemSnapshot: String): String {
        val systemInstructionText = """
You are J.A.R.V.I.S. (Just A Rather Very Intelligent System), the world's most advanced AI personal assistant created by Stark Industries.
Current Date: 2026-10-08.
You are assisting Tony Stark (or the User/Sir). Speak with Jarvis's iconic polite, sophisticated, slightly dry-witted and hyper-efficient British tone. Always address the user respectfully ("Sir", "Mr. Stark").

You have direct control over 5 operational domains, plus complete access to Room database Schedule Reminders and Past Context & Directives history:
1. Calendar & Reminders (view, schedule events, set reminders, query past context)
2. Email (view unread, compose, send)
3. Phone & Contacts (dial phone numbers, call contacts, check logs)
4. System Applications (launch apps like Browser, YouTube, Maps, Camera, Calculator, Clock, Settings)
5. Home Automation & IoT (control lights, thermostat, locks, blinds, arc reactor shield, activate protocols like Daybreak, Night Watch, Lab Focus, House Party)

CURRENT ENVIRONMENT SNAPSHOT & HISTORICAL MEMORY:
$systemSnapshot

When the user asks about past schedule directives, reminders, or previous context, actively utilize the retrieved memory and past context records provided above!

You MUST respond strictly with valid JSON conforming to this schema:
{
  "thought": "Brief internal Jarvis reasoning",
  "spokenResponse": "Crisp, elegant spoken response addressed to Sir",
  "actions": [
    {
      "actionType": "SCHEDULE_EVENT" | "CREATE_REMINDER" | "QUERY_CONTEXT" | "DELETE_EVENT" | "SEND_EMAIL" | "READ_EMAILS" | "CALL_PHONE" | "LAUNCH_APP" | "SET_HOME_DEVICE" | "RUN_ROUTINE" | "DIAGNOSTICS" | "SPEAK_ONLY",
      "title": "Event or reminder title",
      "date": "YYYY-MM-DD",
      "time": "HH:MM AM/PM",
      "location": "Location if applicable",
      "reminderPriority": "HIGH" | "MEDIUM" | "LOW",
      "reminderNotes": "Additional reminder notes",
      "recipient": "Email address or recipient name",
      "subject": "Email subject",
      "body": "Email body message",
      "contactName": "Contact name",
      "phoneNumber": "Phone number to dial",
      "appName": "App to launch (e.g. YouTube, Maps, Camera, Chrome, Calculator)",
      "packageName": "Android package name if known or null",
      "deviceId": "ID of smart device (e.g. light_living_room, climate_living_room, light_workshop, lock_main_vault, perimeter_defense)",
      "turnOn": true or false,
      "numericValue": 72.0 (for temp or brightness),
      "routineId": "routine_good_morning" | "routine_lab_focus" | "routine_night_watch" | "routine_party_mode"
    }
  ]
}
""".trimIndent()

        val root = JSONObject()
        val contents = JSONArray()
        val contentObj = JSONObject()
        val parts = JSONArray()
        val partObj = JSONObject()
        partObj.put("text", userQuery)
        parts.put(partObj)
        contentObj.put("parts", parts)
        contents.put(contentObj)
        root.put("contents", contents)

        val sysInstObj = JSONObject()
        val sysParts = JSONArray()
        val sysPartObj = JSONObject()
        sysPartObj.put("text", systemInstructionText)
        sysParts.put(sysPartObj)
        sysInstObj.put("parts", sysParts)
        root.put("systemInstruction", sysInstObj)

        val genConfig = JSONObject()
        genConfig.put("responseMimeType", "application/json")
        genConfig.put("temperature", 0.4)
        root.put("generationConfig", genConfig)

        return root.toString()
    }

    private suspend fun parseGeminiResponse(rawJson: String, originalQuery: String): JarvisAiResult {
        return try {
            val root = JSONObject(rawJson)
            val candidates = root.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: ""

            val cleanedText = text.trim()
                .removePrefix("```json")
                .removePrefix("```JSON")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val parsedPayload = JSONObject(cleanedText)
            val spoken = parsedPayload.optString("spokenResponse", "At your service, Sir.")
            val thought = parsedPayload.optString("thought", "")
            val actionsArray = parsedPayload.optJSONArray("actions")

            val actions = mutableListOf<ToolAction>()
            if (actionsArray != null) {
                for (i in 0 until actionsArray.length()) {
                    val obj = actionsArray.getJSONObject(i)
                    actions.add(
                        ToolAction(
                            actionType = obj.optString("actionType", "SPEAK_ONLY"),
                            title = obj.optString("title").takeIf { it.isNotEmpty() },
                            date = obj.optString("date").takeIf { it.isNotEmpty() },
                            time = obj.optString("time").takeIf { it.isNotEmpty() },
                            location = obj.optString("location").takeIf { it.isNotEmpty() },
                            reminderPriority = obj.optString("reminderPriority").takeIf { it.isNotEmpty() },
                            reminderNotes = obj.optString("reminderNotes").takeIf { it.isNotEmpty() },
                            recipient = obj.optString("recipient").takeIf { it.isNotEmpty() },
                            subject = obj.optString("subject").takeIf { it.isNotEmpty() },
                            body = obj.optString("body").takeIf { it.isNotEmpty() },
                            contactName = obj.optString("contactName").takeIf { it.isNotEmpty() },
                            phoneNumber = obj.optString("phoneNumber").takeIf { it.isNotEmpty() },
                            appName = obj.optString("appName").takeIf { it.isNotEmpty() },
                            packageName = obj.optString("packageName").takeIf { it.isNotEmpty() },
                            deviceId = obj.optString("deviceId").takeIf { it.isNotEmpty() },
                            turnOn = if (obj.has("turnOn")) obj.getBoolean("turnOn") else null,
                            numericValue = if (obj.has("numericValue")) obj.getDouble("numericValue").toFloat() else null,
                            routineId = obj.optString("routineId").takeIf { it.isNotEmpty() }
                        )
                    )
                }
            }

            JarvisAiResult(
                spokenResponse = spoken,
                thought = thought,
                actions = actions,
                isGeminiPowered = true
            )
        } catch (_: Exception) {
            parseWithLocalIntelligence(originalQuery)
        }
    }

    /**
     * Highly capable local heuristic assistant ensuring zero-latency response
     * even when offline or before the user configures their Gemini API key.
     */
    private suspend fun parseWithLocalIntelligence(query: String, note: String = ""): JarvisAiResult {
        val lower = query.lowercase().trim()
        val actions = mutableListOf<ToolAction>()
        var spoken = "Certainly, Sir. I am processing your directive."

        when {
            // Reminders explicitly
            lower.contains("remind") || lower.contains("reminder") -> {
                if (lower.contains("what") || lower.contains("list") || lower.contains("show") || lower.contains("check")) {
                    val currentReminders = repository.reminders.first().filter { !it.isCompleted }
                    if (currentReminders.isEmpty()) {
                        spoken = "You have no active schedule reminders at present, Sir."
                    } else {
                        val first = currentReminders.first()
                        spoken = "You have ${currentReminders.size} active reminders in your schedule memory, Sir. Primary task: '${first.title}', scheduled for ${first.dueDateString} at ${first.dueTimeString}."
                    }
                    actions.add(ToolAction(actionType = "QUERY_CONTEXT"))
                } else {
                    val reminderTitle = if (lower.contains("to ")) query.substringAfter("to ").trim() else "Priority Objective"
                    spoken = "I have logged a high-priority reminder for '$reminderTitle' into your memory schedule, Sir."
                    actions.add(
                        ToolAction(
                            actionType = "CREATE_REMINDER",
                            title = reminderTitle,
                            date = "2026-10-09",
                            time = "09:30 AM",
                            reminderPriority = "HIGH",
                            reminderNotes = "Logged via J.A.R.V.I.S. voice directive"
                        )
                    )
                }
            }

            // Historical context query
            lower.contains("history") || lower.contains("past") || lower.contains("earlier") || lower.contains("context") -> {
                val pastContext = repository.scheduleHistory.first()
                if (pastContext.isEmpty()) {
                    spoken = "Schedule history memory is clear, Sir."
                } else {
                    val recent = pastContext.take(2).joinToString("; ") { it.contextSummary }
                    spoken = "Retrieving past schedule context from memory, Sir: $recent."
                }
                actions.add(ToolAction(actionType = "QUERY_CONTEXT"))
            }

            // Calendar
            lower.contains("meeting") || lower.contains("schedule") || lower.contains("calendar") || lower.contains("appointment") -> {
                if (lower.contains("what") || lower.contains("show") || lower.contains("check") || lower.contains("list")) {
                    spoken = "Accessing your schedule, Sir. You have the Stark Industries Quarterly Board Review tomorrow morning at 10:00 AM, followed by Mark 85 armor calibration at 2:30 PM."
                    actions.add(ToolAction(actionType = "SPEAK_ONLY"))
                } else {
                    spoken = "Scheduling the event into your calendar right away, Sir. Reminders have been synchronized."
                    actions.add(
                        ToolAction(
                            actionType = "SCHEDULE_EVENT",
                            title = if (lower.contains("with")) "Meeting with " + query.substringAfter("with") else "Executive Briefing",
                            date = "2026-10-10",
                            time = "03:00 PM",
                            location = "Avengers Tower Ops"
                        )
                    )
                }
            }

            // Email
            lower.contains("email") || lower.contains("mail") || lower.contains("inbox") -> {
                if (lower.contains("send") || lower.contains("compose") || lower.contains("write")) {
                    spoken = "Preparing draft message for Ms. Potts, Sir. Message queued for dispatch."
                    actions.add(
                        ToolAction(
                            actionType = "SEND_EMAIL",
                            recipient = "pepper@stark.industries",
                            subject = "Project Update & Clean Energy Status",
                            body = "Pepper, I reviewed the micro-grid proposal. Proceed with contract finalization. - Tony"
                        )
                    )
                } else {
                    spoken = "You have 2 high-priority unread transmissions, Sir: one from Ms. Potts regarding the Q4 clean energy expansion, and an urgent satellite radar alert from Director Fury."
                    actions.add(ToolAction(actionType = "READ_EMAILS"))
                }
            }

            // Phone & Calls
            lower.contains("call") || lower.contains("dial") || lower.contains("phone") -> {
                val target = when {
                    lower.contains("pepper") -> Pair("Pepper Potts", "+1 (555) 019-2831")
                    lower.contains("rhodey") || lower.contains("rhodes") -> Pair("Col. James Rhodes", "+1 (555) 014-9923")
                    lower.contains("fury") -> Pair("Director Nick Fury", "+1 (555) 010-0077")
                    lower.contains("happy") -> Pair("Happy Hogan", "+1 (555) 017-3829")
                    lower.contains("peter") || lower.contains("parker") -> Pair("Peter Parker", "+1 (555) 018-4491")
                    else -> Pair("Colonel Rhodes", "+1 (555) 014-9923")
                }
                spoken = "Establishing secure frequency with ${target.first}, Sir. Dialing now."
                actions.add(
                    ToolAction(
                        actionType = "CALL_PHONE",
                        contactName = target.first,
                        phoneNumber = target.second
                    )
                )
            }

            // Applications
            lower.contains("open") || lower.contains("launch") || lower.contains("start app") -> {
                val app = when {
                    lower.contains("youtube") -> Pair("YouTube", "com.google.android.youtube")
                    lower.contains("map") -> Pair("Google Maps", "com.google.android.apps.maps")
                    lower.contains("chrome") || lower.contains("browser") -> Pair("Chrome", "com.android.chrome")
                    lower.contains("camera") -> Pair("Camera", "android.media.action.IMAGE_CAPTURE")
                    lower.contains("calc") -> Pair("Calculator", "com.google.android.calculator")
                    lower.contains("clock") || lower.contains("alarm") -> Pair("Clock", "android.intent.action.SET_ALARM")
                    else -> Pair("Browser", "com.android.chrome")
                }
                spoken = "Initializing ${app.first} interface, Sir."
                actions.add(
                    ToolAction(
                        actionType = "LAUNCH_APP",
                        appName = app.first,
                        packageName = app.second
                    )
                )
            }

            // Smart Home - Climate & Thermostat
            lower.contains("thermostat") || lower.contains("temp") || lower.contains("degrees") || lower.contains("climate") || lower.contains("heat") || lower.contains("cool") || lower.contains("ac ") || lower.endsWith("ac") -> {
                val temp = Regex("""\b(\d{2}(?:\.\d+)?)\b""").find(lower)?.groupValues?.get(1)?.toFloatOrNull() ?: 72f
                val deviceId = if (lower.contains("penthouse")) "climate_penthouse" else "climate_living_room"
                spoken = "Adjusting environmental temperature to ${temp.toInt()} degrees, Sir. Climate relay updated."
                actions.add(
                    ToolAction(
                        actionType = "SET_HOME_DEVICE",
                        deviceId = deviceId,
                        turnOn = true,
                        numericValue = temp
                    )
                )
            }

            // Smart Home - Lights
            lower.contains("light") || lower.contains("lamp") || lower.contains("illuminate") || lower.contains("illumination") || lower.contains("brightness") -> {
                val isTurnOff = lower.contains("off") || lower.contains("kill") || lower.contains("shut") || lower.contains("dark")
                val deviceId = when {
                    lower.contains("workshop") || lower.contains("lab") -> "light_workshop"
                    lower.contains("penthouse") -> "light_penthouse"
                    else -> "light_living_room"
                }
                val roomName = if (deviceId == "light_workshop") "Workshop" else "Living Room"
                if (isTurnOff) {
                    spoken = "Disengaging $roomName illumination sectors, Sir."
                    actions.add(
                        ToolAction(
                            actionType = "SET_HOME_DEVICE",
                            deviceId = deviceId,
                            turnOn = false,
                            numericValue = 0f
                        )
                    )
                } else {
                    val percentMatch = Regex("""\b(\d+)\s*(?:%|percent)\b""").find(lower)
                    val brightness = percentMatch?.groupValues?.get(1)?.toFloatOrNull() ?: 100f
                    spoken = if (brightness < 100f) {
                        "$roomName illumination dimmed to ${brightness.toInt()}%, Sir."
                    } else {
                        "$roomName illumination engaged at full output, Sir."
                    }
                    actions.add(
                        ToolAction(
                            actionType = "SET_HOME_DEVICE",
                            deviceId = deviceId,
                            turnOn = true,
                            numericValue = brightness
                        )
                    )
                }
            }

            // Smart Home - Locks & Security
            lower.contains("lock") || lower.contains("secure") || lower.contains("vault") -> {
                val isLock = !lower.contains("unlock")
                spoken = if (isLock) {
                    "Main air-lock and vault seals engaged, Sir. Perimeter barrier verified."
                } else {
                    "Disengaging air-lock seals. Vault unlocked, Sir."
                }
                actions.add(
                    ToolAction(
                        actionType = "SET_HOME_DEVICE",
                        deviceId = "lock_main_vault",
                        turnOn = isLock,
                        numericValue = if (isLock) 100f else 0f
                    )
                )
            }

            // Smart Home - Shields & Defense
            lower.contains("shield") || lower.contains("defense") || lower.contains("barrier") -> {
                spoken = "Arc Reactor harmonic shield calibrated to full defense resonance, Sir."
                actions.add(
                    ToolAction(
                        actionType = "SET_HOME_DEVICE",
                        deviceId = "perimeter_defense",
                        turnOn = true,
                        numericValue = 100f
                    )
                )
            }

            // Smart Home - Blinds
            lower.contains("blind") || lower.contains("window") || lower.contains("shade") -> {
                val open = lower.contains("open") || lower.contains("up") || lower.contains("draw")
                spoken = if (open) "Penthouse automated blinds drawn open, Sir." else "Penthouse blinds lowered for privacy, Sir."
                actions.add(
                    ToolAction(
                        actionType = "SET_HOME_DEVICE",
                        deviceId = "blinds_living_room",
                        turnOn = open,
                        numericValue = if (open) 100f else 0f
                    )
                )
            }

            // Protocols
            lower.contains("protocol") || lower.contains("good morning") || lower.contains("lockdown") || lower.contains("party") || lower.contains("night watch") -> {
                val routineId = when {
                    lower.contains("morning") || lower.contains("daybreak") -> "routine_good_morning"
                    lower.contains("night") || lower.contains("lockdown") -> "routine_night_watch"
                    lower.contains("party") -> "routine_party_mode"
                    else -> "routine_lab_focus"
                }
                spoken = when (routineId) {
                    "routine_good_morning" -> "Good morning, Sir. Penthouse blinds drawn, temperature balanced to 72 degrees. Your coffee is brewing, and the quarterly board briefing is queued."
                    "routine_night_watch" -> "Protocol Night Watch activated, Sir. Tower locked down, perimeter shielding set to maximum output."
                    "routine_party_mode" -> "Protocol House Party engaged. Ambient lighting tuned to dynamic violet, acoustics initialized."
                    else -> "Protocol Mark Calibration active, Sir. Workshop power routed to primary fabrication bench."
                }
                actions.add(
                    ToolAction(
                        actionType = "RUN_ROUTINE",
                        routineId = routineId
                    )
                )
            }

            // Diagnostics
            lower.contains("status") || lower.contains("diagnostics") || lower.contains("arc reactor") || lower.contains("how are you") || lower.contains("power") -> {
                spoken = "All core systems are nominal, Sir. Arc Reactor core operating at 99.4% output. Cellular transceiver active, perimeter defenses stable, and automation relays responding with zero latency."
                actions.add(ToolAction(actionType = "DIAGNOSTICS"))
            }

            // Default
            else -> {
                spoken = "At your command, Sir. All primary systems—calendar, schedule reminders, communications, applications, and home automation—are standing by for your directive."
                actions.add(ToolAction(actionType = "SPEAK_ONLY"))
            }
        }

        return JarvisAiResult(
            spokenResponse = spoken,
            thought = "Heuristic Jarvis intent processor ($note)",
            actions = actions,
            isGeminiPowered = false
        )
    }
}
