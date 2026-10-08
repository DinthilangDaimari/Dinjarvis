package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val date: String, // YYYY-MM-DD
    val time: String, // HH:MM
    val durationMinutes: Int = 60,
    val location: String = "Avengers Tower",
    val attendees: String = "",
    val category: String = "Stark Industries",
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "emails")
data class EmailMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderName: String,
    val senderEmail: String,
    val recipientEmail: String = "tony@stark.industries",
    val subject: String,
    val body: String,
    val dateFormatted: String,
    val isRead: Boolean = false,
    val isStarred: Boolean = false,
    val isSent: Boolean = false,
    val priority: String = "Normal", // "High", "Normal", "Low"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "contacts")
data class ContactPerson(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String,
    val role: String,
    val avatarColorHex: String = "#00E5FF",
    val isFavorite: Boolean = true
)

@Entity(tableName = "call_logs")
data class CallLogEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactName: String,
    val phone: String,
    val type: String, // "INCOMING", "OUTGOING", "MISSED"
    val timeFormatted: String,
    val durationSeconds: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "smart_devices")
data class SmartDevice(
    @PrimaryKey val id: String,
    val name: String,
    val room: String, // "Living Room", "Workshop", "Penthouse", "Arc Reactor Core", "Perimeter"
    val type: String, // "LIGHT", "THERMOSTAT", "LOCK", "CAMERA", "SHIELD", "AC"
    val isOn: Boolean = false,
    val numericValue: Float = 0f, // brightness 0..100, temp 60..85, shield 0..100
    val mode: String = "Auto",
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "smart_routines")
data class SmartRoutine(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val triggerPhrase: String,
    val iconName: String = "security",
    val isActive: Boolean = false
)

@Entity(tableName = "assistant_logs")
data class AssistantLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userInput: String,
    val jarvisResponse: String,
    val actionType: String, // "VOICE_COMMAND", "CALENDAR", "EMAIL", "CALL", "APP", "HOME", "DIAGNOSTICS"
    val status: String = "SUCCESS",
    val timestamp: Long = System.currentTimeMillis()
)
