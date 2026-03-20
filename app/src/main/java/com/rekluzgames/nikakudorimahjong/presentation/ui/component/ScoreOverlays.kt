/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 */

package com.rekluzgames.nikakudorimahjong.presentation.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rekluzgames.nikakudorimahjong.domain.model.Difficulty
import com.rekluzgames.nikakudorimahjong.domain.model.GameState
import com.rekluzgames.nikakudorimahjong.domain.model.Medal
import com.rekluzgames.nikakudorimahjong.presentation.viewmodel.GameViewModel

@Composable
fun ScoreEntryOverlay(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    OverlayContainer {
        Column(
            Modifier
                .width(450.dp)
                .background(Color(0xFF0D1A3A), RoundedCornerShape(24.dp))
                .border(1.dp, Color(0xFF00BFFF).copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("VICTORY!", color = Color.Yellow, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("Final Time: ${uiState.timeFormatted}", color = Color.White)

            val medals = uiState.earnedMedals
            if (medals.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    medals.forEach { medal ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(medal.icon, fontSize = 22.sp)
                            Text(
                                medal.label,
                                color = Color(0xFFFFD700),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                // Medal descriptions so the player knows what they earned
                Text(
                    medals.joinToString(" · ") { it.description },
                    color = Color.Gray,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(24.dp))
            Text("ENTER YOUR 3-DIGIT NAME", color = Color.Gray, fontSize = 12.sp)

            OutlinedTextField(
                value = uiState.playerName,
                onValueChange = { viewModel.updatePlayerName(it) },
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .width(200.dp)
                    .padding(vertical = 16.dp)
                    .focusRequester(focusRequester),
                maxLines = 1,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    viewModel.saveScoreAndShowBoard()
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFF00BFFF),
                    focusedBorderColor = Color.Yellow,
                    cursorColor = Color.Yellow
                )
            )

            // Character counter
            Text(
                "${uiState.playerName.length}/3",
                color = Color.Gray,
                fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            MenuPillButton("SAVE SCORE", color = Color(0xFF00BFFF)) {
                keyboardController?.hide()
                focusManager.clearFocus()
                viewModel.saveScoreAndShowBoard()
            }
        }
    }
}

@Composable
fun ScoreboardOverlay(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val tabs = Difficulty.entries.map { it.label }
    val activeTab = uiState.selectedScoreTab
    val scores = (uiState.highScores[activeTab] ?: emptyList()).take(3)
    val lastSaved = uiState.lastSavedScore

    var showClearConfirm by remember { mutableStateOf(false) }
    LaunchedEffect(activeTab) { showClearConfirm = false }

    val infiniteTransition = rememberInfiniteTransition(label = "flash")
    val flashAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flashAlpha"
    )

    OverlayContainer {
        Column(
            modifier = Modifier
                .width(450.dp)
                .background(Color(0xFF0D1A3A), RoundedCornerShape(24.dp))
                .border(1.dp, Color(0xFF00BFFF).copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "HALL OF FAME",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )

            // Difficulty tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF060E1E)),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tabs.forEach { tab ->
                    val isActive = tab == activeTab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isActive) Color(0xFF00BFFF).copy(alpha = 0.85f)
                                else Color.Transparent
                            )
                            .clickable { viewModel.selectScoreTab(tab) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = if (isActive) Color.Black else Color.Gray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Score rows
            if (scores.isEmpty()) {
                Text(
                    "No scores yet.\nFinish a $activeTab game to appear here!",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    scores.forEachIndexed { index, score ->
                        val isNewScore = lastSaved != null &&
                                score.name == lastSaved.name &&
                                score.time == lastSaved.time

                        val rankColor = when (index) {
                            0 -> Color(0xFFFFD700)
                            1 -> Color(0xFFC0C0C0)
                            2 -> Color(0xFFCD7F32)
                            else -> Color.Gray
                        }
                        val rowBackground = if (isNewScore) {
                            Color(0xFF00BFFF).copy(alpha = flashAlpha)
                        } else {
                            Color.White.copy(alpha = 0.05f)
                        }

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(rowBackground, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "#${index + 1}",
                                color = if (isNewScore) Color.White else rankColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                modifier = Modifier.width(28.dp)
                            )
                            Text(
                                score.name,
                                color = if (isNewScore) Color.White else Color.Yellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.width(44.dp)
                            )
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                score.medals.forEach { medal ->
                                    Text(
                                        text = medal.icon,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(end = 2.dp)
                                    )
                                }
                            }
                            Text(
                                score.timeFormatted,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Medal legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Medal.entries.forEach { medal ->
                    Text(
                        "${medal.icon} ${medal.label}",
                        color = Color.Gray,
                        fontSize = 8.sp,
                        modifier = Modifier.padding(horizontal = 3.dp)
                    )
                }
            }

            // Bottom buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (showClearConfirm) {
                        MenuPillButton("CONFIRM CLEAR", color = Color(0xFFFF4444)) {
                            viewModel.clearScores(activeTab)
                            showClearConfirm = false
                        }
                    } else {
                        MenuPillButton(
                            text = "CLEAR $activeTab",
                            color = Color(0xFF663333),
                            enabled = scores.isNotEmpty()
                        ) { showClearConfirm = true }
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    MenuPillButton("CLOSE") {
                        showClearConfirm = false
                        viewModel.clearLastSavedScore()
                        viewModel.changeState(GameState.PLAYING)
                    }
                }
            }
        }
    }
}