/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 * This code and its assets are the exclusive property of Rekluz Games.
 * Unauthorized copying, distribution, or commercial use is strictly prohibited.
 */

package com.rekluzgames.nikakudorifresh.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // FIXED: Added missing import
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
import com.rekluzgames.nikakudorifresh.domain.model.Difficulty
import com.rekluzgames.nikakudorifresh.domain.model.GameState
import com.rekluzgames.nikakudorifresh.presentation.viewmodel.GameViewModel

@Composable
fun BoardsOverlay(viewModel: GameViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(450.dp)
                .background(Color(0xCC1A1A1A), RoundedCornerShape(24.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "SELECT BOARD",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // 2x2 Grid of Difficulty Buttons
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
    Box(
        modifier = modifier
            .height(80.dp)
            .background(Color(0xFF00BFFF).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = difficulty.label,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${difficulty.rows} x ${difficulty.cols}",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}