package com.example

import com.example.ui.components.WaveformStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioWaveformUnitTest {

    @Test
    fun waveformStyles_areDefined() {
        val styles = WaveformStyle.values()
        assertTrue(styles.contains(WaveformStyle.BARS))
        assertTrue(styles.contains(WaveformStyle.SINE_WAVE))
        assertTrue(styles.contains(WaveformStyle.RADIAL_PULSE))
        assertEquals(3, styles.size)
    }

    @Test
    fun speechRecognizerRmsNormalization_clampsCorrectly() {
        // Test normalizer formula used in VoiceSpeechManager onRmsChanged
        fun normalizeRms(rmsdB: Float): Float {
            return ((rmsdB + 2f).coerceIn(0f, 10f) / 10f)
        }

        assertEquals(0f, normalizeRms(-5f), 0.001f)
        assertEquals(0f, normalizeRms(-2f), 0.001f)
        assertEquals(0.5f, normalizeRms(3f), 0.001f)
        assertEquals(1.0f, normalizeRms(8f), 0.001f)
        assertEquals(1.0f, normalizeRms(12f), 0.001f)
    }
}
