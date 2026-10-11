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
import com.example.data.model.ScheduleHistoryContext
import com.example.data.model.SmartDevice
import com.example.data.model.SmartRoutine
import com.example.data.model.UserScheduleReminder
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

    // User Schedule Reminders
    @Query("SELECT * FROM schedule_reminders ORDER BY isCompleted ASC, timestamp DESC")
    fun getAllReminders(): Flow<List<UserScheduleReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: UserScheduleReminder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<UserScheduleReminder>)

    @Update
    suspend fun updateReminder(reminder: UserScheduleReminder)

    @Delete
    suspend fun deleteReminder(reminder: UserScheduleReminder)

    @Query("UPDATE schedule_reminders SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun toggleReminderCompleted(id: Long, isCompleted: Boolean)
}

@Dao
interface ScheduleHistoryDao {
    @Query("SELECT * FROM schedule_history_context ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<ScheduleHistoryContext>>

    @Query("SELECT * FROM schedule_history_context ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 20): Flow<List<ScheduleHistoryContext>>

    @Query("SELECT * FROM schedule_history_context ORDER BY timestamp DESC LIMIT 10")
    suspend fun getRecentHistorySnapshot(): List<ScheduleHistoryContext>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: ScheduleHistoryContext): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryList(items: List<ScheduleHistoryContext>)

    @Query("SELECT * FROM schedule_history_context WHERE userDirective LIKE '%' || :query || '%' OR contextSummary LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchHistory(query: String): List<ScheduleHistoryContext>
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

    @Query("SELECT * FROM smart_devices WHERE id = :id LIMIT 1")
    suspend fun getDeviceById(id: String): SmartDevice?

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
