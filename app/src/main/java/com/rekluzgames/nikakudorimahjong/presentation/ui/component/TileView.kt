/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 * This code and its assets are the exclusive property of Rekluz Games.
 * Unauthorized copying, distribution, or commercial use is strictly prohibited.
 */

package com.rekluzgames.nikakudorimahjong.presentation.ui.component

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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.rekluzgames.nikakudorimahjong.domain.model.Tile
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun TileView(
    tile: Tile,
    isSelected: Boolean,
    isHinted: Boolean,
    isExploding: Boolean,
    width: Float,
    height: Float,
    xOffset: Float,
    yOffset: Float,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    // Pulsing glow for hint
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )

    // Shake + vanish animation
    val shakeX = remember { Animatable(0f) }
    val vanishAlpha = remember { Animatable(1f) }

    LaunchedEffect(isExploding) {
        if (isExploding) {
            coroutineScope {
                launch {
                    val shakeDistance = width * 0.12f
                    val shakeDuration = 55
                    repeat(4) {
                        shakeX.animateTo(
                            targetValue = shakeDistance,
                            animationSpec = tween(shakeDuration, easing = LinearEasing)
                        )
                        shakeX.animateTo(
                            targetValue = -shakeDistance,
                            animationSpec = tween(shakeDuration, easing = LinearEasing)
                        )
                    }
                    shakeX.animateTo(0f, animationSpec = tween(40))
                }
                launch {
                    kotlinx.coroutines.delay(180L)
                    vanishAlpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(150, easing = FastOutLinearInEasing)
                    )
                }
            }
        } else {
            shakeX.snapTo(0f)
            vanishAlpha.snapTo(1f)
        }
    }

    AnimatedVisibility(
        visible = !tile.isRemoved,
        enter = fadeIn(),
        exit = fadeOut(tween(250)) + scaleOut(tween(250), targetScale = 0.85f)
    ) {
        Box(
            modifier = Modifier
                .size(width.dp, height.dp)
                .offset(xOffset.dp, yOffset.dp)
                .graphicsLayer {
                    if (isExploding) {
                        translationX = shakeX.value
                        alpha = vanishAlpha.value
                    }
                }
                .clickable { onClick() },
            contentAlignment = Alignment.TopStart
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

            // SELECTION: Blue tint
            if (isSelected) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0xFF00BFFF).copy(alpha = 0.4f))
                        .border(2.dp, Color.Cyan, RectangleShape)
                )
            }

            // HINT: Yellow pulsing glow
            if (isHinted) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFFEB3B).copy(alpha = glowAlpha * 0.4f))
                        .border(2.dp, Color.Yellow.copy(alpha = glowAlpha), RectangleShape)
                )
            }
        }
    }
}