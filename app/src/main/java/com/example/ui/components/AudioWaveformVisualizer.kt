package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisNeonTeal
import com.example.ui.theme.JarvisOrange
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.RajdhaniFontFamily
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Display mode for the speech visualizer
 */
enum class WaveformStyle {
    BARS,           // Futuristic multi-band frequency bars
    SINE_WAVE,      // Continuous oscillating audio wave line with glow
    RADIAL_PULSE    // Concentric reactive sonic pulses (HUD radar style)
}

/**
 * High-tech reactive audio waveform and pulse visualization component.
 * Directly tracks audio input intensity (RMS dB level) from Android SpeechRecognizer.
 */
@Composable
fun AudioWaveformVisualizer(
    isListening: Boolean,
    rmsLevel: Float,
    modifier: Modifier = Modifier,
    height: Dp = 54.dp,
    style: WaveformStyle = WaveformStyle.BARS,
    primaryColor: Color = JarvisAmber,
    secondaryColor: Color = JarvisCyan,
    showTelemetryInfo: Boolean = false
) {
    // Smoothly animate the target RMS level to prevent jitter while maintaining ultra-responsive punch
    val animatedRms = remember { Animatable(0f) }

    LaunchedEffect(rmsLevel, isListening) {
        val target = if (isListening) rmsLevel.coerceIn(0f, 1f) else 0f
        animatedRms.animateTo(
            targetValue = target,
            animationSpec = tween(
                durationMillis = if (target > animatedRms.value) 45 else 120,
                easing = FastOutSlowInEasing
            )
        )
    }

    // Continuous subtle oscillation cycle for living pulse feel
    val infiniteTransition = rememberInfiniteTransition(label = "audio_wave_infinite")
    val phaseShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase_shift"
    )

    val currentIntensity = animatedRms.value

    Column(
        modifier = modifier
            .testTag("audio_waveform_visualizer")
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(10.dp))
                .background(JarvisSurfaceVariant.copy(alpha = 0.65f))
                .border(
                    width = 1.dp,
                    color = if (isListening) primaryColor.copy(alpha = 0.5f) else secondaryColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("waveform_canvas")
            ) {
                when (style) {
                    WaveformStyle.BARS -> {
                        drawReactiveFrequencyBars(
                            isListening = isListening,
                            intensity = currentIntensity,
                            phase = phaseShift,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor
                        )
                    }
                    WaveformStyle.SINE_WAVE -> {
                        drawReactiveSineWaveform(
                            isListening = isListening,
                            intensity = currentIntensity,
                            phase = phaseShift,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor
                        )
                    }
                    WaveformStyle.RADIAL_PULSE -> {
                        drawReactivePulseRings(
                            isListening = isListening,
                            intensity = currentIntensity,
                            phase = phaseShift,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor
                        )
                    }
                }
            }

            // Real-time HUD status badge overlaid in corner
            if (showTelemetryInfo) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = if (isListening) "INPUT // ACTIVE" else "MIC // STANDBY",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isListening) primaryColor else JarvisTextSecondary
                    )
                    Text(
                        text = "${(currentIntensity * 100).toInt()}% RMS",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isListening) primaryColor else JarvisTextSecondary
                    )
                }
            }
        }
    }
}

/**
 * Draws mirrored high-tech multi-frequency audio bars reacting dynamically to voice intensity.
 */
private fun DrawScope.drawReactiveFrequencyBars(
    isListening: Boolean,
    intensity: Float,
    phase: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    val barCount = 31
    val totalWidth = size.width
    val totalHeight = size.height
    val centerY = totalHeight / 2f

    val spacing = 3.dp.toPx()
    val availableBarWidth = (totalWidth - (spacing * (barCount - 1))) / barCount
    val barWidth = availableBarWidth.coerceAtLeast(2.dp.toPx())

    // Center-weighted distribution weights
    val halfCount = barCount / 2

    for (i in 0 until barCount) {
        val distFromCenter = kotlin.math.abs(i - halfCount).toFloat() / halfCount.toFloat()
        val centerWeight = 1f - (distFromCenter * 0.45f) // Center bars peak higher

        // Dynamic wave variation based on sinusoidal phase & audio intensity
        val waveComponent = sin(phase + i * 0.38f) * 0.25f
        val idleComponent = if (isListening) 0.12f else 0.04f

        val calculatedScale = (idleComponent + (intensity * 0.88f * centerWeight) + (waveComponent * intensity * 0.3f))
            .coerceIn(0.04f, 1f)

        val barHeight = (totalHeight * 0.85f * calculatedScale).coerceAtLeast(3.dp.toPx())
        val startX = i * (barWidth + spacing)
        val startY = centerY - (barHeight / 2f)

        // Gradient color: transition from cyan at base/idle to glowing amber/orange at high vocal intensity
        val barColor = if (!isListening) {
            secondaryColor.copy(alpha = 0.25f)
        } else if (intensity > 0.65f) {
            JarvisOrange
        } else if (intensity > 0.25f) {
            primaryColor
        } else {
            primaryColor.copy(alpha = 0.65f)
        }

        // Draw pill-shaped bar
        drawRoundRect(
            color = barColor,
            topLeft = Offset(startX, startY),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
        )

        // Ambient top/bottom glow caps on intense vocal spikes
        if (isListening && intensity > 0.35f) {
            drawCircle(
                color = Color.White.copy(alpha = 0.7f * intensity),
                radius = (barWidth * 0.7f).coerceAtMost(2.5.dp.toPx()),
                center = Offset(startX + (barWidth / 2f), startY)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.7f * intensity),
                radius = (barWidth * 0.7f).coerceAtMost(2.5.dp.toPx()),
                center = Offset(startX + (barWidth / 2f), startY + barHeight)
            )
        }
    }
}

/**
 * Draws smooth oscillating dual-frequency sine waveforms representing captured voice vibrations.
 */
private fun DrawScope.drawReactiveSineWaveform(
    isListening: Boolean,
    intensity: Float,
    phase: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    val totalWidth = size.width
    val centerY = size.height / 2f
    val maxAmplitude = (size.height / 2f) * 0.85f

    val amplitude = if (isListening) {
        maxAmplitude * (0.15f + intensity * 0.85f)
    } else {
        maxAmplitude * 0.08f
    }

    val primaryPath = Path()
    val secondaryPath = Path()

    val step = 4f
    var x = 0f

    var first = true
    while (x <= totalWidth) {
        val normalizedX = x / totalWidth
        // Windowing envelope: smoothly pin ends to zero at edges
        val envelope = sin(normalizedX * PI.toFloat())

        // Primary waveform
        val y1 = centerY + (sin(phase + normalizedX * 5f * PI.toFloat()) * amplitude * envelope)
        // Harmonics secondary wave
        val y2 = centerY + (sin(phase * 1.4f + normalizedX * 8f * PI.toFloat()) * (amplitude * 0.65f) * envelope)

        if (first) {
            primaryPath.moveTo(x, y1)
            secondaryPath.moveTo(x, y2)
            first = false
        } else {
            primaryPath.lineTo(x, y1)
            secondaryPath.lineTo(x, y2)
        }

        x += step
    }

    // Draw secondary harmonic line (cyan glow)
    drawPath(
        path = secondaryPath,
        color = secondaryColor.copy(alpha = if (isListening) 0.5f else 0.2f),
        style = Stroke(
            width = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    )

    // Draw primary reactive wave (amber/bright)
    val primaryStrokeWidth = (2f + (intensity * 2f)).dp.toPx()
    drawPath(
        path = primaryPath,
        color = if (isListening) primaryColor else secondaryColor.copy(alpha = 0.4f),
        style = Stroke(
            width = primaryStrokeWidth,
            cap = StrokeCap.Round
        )
    )

    // Center focal neon glow
    if (isListening && intensity > 0.2f) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primaryColor.copy(alpha = intensity * 0.5f), Color.Transparent),
                center = Offset(totalWidth / 2f, centerY),
                radius = 40.dp.toPx()
            ),
            radius = 40.dp.toPx(),
            center = Offset(totalWidth / 2f, centerY)
        )
    }
}

/**
 * Draws HUD radar/concentric reactive sound pulse rings.
 */
private fun DrawScope.drawReactivePulseRings(
    isListening: Boolean,
    intensity: Float,
    phase: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val maxRadius = (size.height / 2f) * 0.9f

    val ringCount = 4
    for (i in 0 until ringCount) {
        val ringOffset = (i.toFloat() / ringCount.toFloat())
        val currentPhase = (phase / (2 * PI).toFloat() + ringOffset) % 1f
        val animatedRadius = maxRadius * (0.2f + currentPhase * 0.8f)

        val alpha = if (isListening) {
            ((1f - currentPhase) * (0.3f + intensity * 0.7f)).coerceIn(0f, 1f)
        } else {
            ((1f - currentPhase) * 0.15f).coerceIn(0f, 1f)
        }

        val ringStrokeWidth = (1.5f + (intensity * 1.5f)).dp.toPx()
        drawCircle(
            color = if (isListening) primaryColor.copy(alpha = alpha) else secondaryColor.copy(alpha = alpha * 0.5f),
            radius = animatedRadius * (1f + intensity * 0.2f),
            center = center,
            style = Stroke(width = ringStrokeWidth)
        )
    }

    // Core central dot
    val coreRadius = (3f + (intensity * 5f)).dp.toPx()
    drawCircle(
        color = if (isListening) primaryColor else secondaryColor,
        radius = coreRadius,
        center = center
    )
}

/**
 * Compact reactive pulse mic button that scales and radiates rings directly in sync with rmsLevel.
 */
@Composable
fun ReactiveMicPulseButton(
    isListening: Boolean,
    rmsLevel: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    activeColor: Color = JarvisAmber,
    idleColor: Color = JarvisCyan
) {
    val animatedRms = remember { Animatable(0f) }
    LaunchedEffect(rmsLevel, isListening) {
        val target = if (isListening) rmsLevel.coerceIn(0f, 1f) else 0f
        animatedRms.animateTo(
            targetValue = target,
            animationSpec = tween(durationMillis = 60, easing = FastOutSlowInEasing)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_button_anim")
    val pulseRingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_ring"
    )

    val currentIntensity = animatedRms.value
    val dynamicButtonScale = 1f + (currentIntensity * 0.18f)

    Box(
        modifier = modifier
            .size(size)
            .testTag("reactive_mic_pulse_container"),
        contentAlignment = Alignment.Center
    ) {
        // Radiating pulse aura when listening
        if (isListening) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val baseRadius = (this.size.width / 2f) * 0.82f

                // Outer aura ring 1
                drawCircle(
                    color = activeColor.copy(alpha = (0.45f * (1f - (pulseRingScale - 1f) / 0.45f) * (0.5f + currentIntensity * 0.5f)).coerceIn(0f, 1f)),
                    radius = baseRadius * pulseRingScale * (1f + currentIntensity * 0.25f),
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Reactive shockwave ring proportional directly to mic volume
                if (currentIntensity > 0.15f) {
                    val shockWidth = (2.5f * currentIntensity).dp.toPx()
                    drawCircle(
                        color = JarvisOrange.copy(alpha = (currentIntensity * 0.6f).coerceIn(0f, 0.8f)),
                        radius = baseRadius * (1.1f + currentIntensity * 0.4f),
                        center = center,
                        style = Stroke(width = shockWidth)
                    )
                }
            }
        }

        // Inner circular button
        val borderWidth = (1.5f + (currentIntensity * 1.5f)).dp
        Box(
            modifier = Modifier
                .size(size * dynamicButtonScale)
                .clip(CircleShape)
                .background(
                    if (isListening) {
                        Brush.radialGradient(
                            colors = listOf(
                                if (currentIntensity > 0.5f) JarvisOrange else activeColor,
                                activeColor.copy(alpha = 0.85f)
                            )
                        )
                    } else {
                        Brush.radialGradient(
                            colors = listOf(idleColor.copy(alpha = 0.25f), idleColor.copy(alpha = 0.10f))
                        )
                    }
                )
                .border(
                    width = borderWidth,
                    color = if (isListening) Color.White else idleColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Replaced content inside caller or can pass icon
        }
    }
}
