package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisNeonTeal
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorCore(
    modifier: Modifier = Modifier,
    size: Dp = 190.dp,
    isListening: Boolean = false,
    isSpeaking: Boolean = false,
    rmsLevel: Float = 0f,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "arc_reactor_anim")

    // Slow continuous rotation
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Reverse outer rotation
    val reverseRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "reverse_rotation"
    )

    // Pulse scale
    val pulseRatio by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isListening || isSpeaking) 700 else 2200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val dynamicPulse = if (isListening) (1f + rmsLevel * 0.35f) else pulseRatio

    Box(
        modifier = modifier
            .size(size)
            .testTag("arc_reactor_core")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val baseRadius = (size.toPx() / 2f) * 0.85f * dynamicPulse

            // 1. Ambient Glow Radial Gradient
            val glowBrush = Brush.radialGradient(
                colors = listOf(
                    if (isListening) JarvisAmber.copy(alpha = 0.45f)
                    else if (isSpeaking) JarvisNeonTeal.copy(alpha = 0.40f)
                    else JarvisCyan.copy(alpha = 0.25f),
                    JarvisCyan.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = center,
                radius = baseRadius * 1.3f
            )
            drawCircle(brush = glowBrush, radius = baseRadius * 1.25f, center = center)

            // 2. Outer Segmented Ring
            rotate(reverseRotation, pivot = center) {
                val segments = 12
                val sweep = 360f / segments
                for (i in 0 until segments) {
                    val startAngle = i * sweep
                    drawArc(
                        color = if (i % 3 == 0) JarvisAmber else JarvisCyan.copy(alpha = 0.65f),
                        startAngle = startAngle + 4f,
                        sweepAngle = sweep - 8f,
                        useCenter = false,
                        topLeft = Offset(center.x - baseRadius, center.y - baseRadius),
                        size = androidx.compose.ui.geometry.Size(baseRadius * 2, baseRadius * 2),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }

            // 3. Middle Rotating Tech Ring with spokes
            rotate(rotationAngle, pivot = center) {
                val middleRadius = baseRadius * 0.72f
                drawCircle(
                    color = JarvisCyan.copy(alpha = 0.3f),
                    radius = middleRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                val spokes = 8
                for (s in 0 until spokes) {
                    val angle = Math.toRadians((s * 360f / spokes).toDouble())
                    val innerPoint = Offset(
                        (center.x + cos(angle) * (baseRadius * 0.45f)).toFloat(),
                        (center.y + sin(angle) * (baseRadius * 0.45f)).toFloat()
                    )
                    val outerPoint = Offset(
                        (center.x + cos(angle) * middleRadius).toFloat(),
                        (center.y + sin(angle) * middleRadius).toFloat()
                    )
                    drawLine(
                        color = if (s % 2 == 0) JarvisCyan else JarvisAmber.copy(alpha = 0.8f),
                        start = innerPoint,
                        end = outerPoint,
                        strokeWidth = 2.5.dp.toPx()
                    )
                }
            }

            // 4. Inner Arc Reactor Energy Core
            val innerRadius = baseRadius * 0.42f
            val coreBrush = Brush.radialGradient(
                colors = listOf(
                    Color.White,
                    if (isListening) JarvisAmber else JarvisCyan,
                    if (isListening) Color(0xFFFF6D00) else Color(0xFF005A70)
                ),
                center = center,
                radius = innerRadius
            )
            drawCircle(brush = coreBrush, radius = innerRadius, center = center)

            // Inner triangular energy lens
            rotate(-rotationAngle * 1.5f, pivot = center) {
                val triRadius = innerRadius * 0.65f
                val path = Path().apply {
                    for (i in 0 until 3) {
                        val angle = Math.toRadians((i * 120.0 - 90.0))
                        val x = (center.x + cos(angle) * triRadius).toFloat()
                        val y = (center.y + sin(angle) * triRadius).toFloat()
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.9f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}
