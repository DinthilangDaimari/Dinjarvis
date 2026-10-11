package com.example

import com.example.ai.ToolAction
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VoiceSpeechGeminiIntentUnitTest {

    @Test
    fun speechRecognizer_resultExtractionSimulation() {
        var capturedText = ""
        val onVoiceInputFinal: (String) -> Unit = { text ->
            capturedText = text
        }

        // Simulate SpeechRecognizer delivering recognized speech text
        val recognizedDirective = "schedule executive meeting with Pepper Potts"
        if (recognizedDirective.isNotBlank()) {
            onVoiceInputFinal(recognizedDirective)
        }

        assertEquals("schedule executive meeting with Pepper Potts", capturedText)
    }

    @Test
    fun geminiResponse_jsonParsingAndIntentExtraction() {
        val sampleGeminiJsonResponse = """
            {
              "thought": "User wishes to schedule an urgent tactical briefing with Ms. Potts.",
              "spokenResponse": "Scheduling the executive briefing with Ms. Potts, Sir.",
              "actions": [
                {
                  "actionType": "SCHEDULE_EVENT",
                  "title": "Meeting with Pepper Potts",
                  "date": "2026-10-10",
                  "time": "03:00 PM",
                  "location": "Avengers Tower Ops"
                }
              ]
            }
        """.trimIndent()

        val parsedPayload = JSONObject(sampleGeminiJsonResponse)
        val spoken = parsedPayload.optString("spokenResponse")
        val thought = parsedPayload.optString("thought")
        val actionsArray = parsedPayload.optJSONArray("actions")

        assertNotNull(actionsArray)
        assertEquals(1, actionsArray!!.length())

        val actionObj = actionsArray.getJSONObject(0)
        val action = ToolAction(
            actionType = actionObj.optString("actionType"),
            title = actionObj.optString("title"),
            date = actionObj.optString("date"),
            time = actionObj.optString("time"),
            location = actionObj.optString("location")
        )

        assertEquals("Scheduling the executive briefing with Ms. Potts, Sir.", spoken)
        assertTrue(thought.contains("briefing"))
        assertEquals("SCHEDULE_EVENT", action.actionType)
        assertEquals("Meeting with Pepper Potts", action.title)
        assertEquals("Avengers Tower Ops", action.location)
    }

    @Test
    fun geminiResponse_smartHomeLightToggleIntentExtraction() {
        val sampleLightToggleResponse = """
            {
              "thought": "User command indicates toggling workshop illumination off.",
              "spokenResponse": "Disengaging workshop illumination sectors, Sir.",
              "actions": [
                {
                  "actionType": "SET_HOME_DEVICE",
                  "deviceId": "light_workshop",
                  "turnOn": false,
                  "numericValue": 0.0
                }
              ]
            }
        """.trimIndent()

        val parsed = JSONObject(sampleLightToggleResponse)
        val actionObj = parsed.getJSONArray("actions").getJSONObject(0)

        assertEquals("SET_HOME_DEVICE", actionObj.getString("actionType"))
        assertEquals("light_workshop", actionObj.getString("deviceId"))
        assertFalse(actionObj.getBoolean("turnOn"))
        assertEquals(0.0, actionObj.getDouble("numericValue"), 0.001)
    }

    @Test
    fun geminiResponse_markdownFenceStripping() {
        val rawWithFences = """
            ```json
            {
              "thought": "Diagnostics query",
              "spokenResponse": "All core systems nominal, Sir.",
              "actions": [
                { "actionType": "DIAGNOSTICS" }
              ]
            }
            ```
        """.trimIndent()

        val cleaned = rawWithFences.trim()
            .removePrefix("```json")
            .removePrefix("```JSON")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val json = JSONObject(cleaned)
        assertEquals("All core systems nominal, Sir.", json.getString("spokenResponse"))
        assertEquals("DIAGNOSTICS", json.getJSONArray("actions").getJSONObject(0).getString("actionType"))
    }
}
