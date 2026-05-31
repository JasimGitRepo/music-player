package com.example.ui.lyrics

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.LyricLine

@Composable
fun LiveLyricsView(
    lyrics: List<LyricLine>,
    activeLineIndex: Int,
    onLineClick: (LyricLine) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Smooth scroll current line to the center-top area when playing
    LaunchedEffect(activeLineIndex) {
        if (activeLineIndex in lyrics.indices) {
            val scrollTarget = (activeLineIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(scrollTarget)
        }
    }

    if (lyrics.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No lyrics available for this track.\nSearch on YouTube for live synchronized synchronization.",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(24.dp)
            )
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 100.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(lyrics) { index, line ->
                val isActive = index == activeLineIndex
                
                // Animated color and size transitions
                val textColor by animateColorAsState(
                    targetValue = if (isActive) MaterialTheme.colorScheme.primary else Color.White,
                    animationSpec = tween(300),
                    label = "textColor"
                )
                
                val alphaValue by animateFloatAsState(
                    targetValue = if (isActive) 1f else 0.42f,
                    animationSpec = tween(300),
                    label = "alpha"
                )

                val scaleValue by animateFloatAsState(
                    targetValue = if (isActive) 1.1f else 0.95f,
                    animationSpec = tween(300),
                    label = "scale"
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLineClick(line) }
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = line.text,
                        color = textColor,
                        fontSize = (18 * scaleValue).sp,
                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        lineHeight = 26.sp,
                        modifier = Modifier.alpha(alphaValue)
                    )
                    
                    // Tiny timestamp tag shown on inactive lines for clean aesthetic look
                    if (isActive) {
                        Text(
                            text = formatTime(line.timeMs),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Light,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val minutes = (ms / 1000) / 60
    val seconds = (ms / 1000) % 60
    return String.format("%02d:%02d", minutes, seconds)
}
