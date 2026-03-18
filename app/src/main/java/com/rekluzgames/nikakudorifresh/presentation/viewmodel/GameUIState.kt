/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 * This code and its assets are the exclusive property of Rekluz Games.
 * Unauthorized copying, distribution, or commercial use is strictly prohibited.
 */

package com.rekluzgames.nikakudorifresh.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import com.rekluzgames.nikakudorifresh.domain.model.*
import com.rekluzgames.nikakudorifresh.presentation.ui.theme.MidnightBlue

data class GameUiState(
    val gameState: GameState = GameState.LOADING,
    val board: List<List<Tile>> = emptyList(),
    val difficulty: Difficulty = Difficulty.NORMAL,
    val gameMode: GameMode = GameMode.REGULAR,
    val selectedTile: Pair<Int, Int>? = null,
    val allAvailableHints: List<Pair<Pair<Int, Int>, Pair<Int, Int>>> = emptyList(),
    val currentHintIndex: Int = -1,
    val timeSeconds: Int = 0,
    val shufflesRemaining: Int = 5,
    val undoHistory: List<Pair<Pair<Int, Int>, Pair<Int, Int>>> = emptyList(),
    val themeColor: Color = MidnightBlue,
    val boardScale: Float = 1.0f,
    val isSoundEnabled: Boolean = true,
    val aboutStage: Int = 0,
    val clearedAboutTiles: Set<Int> = emptySet(),
    val version: String = "5.0.0"
) {
    val timeFormatted: String get() = "%02d:%02d".format(timeSeconds / 60, timeSeconds % 60)
    val canUndo: Boolean get() = undoHistory.isNotEmpty()
    val activeHint: Pair<Pair<Int, Int>, Pair<Int, Int>>?
        get() = if (currentHintIndex in allAvailableHints.indices) allAvailableHints[currentHintIndex] else null
}