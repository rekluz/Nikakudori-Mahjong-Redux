/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 * This code and its assets are the exclusive property of Rekluz Games.
 * Unauthorized copying, distribution, or commercial use is strictly prohibited.
 */

package com.rekluzgames.nikakudorifresh.presentation.ui.component

import androidx.compose.foundation.background
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
import com.rekluzgames.nikakudorifresh.domain.model.GameState
import com.rekluzgames.nikakudorifresh.presentation.viewmodel.GameViewModel

@Composable
fun PauseOverlay(viewModel: GameViewModel, onExit: () -> Unit) {
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
                "MENU",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // 2x2 Grid of Large Buttons
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LargeMenuButton("NEW GAME", Modifier.weight(1f)) {
                        viewModel.changeState(GameState.BOARDS)
                    }
                    LargeMenuButton("RETRY", Modifier.weight(1f)) {
                        viewModel.startNewGame(viewModel.uiState.value.difficulty)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LargeMenuButton("RESUME", Modifier.weight(1f)) {
                        viewModel.changeState(GameState.PLAYING)
                    }
                    LargeMenuButton("ABOUT", Modifier.weight(1f)) {
                        viewModel.changeState(GameState.ABOUT)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.width(140.dp)) {
                MenuPillButton("EXIT", color = Color(0xFFFF4444)) { onExit() }
            }
        }
    }
}

@Composable
fun LargeMenuButton(text: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(80.dp)
            .background(Color(0xFF00BFFF).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun VictoryOverlay(time: String, onRestart: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .background(Color(0xDD1A1A1A), RoundedCornerShape(24.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("VICTORY!", color = Color.Yellow, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("Final Time: $time", color = Color.White)
            Spacer(Modifier.height(24.dp))
            MenuPillButton("PLAY AGAIN", color = Color(0xFF00BFFF)) { onRestart() }
        }
    }
}