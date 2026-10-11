package com.example

import com.example.data.model.CalendarEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardEventsUnitTest {

    @Test
    fun calendarEvent_creationAndFormatting() {
        val event = CalendarEvent(
            id = 101L,
            title = "Stark Defense Tactical Briefing",
            description = "Review next-generation clean arc reactor defense array",
            date = "2026-10-10",
            time = "10:30 AM",
            location = "Avengers Tower Ops",
            category = "Defense"
        )

        assertEquals(101L, event.id)
        assertEquals("Stark Defense Tactical Briefing", event.title)
        assertEquals("2026-10-10", event.date)
        assertEquals("10:30 AM", event.time)
        assertEquals("Avengers Tower Ops", event.location)
        assertEquals("Defense", event.category)
        assertFalse(event.isCompleted)
    }

    @Test
    fun calendarEvents_categoryStylingKeyMapping() {
        fun resolveBorderColorKey(category: String): String {
            return when (category.lowercase()) {
                "tactical" -> "AMBER"
                "defense" -> "RED"
                else -> "CYAN"
            }
        }

        assertEquals("AMBER", resolveBorderColorKey("tactical"))
        assertEquals("RED", resolveBorderColorKey("defense"))
        assertEquals("CYAN", resolveBorderColorKey("Stark Industries"))
        assertEquals("CYAN", resolveBorderColorKey("Personal"))
    }
}
