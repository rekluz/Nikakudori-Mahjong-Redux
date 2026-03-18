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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
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
import com.rekluzgames.nikakudorifresh.domain.model.GameState
import com.rekluzgames.nikakudorifresh.domain.model.GameMode
import com.rekluzgames.nikakudorifresh.presentation.viewmodel.GameViewModel

@Composable
fun SettingsOverlay(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(420.dp)
                .background(Color(0xEE1A1A1A), RoundedCornerShape(24.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("SETTINGS", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)

            Spacer(Modifier.height(24.dp))

            // --- GAME MODE SECTION ---
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("GAME MODE", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Regular Label
                    Text(
                        text = "REGULAR",
                        color = if (uiState.gameMode == GameMode.REGULAR) Color(0xFF00BFFF) else Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = if (uiState.gameMode == GameMode.REGULAR) FontWeight.Bold else FontWeight.Normal
                    )

                    // The Toggle Switch
                    Switch(
                        checked = uiState.gameMode == GameMode.GRAVITY,
                        onCheckedChange = { viewModel.toggleGameMode() },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Gravity Label
                    Text(
                        text = "GRAVITY",
                        color = if (uiState.gameMode == GameMode.GRAVITY) Color(0xFF00BFFF) else Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = if (uiState.gameMode == GameMode.GRAVITY) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Text(
                    text = if (uiState.gameMode == GameMode.REGULAR)
                        "Tiles stay in place when matched" else "Tiles drop down to fill empty spaces",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
            Spacer(Modifier.height(24.dp))

            // --- SOUND SECTION ---
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("SOUND EFFECTS", color = Color.White, modifier = Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Switch(
                    checked = uiState.isSoundEnabled,
                    onCheckedChange = { viewModel.updateSoundEnabled(it) }
                )
            }

            Spacer(Modifier.height(32.dp))

            // --- DONE BUTTON ---
            Box(modifier = Modifier.width(160.dp)) {
                MenuPillButton("DONE", color = Color(0xFF00BFFF)) {
                    viewModel.applySettingsAndResume()
                }
            }
        }
    }
}