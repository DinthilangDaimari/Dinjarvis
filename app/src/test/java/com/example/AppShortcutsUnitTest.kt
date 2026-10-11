package com.example

import com.example.util.HomeScreenHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AppShortcutsUnitTest {

    @Test
    fun shortcutConstants_areCorrect() {
        assertEquals("schedule_meeting", HomeScreenHelper.SHORTCUT_SCHEDULE_MEETING)
        assertEquals("toggle_smart_lights", HomeScreenHelper.SHORTCUT_TOGGLE_LIGHTS)
    }

    @Test
    fun shortcutIdentifiers_areNotNull() {
        assertNotNull(HomeScreenHelper.SHORTCUT_SCHEDULE_MEETING)
        assertNotNull(HomeScreenHelper.SHORTCUT_TOGGLE_LIGHTS)
    }
}
