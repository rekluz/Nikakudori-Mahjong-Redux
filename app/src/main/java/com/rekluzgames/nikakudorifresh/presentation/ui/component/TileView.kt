/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 * This code and its assets are the exclusive property of Rekluz Games.
 * Unauthorized copying, distribution, or commercial use is strictly prohibited.
 */

package com.rekluzgames.nikakudorifresh.presentation.ui.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.rekluzgames.nikakudorifresh.domain.model.Tile

@Composable
fun TileView(
    tile: Tile,
    isSelected: Boolean,
    isHinted: Boolean,
    width: Float,
    height: Float,
    xOffset: Float,
    yOffset: Float,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )

    AnimatedVisibility(
        visible = !tile.isRemoved,
        enter = fadeIn(),
        exit = fadeOut(tween(300)) + scaleOut(tween(300), targetScale = 0.8f)
    ) {
        Box(
            modifier = Modifier
                .size(width.dp, height.dp)
                .offset(xOffset.dp, yOffset.dp)
                .clickable { onClick() },
            contentAlignment = Alignment.TopStart // Stacking looks best aligned to top-start
        ) {
            val resId = remember(tile.imageName) {
                context.resources.getIdentifier(tile.imageName, "drawable", context.packageName)
            }

            if (resId != 0) {
                Image(
                    painter = painterResource(resId),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // SELECTION: Blue Tint
            if (isSelected) {
                Box(Modifier.fillMaxSize()
                    .background(Color(0xFF00BFFF).copy(alpha = 0.4f))
                    .border(2.dp, Color.Cyan, RectangleShape)
                )
            }

            // HINT: Yellow Glow
            if (isHinted) {
                Box(Modifier.fillMaxSize()
                    .background(Color(0xFFFFEB3B).copy(alpha = glowAlpha * 0.4f))
                    .border(2.dp, Color.Yellow.copy(alpha = glowAlpha), RectangleShape)
                )
            }
        }
    }
}