package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalendarEvent
import com.example.data.model.ScheduleHistoryContext
import com.example.data.model.UserScheduleReminder
import com.example.ui.theme.JarvisAlert
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBg
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisNeonTeal
import com.example.ui.theme.JarvisOnline
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.viewmodel.JarvisViewModel

enum class CalendarViewMode {
    SCHEDULE,
    REMINDERS,
    HISTORY_CONTEXT
}

@Composable
fun CalendarScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val events by viewModel.events.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val historyContext by viewModel.scheduleHistory.collectAsState()
    val triggerAddMeetingDialog by viewModel.showScheduleMeetingDialog.collectAsState()

    var viewMode by remember { mutableStateOf(CalendarViewMode.SCHEDULE) }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddEventDialog by remember { mutableStateOf(false) }
    var showAddReminderDialog by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(triggerAddMeetingDialog) {
        if (triggerAddMeetingDialog) {
            viewMode = CalendarViewMode.SCHEDULE
            showAddEventDialog = true
            viewModel.clearScheduleMeetingDialog()
        }
    }

    val categories = listOf("All", "Stark Industries", "Lab", "Defense", "Personal")
    val filteredEvents = if (selectedCategory == "All") events else events.filter { it.category == selectedCategory }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBg)
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SCHEDULE & MEMORY",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = JarvisCyan
                )
                Text(
                    text = "${events.size} briefings • ${reminders.count { !it.isCompleted }} active reminders",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 13.sp,
                    color = JarvisTextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(JarvisCyan.copy(alpha = 0.2f))
                    .border(1.dp, JarvisCyan, RoundedCornerShape(12.dp))
                    .clickable {
                        if (viewMode == CalendarViewMode.REMINDERS) {
                            showAddReminderDialog = true
                        } else {
                            showAddEventDialog = true
                        }
                    }
                    .testTag("schedule_add_button")
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = JarvisCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (viewMode == CalendarViewMode.REMINDERS) "NEW REMINDER" else "NEW EVENT",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisCyan
                    )
                }
            }
        }

        // View Mode Switcher: SCHEDULE vs REMINDERS vs HISTORY CONTEXT
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(JarvisSurfaceVariant)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CalendarViewMode.values().forEach { mode ->
                val isSelected = viewMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) JarvisCyan else Color.Transparent)
                        .clickable { viewMode = mode }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (mode) {
                            CalendarViewMode.SCHEDULE -> "EVENTS (${events.size})"
                            CalendarViewMode.REMINDERS -> "REMINDERS (${reminders.count { !it.isCompleted }})"
                            CalendarViewMode.HISTORY_CONTEXT -> "MEMORY LOG"
                        },
                        fontFamily = OrbitronFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.Black else JarvisTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Voice directive tip
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisSurfaceVariant),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, JarvisCyan.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (viewMode) {
                        CalendarViewMode.SCHEDULE -> Icons.Default.DateRange
                        CalendarViewMode.REMINDERS -> Icons.Default.NotificationsActive
                        CalendarViewMode.HISTORY_CONTEXT -> Icons.Default.History
                    },
                    contentDescription = null,
                    tint = JarvisAmber,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (viewMode) {
                        CalendarViewMode.SCHEDULE -> "Voice: \"Jarvis, schedule meeting with Pepper tomorrow at 10 AM\""
                        CalendarViewMode.REMINDERS -> "Voice: \"Jarvis, remind me to check the repulsor coils at 2 PM\""
                        CalendarViewMode.HISTORY_CONTEXT -> "Voice: \"Jarvis, retrieve past schedule context from memory\""
                    },
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 12.sp,
                    color = JarvisTextPrimary
                )
            }
        }

        // Content according to view mode
        when (viewMode) {
            CalendarViewMode.SCHEDULE -> {
                // Category filter chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) JarvisCyan else JarvisSurface)
                                .border(1.dp, if (isSelected) JarvisCyan else JarvisCardBorder, RoundedCornerShape(10.dp))
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = category,
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else JarvisTextSecondary
                            )
                        }
                    }
                }

                // Event List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (filteredEvents.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No scheduled events in this sector.",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 14.sp,
                                    color = JarvisTextSecondary
                                )
                            }
                        }
                    } else {
                        items(filteredEvents) { event ->
                            CalendarEventCard(
                                event = event,
                                onDelete = { viewModel.deleteEvent(event) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }

            CalendarViewMode.REMINDERS -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (reminders.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No active reminders stored in database.",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 14.sp,
                                    color = JarvisTextSecondary
                                )
                            }
                        }
                    } else {
                        items(reminders) { reminder ->
                            ReminderItemCard(
                                reminder = reminder,
                                onToggle = { viewModel.toggleReminder(reminder.id, !reminder.isCompleted) },
                                onDelete = { viewModel.deleteReminder(reminder) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }

            CalendarViewMode.HISTORY_CONTEXT -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (historyContext.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No schedule history memory records.",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 14.sp,
                                    color = JarvisTextSecondary
                                )
                            }
                        }
                    } else {
                        items(historyContext) { contextItem ->
                            HistoryContextCard(item = contextItem)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Add Event Dialog
    if (showAddEventDialog) {
        var title by remember { mutableStateOf("") }
        var date by remember { mutableStateOf("2026-10-10") }
        var time by remember { mutableStateOf("02:00 PM") }
        var location by remember { mutableStateOf("Avengers Tower Ops") }
        var category by remember { mutableStateOf("Stark Industries") }

        AlertDialog(
            onDismissRequest = { showAddEventDialog = false },
            title = {
                Text(
                    text = "SCHEDULE APPOINTMENT",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 16.sp,
                    color = JarvisCyan
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Event Title") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time (e.g. 10:00 AM)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.addNewEvent(title, date, time, location, category)
                            showAddEventDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                ) {
                    Text("REGISTER", color = Color.Black, fontFamily = OrbitronFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEventDialog = false }) {
                    Text("CANCEL", color = JarvisTextSecondary, fontFamily = OrbitronFontFamily)
                }
            },
            containerColor = JarvisSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Add Reminder Dialog
    if (showAddReminderDialog) {
        var title by remember { mutableStateOf("") }
        var date by remember { mutableStateOf("2026-10-09") }
        var time by remember { mutableStateOf("09:30 AM") }
        var priority by remember { mutableStateOf("HIGH") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddReminderDialog = false },
            title = {
                Text(
                    text = "SET SCHEDULE REMINDER",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 16.sp,
                    color = JarvisAmber
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Reminder Title / Directive") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisAmber,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Due Date") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisAmber,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Due Time") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisAmber,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Context") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisAmber,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.addNewReminder(title, date, time, priority, notes)
                            showAddReminderDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisAmber)
                ) {
                    Text("SAVE REMINDER", color = Color.Black, fontFamily = OrbitronFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddReminderDialog = false }) {
                    Text("CANCEL", color = JarvisTextSecondary, fontFamily = OrbitronFontFamily)
                }
            },
            containerColor = JarvisSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun ReminderItemCard(
    reminder: UserScheduleReminder,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("reminder_${reminder.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = JarvisSurface),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (reminder.isCompleted) JarvisCardBorder else JarvisAmber.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (reminder.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Toggle Reminder",
                        tint = if (reminder.isCompleted) JarvisOnline else JarvisAmber,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = reminder.title,
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (reminder.isCompleted) JarvisTextSecondary else JarvisTextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (reminder.priority == "HIGH") JarvisAlert.copy(alpha = 0.2f) else JarvisCyan.copy(alpha = 0.2f))
                                .border(0.5.dp, if (reminder.priority == "HIGH") JarvisAlert else JarvisCyan, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = reminder.priority,
                                fontFamily = OrbitronFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (reminder.priority == "HIGH") JarvisAlert else JarvisCyan
                            )
                        }
                    }

                    if (reminder.notes.isNotBlank()) {
                        Text(
                            text = reminder.notes,
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 12.sp,
                            color = JarvisTextSecondary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = JarvisAmber,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${reminder.dueDateString} • ${reminder.dueTimeString}",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 12.sp,
                            color = JarvisAmber
                        )
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Reminder",
                    tint = JarvisTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun HistoryContextCard(
    item: ScheduleHistoryContext,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = JarvisSurface),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, JarvisCardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(JarvisCyan.copy(alpha = 0.15f))
                        .border(0.5.dp, JarvisCyan, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.resolvedAction,
                        fontFamily = OrbitronFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisCyan
                    )
                }

                Text(
                    text = "MEMORY ARCHIVE",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 9.sp,
                    color = JarvisTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Directive: \"${item.userDirective}\"",
                fontFamily = RajdhaniFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = JarvisAmber
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Retrieved Context: ${item.contextSummary}",
                fontFamily = RajdhaniFontFamily,
                fontSize = 13.sp,
                color = JarvisTextPrimary
            )
        }
    }
}

@Composable
fun CalendarEventCard(
    event: CalendarEvent,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("event_card_${event.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = JarvisSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(JarvisCyan.copy(alpha = 0.15f))
                        .border(0.5.dp, JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = event.category.uppercase(),
                        fontFamily = OrbitronFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisCyan
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Event",
                        tint = JarvisTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = event.title,
                fontFamily = RajdhaniFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = JarvisTextPrimary
            )

            if (event.description.isNotBlank()) {
                Text(
                    text = event.description,
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 13.sp,
                    color = JarvisTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = JarvisAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${event.date} • ${event.time}",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 12.sp,
                        color = JarvisAmber
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = JarvisTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.location,
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 12.sp,
                        color = JarvisTextSecondary
                    )
                }
            }
        }
    }
}
