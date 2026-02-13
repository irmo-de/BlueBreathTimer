package de.irmo.bluebreath.ui.components

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import de.irmo.bluebreath.model.BreathingPhase

@Composable
fun BreathingCircle(
    phase: BreathingPhase,
    progress: Float,
    timeRemaining: Int,
    currentRep: Int,
    totalReps: Int,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val targetScale = when (phase) {
        BreathingPhase.INHALE -> 0.5f + 0.5f * progress
        BreathingPhase.HOLD -> 1f
        BreathingPhase.EXHALE -> 1f - 0.5f * progress
        BreathingPhase.IDLE -> 0.5f
        BreathingPhase.COMPLETE -> 0.7f
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 100, easing = LinearEasing),
        label = "breathScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(280.dp)) {
            val maxRadius = size.minDimension / 2f - 20f
            val radius = maxRadius * animatedScale
            val circleCenter = center

            // Outer glow
            if (phase != BreathingPhase.IDLE && phase != BreathingPhase.COMPLETE) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = glowAlpha * 0.3f),
                            Color.Transparent
                        ),
                        center = circleCenter,
                        radius = radius * 1.4f
                    ),
                    radius = radius * 1.4f,
                    center = circleCenter
                )
            }

            // Main circle fill
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryContainerColor.copy(alpha = 0.8f),
                        primaryColor.copy(alpha = 0.4f)
                    ),
                    center = circleCenter,
                    radius = radius
                ),
                radius = radius,
                center = circleCenter
            )

            // Circle border
            drawCircle(
                color = primaryColor,
                radius = radius,
                center = circleCenter,
                style = Stroke(width = 4f)
            )

            // Progress arc
            if (phase != BreathingPhase.IDLE && phase != BreathingPhase.COMPLETE) {
                drawArc(
                    color = tertiaryColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = 6f),
                    topLeft = Offset(
                        circleCenter.x - radius,
                        circleCenter.y - radius
                    ),
                    size = Size(radius * 2, radius * 2)
                )
            }
        }

        // Center text overlay
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = when (phase) {
                    BreathingPhase.IDLE -> "Ready"
                    BreathingPhase.COMPLETE -> "Done!"
                    else -> phase.displayName
                },
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (phase != BreathingPhase.IDLE && phase != BreathingPhase.COMPLETE) {
                Text(
                    text = "${timeRemaining}s",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Cycle $currentRep / $totalReps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
