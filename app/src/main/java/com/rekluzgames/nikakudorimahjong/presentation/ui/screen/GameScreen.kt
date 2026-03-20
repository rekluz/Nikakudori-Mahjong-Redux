/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 */

package com.rekluzgames.nikakudorimahjong.presentation.ui.screen

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.rekluzgames.nikakudorimahjong.domain.model.GameState
import com.rekluzgames.nikakudorimahjong.presentation.viewmodel.GameViewModel
import com.rekluzgames.nikakudorimahjong.presentation.ui.component.*

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val screenPadding = if (!uiState.isFullScreen) {
        Modifier.windowInsetsPadding(WindowInsets.systemBars)
    } else {
        Modifier.padding(0.dp)
    }

    Box(modifier = Modifier.fillMaxSize().background(uiState.themeColor).then(screenPadding)) {

        if (uiState.gameState == GameState.LOADING) {
            LoadingOverlay()
        } else {

            // Watermark icon centred behind the board
            val iconId = context.resources.getIdentifier(
                "nikakudorimahjong", "drawable", context.packageName
            )
            if (iconId != 0) {
                Image(
                    painter = painterResource(iconId),
                    contentDescription = null,
                    modifier = Modifier
                        .size(280.dp)
                        .align(Alignment.Center),
                    alpha = 0.40f
                )
            }

            Row(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    BoardGrid(uiState) { r, c -> viewModel.handleTileClick(r, c) }
                }

                Column(
                    modifier = Modifier
                        .width(125.dp)
                        .fillMaxHeight()
                        .background(Color(0x99000000), RoundedCornerShape(16.dp))
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    // Tighter spacing so all buttons + timer fit without clipping
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    MenuPillButton(
                        text = "MENU",
                        color = Color(0xFF2A2A2A)
                    ) { viewModel.changeState(GameState.PAUSED) }

                    val hintText = if (uiState.canFinish) "FINISH" else "HINT"
                    val hintColor = if (uiState.canFinish) Color(0xFFCC2200) else Color(0xFF00BFFF)

                    MenuPillButton(text = hintText, color = hintColor) {
                        viewModel.getHint()
                    }

                    MenuPillButton(
                        text = "SHUFFLE (${uiState.shufflesRemaining})",
                        enabled = uiState.shufflesRemaining > 0,
                        color = Color(0xFF708090)
                    ) { viewModel.shuffle() }

                    MenuPillButton(
                        text = "UNDO",
                        enabled = uiState.canUndo,
                        color = Color(0xFF708090)
                    ) { viewModel.undo() }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.1f))
                    )

                    MenuPillButton(
                        text = "SETTINGS",
                        color = Color(0xFF2A2A2A)
                    ) { viewModel.changeState(GameState.OPTIONS) }

                    MenuPillButton(
                        text = "BOARDS",
                        color = Color(0xFF2A2A2A)
                    ) { viewModel.changeState(GameState.BOARDS) }

                    Spacer(modifier = Modifier.weight(1f))

                    val remaining = uiState.remainingTilesCount
                    if (remaining > 0) {
                        androidx.compose.material3.Text(
                            text = "$remaining tiles",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = androidx.compose.ui.unit.TextUnit(
                                9f,
                                androidx.compose.ui.unit.TextUnitType.Sp
                            ),
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }

                    TimerDisplay(uiState.timeFormatted, uiState.timeSeconds)
                }
            }
        }

        when (uiState.gameState) {
            GameState.PAUSED      -> PauseOverlay(viewModel) { (context as? Activity)?.finish() }
            GameState.BOARDS      -> BoardsOverlay(viewModel)
            GameState.OPTIONS     -> SettingsOverlay(viewModel)
            GameState.ABOUT       -> AboutScreen(viewModel)
            GameState.SCORE_ENTRY -> ScoreEntryOverlay(viewModel)
            GameState.SCORE       -> ScoreboardOverlay(viewModel)
            GameState.NO_MOVES    -> StalemateOverlay(viewModel)
            else -> {}
        }
    }
}