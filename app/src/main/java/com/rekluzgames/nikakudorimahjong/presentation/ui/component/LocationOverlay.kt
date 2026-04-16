/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 * This code and its assets are the exclusive property of Rekluz Games.
 * Unauthorized copying, distribution, or commercial use is strictly prohibited.
 */

package com.rekluzgames.nikakudorimahjong.presentation.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rekluzgames.nikakudorimahjong.domain.model.BackgroundLocations

@Composable
fun LocationOverlay(backgroundImageName: String) {
    val bgMetadata = BackgroundLocations.getLocationByResourceName(backgroundImageName)

    if (bgMetadata == null) return

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "locationAlpha"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            modifier = Modifier
                .alpha(alpha)
                .padding(24.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "📍 ${bgMetadata.locationName}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Right,
                modifier = Modifier.shadow(
                    elevation = 8.dp,
                    spotColor = Color.Black.copy(alpha = 0.8f),
                    ambientColor = Color.Black.copy(alpha = 0.8f)
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = bgMetadata.description,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Right,
                modifier = Modifier.shadow(
                    elevation = 6.dp,
                    spotColor = Color.Black.copy(alpha = 0.8f),
                    ambientColor = Color.Black.copy(alpha = 0.8f)
                )
            )
        }
    }
}