/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 * This code and its assets are the exclusive property of Rekluz Games.
 * Unauthorized copying, distribution, or commercial use is strictly prohibited.
 */

package com.rekluzgames.nikakudorimahjong.presentation.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TimerDisplay(time: String, timeSeconds: Int) {
    // White under 2 min, amber 2–5 min, red over 5 min
    val targetColor = when {
        timeSeconds < 120  -> Color.White
        timeSeconds < 300  -> Color(0xFFFFB300) // Amber
        else               -> Color(0xFFFF4444) // Red
    }
    val timerColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(1000),
        label = "timerColor"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(CircleShape)
            .background(Color(0xFF121212))
            .border(1.dp, timerColor.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("TIME ", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(time, color = timerColor, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}