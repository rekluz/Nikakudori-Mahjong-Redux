/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 * This code and its assets are the exclusive property of Rekluz Games.
 * Unauthorized copying, distribution, or commercial use is strictly prohibited.
 */

package com.rekluzgames.nikakudorimahjong.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rekluzgames.nikakudorimahjong.domain.model.Difficulty
import com.rekluzgames.nikakudorimahjong.domain.model.GameState
import com.rekluzgames.nikakudorimahjong.presentation.viewmodel.GameViewModel

@Composable
fun BoardsOverlay(viewModel: GameViewModel) {
    OverlayContainer {
        OverlayCard {
            OverlayTitle("SELECT BOARD")

            val chunks = Difficulty.entries.chunked(2)
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                chunks.forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        rowItems.forEach { diff ->
                            BoardRectButton(diff, Modifier.weight(1f)) {
                                viewModel.startNewGame(diff)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.width(140.dp)) {
                MenuPillButton("CANCEL", color = Color.Gray) {
                    viewModel.changeState(GameState.PLAYING)
                }
            }
        }
    }
}

@Composable
fun BoardRectButton(difficulty: Difficulty, modifier: Modifier, onClick: () -> Unit) {
    // Each difficulty gets a distinct colour to aid recognition
    val buttonColor = when (difficulty) {
        Difficulty.EASY    -> Color(0xFF1A5C2A) // Green — approachable
        Difficulty.NORMAL  -> Color(0xFF1A3A5C) // Blue — standard
        Difficulty.HARD    -> Color(0xFF5C3A1A) // Amber — challenging
        Difficulty.EXTREME -> Color(0xFF5C1A1A) // Red — expert
    }
    val accentColor = when (difficulty) {
        Difficulty.EASY    -> Color(0xFF44BB66)
        Difficulty.NORMAL  -> Color(0xFF00BFFF)
        Difficulty.HARD    -> Color(0xFFFFB300)
        Difficulty.EXTREME -> Color(0xFFFF4444)
    }
    val descriptors = when (difficulty) {
        Difficulty.EASY    -> "Beginner"
        Difficulty.NORMAL  -> "Standard"
        Difficulty.HARD    -> "Challenging"
        Difficulty.EXTREME -> "Expert"
    }

    Box(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(buttonColor)
            .border(1.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = difficulty.label,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = descriptors,
                color = accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${difficulty.rows} × ${difficulty.cols}",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp
            )
        }
    }
}