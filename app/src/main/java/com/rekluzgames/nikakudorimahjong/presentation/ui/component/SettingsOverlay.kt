/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 */

package com.rekluzgames.nikakudorimahjong.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rekluzgames.nikakudorimahjong.domain.model.GameMode
import com.rekluzgames.nikakudorimahjong.domain.model.GameState
import com.rekluzgames.nikakudorimahjong.presentation.viewmodel.GameViewModel

@Composable
fun SettingsOverlay(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    OverlayContainer {
        OverlayCard {
            OverlayTitle("SETTINGS")

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SettingGridButton(
                        title = "MODE",
                        status = if (uiState.gameMode == GameMode.REGULAR) "REGULAR" else "GRAVITY",
                        isActive = true,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.toggleGameMode() }

                    SettingGridButton(
                        title = "SOUND",
                        status = if (uiState.isSoundEnabled) "ON" else "OFF",
                        isActive = uiState.isSoundEnabled,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.updateSoundEnabled(!uiState.isSoundEnabled) }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SettingGridButton(
                        title = "SCREEN",
                        status = if (uiState.isFullScreen) "FULL" else "NORMAL",
                        isActive = uiState.isFullScreen,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.toggleFullScreen() }

                    SettingGridButton(
                        title = "SCORES",
                        status = "VIEW",
                        isActive = true,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.changeState(GameState.SCORE) }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.width(140.dp)) {
                MenuPillButton("DONE", color = Color(0xFF00BFFF)) {
                    viewModel.applySettingsAndResume()
                }
            }
        }
    }
}

@Composable
fun SettingGridButton(
    title: String,
    status: String,
    isActive: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    // Active settings glow cyan, inactive are muted dark
    val bgColor = if (isActive) Color(0xFF1A3A5C) else Color(0xFF1A1A2A)
    val borderColor = if (isActive) Color(0xFF00BFFF).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f)
    val statusColor = if (isActive) Color(0xFF00BFFF) else Color.Gray

    Box(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = status,
                color = statusColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}