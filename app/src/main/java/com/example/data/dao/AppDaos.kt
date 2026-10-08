package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AssistantLog
import com.example.data.model.CalendarEvent
import com.example.data.model.CallLogEntry
import com.example.data.model.ContactPerson
import com.example.data.model.EmailMessage
import com.example.data.model.SmartDevice
import com.example.data.model.SmartRoutine
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar_events ORDER BY date ASC, time ASC")
    fun getAllEvents(): Flow<List<CalendarEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEvent): Long

    @Update
    suspend fun updateEvent(event: CalendarEvent)

    @Delete
    suspend fun deleteEvent(event: CalendarEvent)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteEventById(id: Long)
}

@Dao
interface EmailDao {
    @Query("SELECT * FROM emails ORDER BY timestamp DESC")
    fun getAllEmails(): Flow<List<EmailMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmail(email: EmailMessage): Long

    @Update
    suspend fun updateEmail(email: EmailMessage)

    @Delete
    suspend fun deleteEmail(email: EmailMessage)

    @Query("UPDATE emails SET isRead = :isRead WHERE id = :id")
    suspend fun updateReadStatus(id: Long, isRead: Boolean)

    @Query("UPDATE emails SET isStarred = NOT isStarred WHERE id = :id")
    suspend fun toggleStar(id: Long)
}

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY isFavorite DESC, name ASC")
    fun getAllContacts(): Flow<List<ContactPerson>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactPerson): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactPerson>)

    @Delete
    suspend fun deleteContact(contact: ContactPerson)
}

@Dao
interface CallLogDao {
    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC")
    fun getAllCallLogs(): Flow<List<CallLogEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLog(callLog: CallLogEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLogs(callLogs: List<CallLogEntry>)
}

@Dao
interface HomeDao {
    @Query("SELECT * FROM smart_devices ORDER BY room ASC, name ASC")
    fun getAllDevices(): Flow<List<SmartDevice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevices(devices: List<SmartDevice>)

    @Update
    suspend fun updateDevice(device: SmartDevice)

    @Query("UPDATE smart_devices SET isOn = :isOn, lastUpdated = :now WHERE id = :id")
    suspend fun updateDevicePower(id: String, isOn: Boolean, now: Long = System.currentTimeMillis())

    @Query("UPDATE smart_devices SET numericValue = :value, lastUpdated = :now WHERE id = :id")
    suspend fun updateDeviceValue(id: String, value: Float, now: Long = System.currentTimeMillis())

    @Query("SELECT * FROM smart_routines")
    fun getAllRoutines(): Flow<List<SmartRoutine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutines(routines: List<SmartRoutine>)

    @Update
    suspend fun updateRoutine(routine: SmartRoutine)

    @Query("UPDATE smart_routines SET isActive = :isActive WHERE id = :id")
    suspend fun setRoutineActive(id: String, isActive: Boolean)
}

@Dao
interface AssistantLogDao {
    @Query("SELECT * FROM assistant_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentLogs(): Flow<List<AssistantLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AssistantLog): Long

    @Query("DELETE FROM assistant_logs")
    suspend fun clearLogs()
}
