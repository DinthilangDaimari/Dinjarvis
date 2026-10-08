package com.example.data.repository

import com.example.data.dao.AssistantLogDao
import com.example.data.dao.CalendarDao
import com.example.data.dao.CallLogDao
import com.example.data.dao.ContactDao
import com.example.data.dao.EmailDao
import com.example.data.dao.HomeDao
import com.example.data.model.AssistantLog
import com.example.data.model.CalendarEvent
import com.example.data.model.CallLogEntry
import com.example.data.model.ContactPerson
import com.example.data.model.EmailMessage
import com.example.data.model.SmartDevice
import com.example.data.model.SmartRoutine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class JarvisRepository(
    private val calendarDao: CalendarDao,
    private val emailDao: EmailDao,
    private val contactDao: ContactDao,
    private val callLogDao: CallLogDao,
    private val homeDao: HomeDao,
    private val assistantLogDao: AssistantLogDao
) {
    // Flows
    val events: Flow<List<CalendarEvent>> = calendarDao.getAllEvents()
    val emails: Flow<List<EmailMessage>> = emailDao.getAllEmails()
    val contacts: Flow<List<ContactPerson>> = contactDao.getAllContacts()
    val callLogs: Flow<List<CallLogEntry>> = callLogDao.getAllCallLogs()
    val devices: Flow<List<SmartDevice>> = homeDao.getAllDevices()
    val routines: Flow<List<SmartRoutine>> = homeDao.getAllRoutines()
    val recentLogs: Flow<List<AssistantLog>> = assistantLogDao.getRecentLogs()

    // Calendar
    suspend fun addEvent(event: CalendarEvent): Long = calendarDao.insertEvent(event)
    suspend fun updateEvent(event: CalendarEvent) = calendarDao.updateEvent(event)
    suspend fun deleteEvent(event: CalendarEvent) = calendarDao.deleteEvent(event)
    suspend fun deleteEventById(id: Long) = calendarDao.deleteEventById(id)

    // Emails
    suspend fun addEmail(email: EmailMessage): Long = emailDao.insertEmail(email)
    suspend fun setReadStatus(id: Long, isRead: Boolean) = emailDao.updateReadStatus(id, isRead)
    suspend fun toggleEmailStar(id: Long) = emailDao.toggleStar(id)
    suspend fun deleteEmail(email: EmailMessage) = emailDao.deleteEmail(email)

    // Contacts & Calls
    suspend fun addContact(contact: ContactPerson): Long = contactDao.insertContact(contact)
    suspend fun deleteContact(contact: ContactPerson) = contactDao.deleteContact(contact)
    suspend fun addCallLog(log: CallLogEntry): Long = callLogDao.insertCallLog(log)

    // Home Automation
    suspend fun setDevicePower(id: String, isOn: Boolean) = homeDao.updateDevicePower(id, isOn)
    suspend fun setDeviceValue(id: String, value: Float) = homeDao.updateDeviceValue(id, value)
    suspend fun updateDevice(device: SmartDevice) = homeDao.updateDevice(device)
    suspend fun toggleRoutine(id: String, isActive: Boolean) {
        homeDao.setRoutineActive(id, isActive)
        if (isActive) {
            applyRoutineEffects(id)
        }
    }

    private suspend fun applyRoutineEffects(routineId: String) {
        when (routineId) {
            "routine_good_morning" -> {
                homeDao.updateDevicePower("light_living_room", true)
                homeDao.updateDeviceValue("light_living_room", 80f)
                homeDao.updateDevicePower("blinds_living_room", true)
                homeDao.updateDeviceValue("climate_living_room", 72f)
            }
            "routine_lab_focus" -> {
                homeDao.updateDevicePower("light_workshop", true)
                homeDao.updateDeviceValue("light_workshop", 100f)
                homeDao.updateDevicePower("climate_workshop", true)
                homeDao.updateDeviceValue("climate_workshop", 68f)
            }
            "routine_night_watch" -> {
                homeDao.updateDevicePower("lock_main_vault", true)
                homeDao.updateDevicePower("perimeter_defense", true)
                homeDao.updateDevicePower("light_living_room", true)
                homeDao.updateDeviceValue("light_living_room", 15f)
            }
            "routine_party_mode" -> {
                homeDao.updateDevicePower("light_living_room", true)
                homeDao.updateDeviceValue("light_living_room", 95f)
                homeDao.updateDevicePower("climate_living_room", true)
                homeDao.updateDeviceValue("climate_living_room", 68f)
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

    // Snapshot context for Gemini AI
    suspend fun getSystemSnapshot(): String {
        val currentEvents = events.first()
        val currentEmails = emails.first()
        val currentContacts = contacts.first()
        val currentDevices = devices.first()
        val currentRoutines = routines.first()

        val sb = StringBuilder()
        sb.append("Current Calendar Events:\n")
        currentEvents.take(5).forEach {
            sb.append("- [ID:${it.id}] ${it.title} on ${it.date} at ${it.time} (${it.location})\n")
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

        sb.append("\nSmart Home Automation Devices:\n")
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
