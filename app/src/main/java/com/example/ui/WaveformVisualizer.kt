package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun WaveformVisualizer(
    isAnimating: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 20,
    primaryColor: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")

    val animValues = remember(isAnimating) {
        if (!isAnimating) {
            List(barCount) { 0.15f }
        } else {
            emptyList()
        }
    }

    val phase1 = infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p1"
    )

    val phase2 = infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p2"
    )

    val phase3 = infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p3"
    )

    Box(modifier = modifier.height(44.dp).fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val barWidth = (canvasWidth / (barCount * 1.8f)).coerceAtLeast(3f)
            val spacing = (canvasWidth - (barWidth * barCount)) / (barCount - 1).coerceAtLeast(1)

            for (i in 0 until barCount) {
                val scale = if (!isAnimating) {
                    0.15f
                } else {
                    when (i % 3) {
                        0 -> phase1.value * ((i % 5 + 1) / 5f + 0.5f).coerceIn(0.2f, 1f)
                        1 -> phase2.value * (((i + 2) % 5 + 1) / 5f + 0.4f).coerceIn(0.2f, 1f)
                        else -> phase3.value * (((i + 4) % 5 + 1) / 5f + 0.3f).coerceIn(0.2f, 1f)
                    }.coerceIn(0.15f, 1.0f)
                }

                val barHeight = (canvasHeight * scale).coerceAtLeast(6f)
                val x = i * (barWidth + spacing)
                val y = (canvasHeight - barHeight) / 2f

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor,
                            primaryColor.copy(alpha = 0.4f)
                        )
                    ),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }
        }
    }
}
