package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CalendarEvent::class,
        EmailMessage::class,
        ContactPerson::class,
        CallLogEntry::class,
        SmartDevice::class,
        SmartRoutine::class,
        AssistantLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class JarvisDatabase : RoomDatabase() {
    abstract fun calendarDao(): CalendarDao
    abstract fun emailDao(): EmailDao
    abstract fun contactDao(): ContactDao
    abstract fun callLogDao(): CallLogDao
    abstract fun homeDao(): HomeDao
    abstract fun assistantLogDao(): AssistantLogDao

    companion object {
        @Volatile
        private var INSTANCE: JarvisDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): JarvisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JarvisDatabase::class.java,
                    "jarvis_core.db"
                )
                    .addCallback(JarvisDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class JarvisDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(database: JarvisDatabase) {
            val now = System.currentTimeMillis()

            // 1. Initial Calendar Events
            val calendarDao = database.calendarDao()
            calendarDao.insertEvent(
                CalendarEvent(
                    title = "Stark Industries Quarterly Board Meeting",
                    description = "Review clean energy deployment metrics and Arc Reactor global grid status.",
                    date = "2026-10-09",
                    time = "10:00 AM",
                    durationMinutes = 90,
                    location = "Tower Executive Suite, 93rd Floor",
                    attendees = "Pepper Potts, Stark Board Members",
                    category = "Stark Industries"
                )
            )
            calendarDao.insertEvent(
                CalendarEvent(
                    title = "Mark 85 Armor Diagnostic Calibration",
                    description = "Nanotech telemetry analysis and repulsor yield optimization test.",
                    date = "2026-10-09",
                    time = "02:30 PM",
                    durationMinutes = 60,
                    location = "Workshop Sub-level 3",
                    attendees = "Tony Stark, J.A.R.V.I.S.",
                    category = "Lab"
                )
            )
            calendarDao.insertEvent(
                CalendarEvent(
                    title = "Tactical Briefing with Colonel Rhodes",
                    description = "Review joint defense logistics and regional emergency readiness.",
                    date = "2026-10-10",
                    time = "11:15 AM",
                    durationMinutes = 45,
                    location = "Avenger Compound Ops",
                    attendees = "James Rhodes, Tony Stark",
                    category = "Defense"
                )
            )
            calendarDao.insertEvent(
                CalendarEvent(
                    title = "Private Dinner with Pepper",
                    description = "Reservation confirmed at Le Bernardin, Rooftop Terrace.",
                    date = "2026-10-10",
                    time = "07:30 PM",
                    durationMinutes = 120,
                    location = "Downtown Manhattan",
                    attendees = "Pepper Potts",
                    category = "Personal"
                )
            )

            // 2. Initial Emails
            val emailDao = database.emailDao()
            emailDao.insertEmail(
                EmailMessage(
                    senderName = "Pepper Potts",
                    senderEmail = "pepper@stark.industries",
                    subject = "Q4 Clean Energy Global Expansion Report",
                    body = "Tony, the board approved the new clean energy micro-grid proposal for Southeast Asia. Please review the attached contract signatures before our 10 AM briefing tomorrow. Love, Pepper.",
                    dateFormatted = "Today, 09:14 AM",
                    isRead = false,
                    isStarred = true,
                    priority = "High",
                    timestamp = now - 3600000
                )
            )
            emailDao.insertEmail(
                EmailMessage(
                    senderName = "Nick Fury",
                    senderEmail = "director.fury@shield.gov",
                    subject = "Priority Alpha: Planetary Defense Radar Anomaly",
                    body = "Stark, our deep space orbital sensors picked up a faint gamma signature in sector 4. Have your AI run a triangulation sweep on the telemetry logs. We need answers by midday.",
                    dateFormatted = "Today, 07:45 AM",
                    isRead = false,
                    isStarred = true,
                    priority = "High",
                    timestamp = now - 7200000
                )
            )
            emailDao.insertEmail(
                EmailMessage(
                    senderName = "Dr. Bruce Banner",
                    senderEmail = "bruce.banner@caltech.edu",
                    subject = "Gamma Decay Computations & Lab Notes",
                    body = "Hey Tony, the isotope decay rates in the chamber matched our quantum simulation. The containment coils held up without thermal spikes. Whenever you're free, take a look at the data charts.",
                    dateFormatted = "Yesterday, 04:30 PM",
                    isRead = true,
                    isStarred = false,
                    priority = "Normal",
                    timestamp = now - 86400000
                )
            )
            emailDao.insertEmail(
                EmailMessage(
                    senderName = "Peter Parker",
                    senderEmail = "peter.parker@midtownhigh.edu",
                    subject = "Quick question about the web-shooter fluid polymer!",
                    body = "Mr. Stark! Hope you're doing great. I tried modifying the tensile strength formula as you suggested in lab, and the elasticity improved by 34%! Let me know if I can drop by the workshop this weekend.",
                    dateFormatted = "Yesterday, 02:10 PM",
                    isRead = true,
                    isStarred = false,
                    priority = "Normal",
                    timestamp = now - 95000000
                )
            )

            // 3. Contacts
            val contactDao = database.contactDao()
            val contacts = listOf(
                ContactPerson(name = "Pepper Potts", phone = "+1 (555) 019-2831", email = "pepper@stark.industries", role = "CEO - Stark Industries", avatarColorHex = "#FF4081", isFavorite = true),
                ContactPerson(name = "Col. James Rhodes", phone = "+1 (555) 014-9923", email = "rhodey@usaf.mil", role = "War Machine / USAF Liaison", avatarColorHex = "#607D8B", isFavorite = true),
                ContactPerson(name = "Director Nick Fury", phone = "+1 (555) 010-0077", email = "director.fury@shield.gov", role = "Director of S.H.I.E.L.D.", avatarColorHex = "#212121", isFavorite = true),
                ContactPerson(name = "Happy Hogan", phone = "+1 (555) 017-3829", email = "happy@stark.industries", role = "Head of Asset Management", avatarColorHex = "#FFB300", isFavorite = true),
                ContactPerson(name = "Peter Parker", phone = "+1 (555) 018-4491", email = "peter.parker@midtownhigh.edu", role = "Stark Intern / Spider-Man", avatarColorHex = "#E53935", isFavorite = true),
                ContactPerson(name = "Dr. Bruce Banner", phone = "+1 (555) 012-7744", email = "bruce.banner@caltech.edu", role = "Biochemistry Specialist", avatarColorHex = "#4CAF50", isFavorite = false)
            )
            contactDao.insertContacts(contacts)

            // 4. Call Logs
            val callLogDao = database.callLogDao()
            val logs = listOf(
                CallLogEntry(contactName = "Pepper Potts", phone = "+1 (555) 019-2831", type = "INCOMING", timeFormatted = "Today, 08:30 AM", durationSeconds = 240, timestamp = now - 4000000),
                CallLogEntry(contactName = "Col. James Rhodes", phone = "+1 (555) 014-9923", type = "OUTGOING", timeFormatted = "Yesterday, 06:15 PM", durationSeconds = 185, timestamp = now - 80000000),
                CallLogEntry(contactName = "Director Nick Fury", phone = "+1 (555) 010-0077", type = "MISSED", timeFormatted = "Yesterday, 03:00 PM", durationSeconds = 0, timestamp = now - 90000000)
            )
            callLogDao.insertCallLogs(logs)

            // 5. Smart Home Devices
            val homeDao = database.homeDao()
            val devices = listOf(
                SmartDevice(id = "light_living_room", name = "Living Room Chandelier", room = "Living Room", type = "LIGHT", isOn = true, numericValue = 85f, mode = "Warm Amber"),
                SmartDevice(id = "climate_living_room", name = "Penthouse Climate Control", room = "Living Room", type = "THERMOSTAT", isOn = true, numericValue = 71f, mode = "Cool"),
                SmartDevice(id = "blinds_living_room", name = "Floor-to-Ceiling Smart Blinds", room = "Living Room", type = "AC", isOn = false, numericValue = 40f, mode = "Daylight Tint"),
                SmartDevice(id = "lock_main_vault", name = "Tower Main Secure Air-Lock", room = "Penthouse", type = "LOCK", isOn = true, numericValue = 100f, mode = "Biometric Locked"),
                SmartDevice(id = "light_workshop", name = "Workshop Primary Floodlights", room = "Workshop Lab", type = "LIGHT", isOn = true, numericValue = 100f, mode = "Electric Cyan"),
                SmartDevice(id = "climate_workshop", name = "Lab Cryo-Thermal Regulator", room = "Workshop Lab", type = "THERMOSTAT", isOn = true, numericValue = 68f, mode = "Constant Precision"),
                SmartDevice(id = "core_arc_reactor", name = "Arc Reactor Output Governor", room = "Arc Reactor Core", type = "SHIELD", isOn = true, numericValue = 99.4f, mode = "Optimal Overdrive"),
                SmartDevice(id = "perimeter_defense", name = "Perimeter Holographic Shield", room = "Perimeter", type = "SHIELD", isOn = true, numericValue = 100f, mode = "Active Guard")
            )
            homeDao.insertDevices(devices)

            // 6. Smart Routines
            val routines = listOf(
                SmartRoutine(
                    id = "routine_good_morning",
                    name = "Protocol Daybreak",
                    description = "Open penthouse blinds, warm living room lights to 80%, set thermostat to 72°F, synthesize daily schedule briefing.",
                    triggerPhrase = "Good morning Jarvis",
                    iconName = "wb_sunny",
                    isActive = false
                ),
                SmartRoutine(
                    id = "routine_lab_focus",
                    name = "Protocol Mark Calibration",
                    description = "Maximize workshop lighting to 100% cyan, silence incoming non-urgent calls, prep telemetry monitors.",
                    triggerPhrase = "Initiate lab focus",
                    iconName = "biotech",
                    isActive = true
                ),
                SmartRoutine(
                    id = "routine_night_watch",
                    name = "Protocol Night Watch",
                    description = "Lock all tower access points, engage perimeter holographic shield, dim interior lighting to 15%.",
                    triggerPhrase = "Jarvis, lockdown the tower",
                    iconName = "security",
                    isActive = false
                ),
                SmartRoutine(
                    id = "routine_party_mode",
                    name = "Protocol House Party",
                    description = "Switch ambient lighting to pulsating neon violet, set temperature to 68°F, activate surround acoustics.",
                    triggerPhrase = "Jarvis, party mode",
                    iconName = "celebration",
                    isActive = false
                )
            )
            homeDao.insertRoutines(routines)

            // 7. Initial Assistant Log
            val assistantLogDao = database.assistantLogDao()
            assistantLogDao.insertLog(
                AssistantLog(
                    userInput = "System Initialization",
                    jarvisResponse = "All systems operational, Sir. Arc Reactor core at 99.4% efficiency. Calendar, communications, application controls, and environmental automation standing by for your command.",
                    actionType = "DIAGNOSTICS",
                    status = "SUCCESS"
                )
            )
        }
    }
}
