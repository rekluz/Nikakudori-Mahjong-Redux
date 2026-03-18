/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 * This code and its assets are the exclusive property of Rekluz Games.
 * Unauthorized copying, distribution, or commercial use is strictly prohibited.
 */

package com.rekluzgames.nikakudorifresh.presentation.ui.screen

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rekluzgames.nikakudorifresh.domain.model.GameState
import com.rekluzgames.nikakudorifresh.presentation.viewmodel.GameViewModel
import com.rekluzgames.nikakudorifresh.presentation.ui.component.*

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(uiState.themeColor)) {
        if (uiState.gameState == GameState.LOADING) {
            LoadingOverlay()
        } else {
            Row(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    BoardGrid(uiState) { r, c -> viewModel.handleTileClick(r, c) }
                }

                Column(
                    modifier = Modifier
                        .width(115.dp)
                        .fillMaxHeight()
                        .background(Color(0x99000000), RoundedCornerShape(16.dp))
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MenuPillButton("MENU") { viewModel.changeState(GameState.PAUSED) }
                    MenuPillButton("HINT") { viewModel.getHint() }
                    MenuPillButton("SHUFFLE (${uiState.shufflesRemaining})", enabled = uiState.shufflesRemaining > 0) { viewModel.shuffle() }
                    MenuPillButton("UNDO", enabled = uiState.canUndo) { viewModel.undo() }
                    MenuPillButton("SETTINGS") { viewModel.changeState(GameState.OPTIONS) }
                    MenuPillButton("BOARDS") { viewModel.changeState(GameState.BOARDS) }

                    Spacer(modifier = Modifier.weight(1f))
                    TimerDisplay(uiState.timeFormatted)
                }
            }
        }

        when (uiState.gameState) {
            GameState.PAUSED -> PauseOverlay(viewModel) { (context as? Activity)?.finish() }
            GameState.WON -> VictoryOverlay(uiState.timeFormatted) { viewModel.startNewGame(uiState.difficulty) }
            GameState.BOARDS -> BoardsOverlay(viewModel)
            GameState.OPTIONS -> SettingsOverlay(viewModel)
            GameState.ABOUT -> AboutScreen(viewModel)
            else -> {}
        }
    }
}