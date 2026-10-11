package com.example.data.repository

import com.example.data.dao.AssistantLogDao
import com.example.data.dao.CalendarDao
import com.example.data.dao.CallLogDao
import com.example.data.dao.ContactDao
import com.example.data.dao.EmailDao
import com.example.data.dao.HomeDao
import com.example.data.dao.ScheduleHistoryDao
import com.example.data.model.AssistantLog
import com.example.data.model.CalendarEvent
import com.example.data.model.CallLogEntry
import com.example.data.model.ContactPerson
import com.example.data.model.EmailMessage
import com.example.data.model.ScheduleHistoryContext
import com.example.data.model.SmartDevice
import com.example.data.model.SmartRoutine
import com.example.data.model.UserScheduleReminder
import com.example.smarthome.service.SmartHomeManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class JarvisRepository(
    private val calendarDao: CalendarDao,
    private val scheduleHistoryDao: ScheduleHistoryDao,
    private val emailDao: EmailDao,
    private val contactDao: ContactDao,
    private val callLogDao: CallLogDao,
    private val homeDao: HomeDao,
    private val assistantLogDao: AssistantLogDao,
    val smartHomeManager: SmartHomeManager
) {
    // Flows
    val events: Flow<List<CalendarEvent>> = calendarDao.getAllEvents()
    val reminders: Flow<List<UserScheduleReminder>> = calendarDao.getAllReminders()
    val scheduleHistory: Flow<List<ScheduleHistoryContext>> = scheduleHistoryDao.getAllHistory()
    val emails: Flow<List<EmailMessage>> = emailDao.getAllEmails()
    val contacts: Flow<List<ContactPerson>> = contactDao.getAllContacts()
    val callLogs: Flow<List<CallLogEntry>> = callLogDao.getAllCallLogs()
    val devices: Flow<List<SmartDevice>> = homeDao.getAllDevices()
    val routines: Flow<List<SmartRoutine>> = homeDao.getAllRoutines()
    val recentLogs: Flow<List<AssistantLog>> = assistantLogDao.getRecentLogs()

    // Calendar
    suspend fun addEvent(event: CalendarEvent): Long {
        val id = calendarDao.insertEvent(event)
        recordScheduleHistory(
            directive = "Scheduled event '${event.title}'",
            action = "SCHEDULE_CREATED",
            summary = "Event '${event.title}' registered on ${event.date} at ${event.time} (${event.location}).",
            title = event.title
        )
        return id
    }

    suspend fun updateEvent(event: CalendarEvent) {
        calendarDao.updateEvent(event)
        recordScheduleHistory(
            directive = "Updated event '${event.title}'",
            action = "SCHEDULE_MODIFIED",
            summary = "Modified '${event.title}' for ${event.date} at ${event.time}.",
            title = event.title
        )
    }

    suspend fun deleteEvent(event: CalendarEvent) {
        calendarDao.deleteEvent(event)
        recordScheduleHistory(
            directive = "Removed event '${event.title}'",
            action = "SCHEDULE_DELETED",
            summary = "Cancelled scheduled briefing '${event.title}'.",
            title = event.title
        )
    }

    suspend fun deleteEventById(id: Long) = calendarDao.deleteEventById(id)

    // User Reminders
    suspend fun addReminder(reminder: UserScheduleReminder): Long {
        val id = calendarDao.insertReminder(reminder)
        recordScheduleHistory(
            directive = "Set reminder '${reminder.title}'",
            action = "REMINDER_SET",
            summary = "Reminder created for ${reminder.dueDateString} at ${reminder.dueTimeString} [Priority: ${reminder.priority}].",
            title = reminder.title
        )
        return id
    }

    suspend fun updateReminder(reminder: UserScheduleReminder) = calendarDao.updateReminder(reminder)
    suspend fun deleteReminder(reminder: UserScheduleReminder) = calendarDao.deleteReminder(reminder)
    suspend fun toggleReminder(id: Long, isCompleted: Boolean) {
        calendarDao.toggleReminderCompleted(id, isCompleted)
    }

    // Schedule Context & History Memory
    suspend fun recordScheduleHistory(
        directive: String,
        action: String,
        summary: String,
        title: String = ""
    ): Long {
        return scheduleHistoryDao.insertHistory(
            ScheduleHistoryContext(
                userDirective = directive,
                resolvedAction = action,
                contextSummary = summary,
                relatedTitle = title
            )
        )
    }

    suspend fun searchHistoryContext(query: String): List<ScheduleHistoryContext> {
        return scheduleHistoryDao.searchHistory(query)
    }

    // Emails
    suspend fun addEmail(email: EmailMessage): Long = emailDao.insertEmail(email)
    suspend fun setReadStatus(id: Long, isRead: Boolean) = emailDao.updateReadStatus(id, isRead)
    suspend fun toggleEmailStar(id: Long) = emailDao.toggleStar(id)
    suspend fun deleteEmail(email: EmailMessage) = emailDao.deleteEmail(email)

    // Contacts & Calls
    suspend fun addContact(contact: ContactPerson): Long = contactDao.insertContact(contact)
    suspend fun deleteContact(contact: ContactPerson) = contactDao.deleteContact(contact)
    suspend fun addCallLog(log: CallLogEntry): Long = callLogDao.insertCallLog(log)

    // Home Automation & External Smart Home API Service
    suspend fun setDevicePower(id: String, isOn: Boolean) {
        val device = homeDao.getDeviceById(id)
        val type = device?.type ?: when {
            id.contains("climate") || id.contains("thermostat") -> "THERMOSTAT"
            id.contains("lock") -> "LOCK"
            id.contains("shield") || id.contains("switch") -> "SHIELD"
            else -> "LIGHT"
        }

        when (type) {
            "THERMOSTAT" -> smartHomeManager.setThermostat(id, if (isOn) (device?.numericValue ?: 72f) else 65f)
            "LOCK" -> smartHomeManager.setLockState(id, isOn)
            "SHIELD", "SWITCH" -> smartHomeManager.setSwitchState(id, isOn)
            else -> smartHomeManager.setLightState(id, isOn)
        }
    }

    suspend fun setDeviceValue(id: String, value: Float) {
        val device = homeDao.getDeviceById(id)
        val type = device?.type ?: when {
            id.contains("climate") || id.contains("thermostat") || id.contains("temp") -> "THERMOSTAT"
            else -> "LIGHT"
        }

        if (type == "THERMOSTAT") {
            smartHomeManager.setThermostat(id, value)
        } else {
            smartHomeManager.setLightState(id, true, value.toInt())
        }
    }

    suspend fun updateDevice(device: SmartDevice) = homeDao.updateDevice(device)

    suspend fun syncSmartHomeHub(): Result<Int> = smartHomeManager.syncEntitiesFromHub()

    suspend fun testSmartHomeConnection(): Result<String> = smartHomeManager.testConnection()

    fun updateSmartHomeCredentials(type: String, url: String, token: String) {
        smartHomeManager.updateCredentials(type, url, token)
    }

    suspend fun toggleRoutine(id: String, isActive: Boolean) {
        homeDao.setRoutineActive(id, isActive)
        if (isActive) {
            applyRoutineEffects(id)
        }
    }

    private suspend fun applyRoutineEffects(routineId: String) {
        when (routineId) {
            "routine_good_morning" -> {
                setDevicePower("light_living_room", true)
                setDeviceValue("light_living_room", 80f)
                setDevicePower("blinds_living_room", true)
                setDeviceValue("climate_living_room", 72f)
            }
            "routine_lab_focus" -> {
                setDevicePower("light_workshop", true)
                setDeviceValue("light_workshop", 100f)
                setDevicePower("climate_workshop", true)
                setDeviceValue("climate_workshop", 68f)
            }
            "routine_night_watch" -> {
                setDevicePower("lock_main_vault", true)
                setDevicePower("perimeter_defense", true)
                setDevicePower("light_living_room", true)
                setDeviceValue("light_living_room", 15f)
            }
            "routine_party_mode" -> {
                setDevicePower("light_living_room", true)
                setDeviceValue("light_living_room", 95f)
                setDevicePower("climate_living_room", true)
                setDeviceValue("climate_living_room", 68f)
            }
        }
    }

    // Assistant Logs
    suspend fun logAssistantAction(
        input: String,
        response: String,
        actionType: String = "VOICE_COMMAND",
        status: String = "SUCCESS"
    ) {
        assistantLogDao.insertLog(
            AssistantLog(
                userInput = input,
                jarvisResponse = response,
                actionType = actionType,
                status = status
            )
        )
    }

    // Snapshot context for Gemini AI with Smart Home Hub link status
    suspend fun getSystemSnapshot(): String {
        val currentEvents = events.first()
        val currentReminders = reminders.first()
        val currentHistory = scheduleHistoryDao.getRecentHistorySnapshot()
        val currentEmails = emails.first()
        val currentContacts = contacts.first()
        val currentDevices = devices.first()
        val currentRoutines = routines.first()

        val sb = StringBuilder()
        sb.append("Current Calendar Events:\n")
        currentEvents.take(5).forEach {
            sb.append("- [ID:${it.id}] ${it.title} on ${it.date} at ${it.time} (${it.location})\n")
        }

        sb.append("\nActive Reminders:\n")
        val activeReminders = currentReminders.filter { !it.isCompleted }
        if (activeReminders.isEmpty()) {
            sb.append("- None active\n")
        } else {
            activeReminders.take(5).forEach {
                sb.append("- [Reminder ID:${it.id}] ${it.title} due ${it.dueDateString} at ${it.dueTimeString} [Priority: ${it.priority}]\n")
            }
        }

        sb.append("\nRetrieved Past Schedule Context & History (Memory):\n")
        if (currentHistory.isEmpty()) {
            sb.append("- No recent history\n")
        } else {
            currentHistory.take(5).forEach {
                sb.append("- Past Directive: '${it.userDirective}' -> Action: ${it.resolvedAction} (${it.contextSummary})\n")
            }
        }

        sb.append("\nEmails:\n")
        val unreadEmails = currentEmails.filter { !it.isRead }
        sb.append("Unread: ${unreadEmails.size}\n")
        unreadEmails.take(4).forEach {
            sb.append("- [ID:${it.id}] From: ${it.senderName} (${it.senderEmail}) | Subject: '${it.subject}' | Priority: ${it.priority}\n")
        }

        sb.append("\nContacts:\n")
        currentContacts.take(5).forEach {
            sb.append("- ${it.name} (${it.role}): ${it.phone}\n")
        }

        sb.append("\nSmart Home Automation Devices (Hub connected: ${smartHomeManager.isConnected.value}):\n")
        currentDevices.forEach {
            sb.append("- [ID:${it.id}] ${it.name} in ${it.room}: ${if (it.isOn) "ON" else "OFF"} (Value: ${it.numericValue.toInt()}, Mode: ${it.mode})\n")
        }

        sb.append("\nRoutines:\n")
        currentRoutines.forEach {
            sb.append("- [ID:${it.id}] ${it.name}: ${if (it.isActive) "ACTIVE" else "INACTIVE"} (Trigger: '${it.triggerPhrase}')\n")
        }

        return sb.toString()
    }
}
