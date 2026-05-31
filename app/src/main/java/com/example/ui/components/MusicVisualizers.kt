package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlin.math.sin

/**
 * A beautiful backdrop brush that animates colors slowly in the background
 */
@Composable
fun GlowingGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    val primaryColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
    val secondaryColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
    val baseDark = Color(0xFF0C0E14)

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                val waveX = size.width / 2 + sin(animOffset) * 150f
                val waveY = size.height / 2 + sin(animOffset + 1.5f) * 150f
                
                drawRect(color = baseDark)
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryColor, Color.Transparent),
                        center = Offset(waveX, waveY),
                        radius = size.width * 0.95f
                    )
                )
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(secondaryColor, Color.Transparent),
                        center = Offset(size.width - waveX, size.height - waveY),
                        radius = size.width * 0.85f
                    )
                )
            },
        content = content
    )
}

/**
 * Animated vinyl disc deck that spins endlessly when the music is active
 */
@Composable
fun SpinningVinylCover(
    imageUrl: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 260.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_spinning")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    val currentRotation = if (isPlaying) rotationAngle else 0f

    Box(
        modifier = modifier
            .size(size)
            .shadow(24.dp, shape = CircleShape, clip = false)
            .rotate(currentRotation),
        contentAlignment = Alignment.Center
    ) {
        // Outer Vinyl grooves
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw Vinyl Outer Circle (dark carbon-slate)
            drawCircle(
                color = Color(0xFF14161C),
                radius = size.toPx() / 2
            )
            // Grooves
            for (r in 40 until (size.toPx() / 2).toInt() step 12) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.04f),
                    radius = r.toFloat(),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                )
            }
        }

        // Inner original album cover cut to circle with a hollow core
        Box(
            modifier = Modifier
                .fillMaxSize(0.65f)
                .clip(CircleShape)
                .background(Color.DarkGray)
        ) {
            AsyncImage(
                model = imageUrl ?: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=600",
                contentDescription = "Album Cover",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Center hub gold spindle pin hole
        Box(
            modifier = Modifier
                .fillMaxSize(0.12f)
                .clip(CircleShape)
                .background(Color(0xFF2C2F36))
                .shadow(2.dp, CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.35f)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(Color(0xFFE2B04E))
            )
        }
    }
}

/**
 * Canvas based animated music visualizer bar-waves
 */
@Composable
fun SoundwaveVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barsCount: Int = 16,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "soundwave")
    
    // Create animated parameters for bars
    val animValues = List(barsCount) { i ->
        infiniteTransition.animateFloat(
            initialValue = 0.15f,
            targetValue = 0.95f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 350 + (i * 45) % 250, 
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$i"
        )
    }

    Canvas(modifier = modifier) {
        val spacing = size.width / (barsCount * 2 - 1)
        val barWidth = spacing

        for (i in 0 until barsCount) {
            val progress = if (isPlaying) animValues[i].value else 0.12f
            val barHeight = size.height * progress
            val x = i * (barWidth + spacing)
            val y = (size.height - barHeight) / 2

            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}
