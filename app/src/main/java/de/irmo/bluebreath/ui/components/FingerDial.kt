package de.irmo.bluebreath.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun FingerDial(
    value: Int,
    onValueChange: (Int) -> Unit,
    minValue: Int = 1,
    maxValue: Int = 30,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val density = LocalDensity.current
    val dialSizeDp = 280.dp
    val dialSizePx = with(density) { dialSizeDp.toPx() }

    Box(
        modifier = modifier.size(dialSizeDp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(dialSizeDp)
                .pointerInput(minValue, maxValue) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val angle = atan2(
                            change.position.y - center.y,
                            change.position.x - center.x
                        )
                        val normalizedAngle = ((angle + PI / 2 + 2 * PI) % (2 * PI)).toFloat()
                        val range = maxValue - minValue
                        val newValue = (normalizedAngle / (2 * PI.toFloat()) * range + minValue)
                            .roundToInt()
                            .coerceIn(minValue, maxValue)
                        onValueChange(newValue)
                    }
                }
        ) {
            val radius = size.minDimension / 2f - 40f
            val center = Offset(size.width / 2f, size.height / 2f)
            val range = maxValue - minValue

            // Background track
            drawCircle(
                color = surfaceVariantColor,
                radius = radius,
                center = center,
                style = Stroke(width = 32f, cap = StrokeCap.Round)
            )

            // Active arc
            val sweepAngle = ((value - minValue).toFloat() / range) * 360f
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 32f, cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )

            // Tick marks
            for (i in minValue..maxValue) {
                val isMajor = i % 5 == 0 || i == 1
                val isSelected = i == value

                if (isMajor || isSelected) {
                    val tickAngle = ((i - minValue).toFloat() / range) * 360f - 90f
                    val tickRadians = Math.toRadians(tickAngle.toDouble())

                    val innerRadius = radius - 22f
                    val outerRadius = radius + 22f
                    val innerX = center.x + innerRadius * cos(tickRadians).toFloat()
                    val innerY = center.y + innerRadius * sin(tickRadians).toFloat()
                    val outerX = center.x + outerRadius * cos(tickRadians).toFloat()
                    val outerY = center.y + outerRadius * sin(tickRadians).toFloat()

                    drawLine(
                        color = if (isSelected) primaryColor else onSurfaceColor.copy(alpha = 0.3f),
                        start = Offset(innerX, innerY),
                        end = Offset(outerX, outerY),
                        strokeWidth = if (isSelected) 4f else 2f
                    )
                }
            }

            // Thumb indicator
            val thumbAngle = ((value - minValue).toFloat() / range) * 360f - 90f
            val thumbRadians = Math.toRadians(thumbAngle.toDouble())
            val thumbX = center.x + radius * cos(thumbRadians).toFloat()
            val thumbY = center.y + radius * sin(thumbRadians).toFloat()

            drawCircle(
                color = primaryColor,
                radius = 20f,
                center = Offset(thumbX, thumbY)
            )
            drawCircle(
                color = onPrimaryColor,
                radius = 8f,
                center = Offset(thumbX, thumbY)
            )
        }

        // Number labels as overlay Text composables
        val radius = dialSizePx / 2f - 40f
        val range = maxValue - minValue
        for (i in minValue..maxValue) {
            val isMajor = i % 5 == 0 || i == 1
            val isSelected = i == value
            if (isMajor || isSelected) {
                val tickAngle = ((i - minValue).toFloat() / range) * 360f - 90f
                val tickRadians = Math.toRadians(tickAngle.toDouble())
                val textRadius = radius + 38f
                val offsetX = (textRadius * cos(tickRadians)).toFloat()
                val offsetY = (textRadius * sin(tickRadians)).toFloat()

                Text(
                    text = i.toString(),
                    fontSize = if (isSelected) 14.sp else 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) primaryColor else onSurfaceColor.copy(alpha = 0.5f),
                    modifier = Modifier.offset {
                        IntOffset(offsetX.roundToInt(), offsetY.roundToInt())
                    }
                )
            }
        }

        // Center display
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "repetitions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
