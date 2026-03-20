/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 */

package com.rekluzgames.nikakudorimahjong.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import com.rekluzgames.nikakudorimahjong.domain.model.*
import com.rekluzgames.nikakudorimahjong.presentation.ui.theme.MidnightBlue

data class GameUIState(
    val gameState: GameState = GameState.LOADING,
    val board: List<List<Tile>> = emptyList(),
    val originalBoard: List<List<Tile>> = emptyList(),
    val difficulty: Difficulty = Difficulty.NORMAL,
    val gameMode: GameMode = GameMode.REGULAR,
    val selectedTile: Pair<Int, Int>? = null,
    val allAvailableHints: List<Pair<Pair<Int, Int>, Pair<Int, Int>>> = emptyList(),
    val currentHintIndex: Int = -1,
    val timeSeconds: Int = 0,
    val shufflesRemaining: Int = 5,
    val undoHistory: List<List<List<Tile>>> = emptyList(),
    val themeColor: Color = MidnightBlue,
    val boardScale: Float = 1.0f,
    val isSoundEnabled: Boolean = true,
    val isFullScreen: Boolean = false,
    val aboutStage: Int = 0,
    val clearedAboutTiles: Set<Int> = emptySet(),
    // Empty by default — set at runtime from BuildConfig.VERSION_NAME
    val version: String = "",
    val playerName: String = "",
    val highScores: Map<String, List<HighScore>> = emptyMap(),
    val selectedScoreTab: String = Difficulty.NORMAL.label,
    val lastMatchPath: List<Pair<Int, Int>>? = null,
    val lastMatchedPair: Pair<Pair<Int, Int>, Pair<Int, Int>>? = null,
    val usedHint: Boolean = false,
    val usedShuffle: Boolean = false,
    val lastSavedScore: HighScore? = null
) {
    val timeFormatted: String get() = "%02d:%02d".format(timeSeconds / 60, timeSeconds % 60)
    val canUndo: Boolean get() = undoHistory.isNotEmpty()

    val remainingTilesCount: Int get() = board.flatten().count { !it.isRemoved }

    val canFinish: Boolean get() = remainingTilesCount in 1..12

    val activeHint: Pair<Pair<Int, Int>, Pair<Int, Int>>?
        get() = if (currentHintIndex in allAvailableHints.indices) allAvailableHints[currentHintIndex] else null

    val earnedMedals: List<Medal>
        get() = buildList {
            if (!usedHint) add(Medal.SNIPER)
            if (timeSeconds < 120) add(Medal.FLASH)
            if (!usedShuffle) add(Medal.STRATEGIST)
        }
}

data class HighScore(
    val name: String,
    val time: Int,
    val difficulty: String,
    val medals: List<Medal> = emptyList()
) {
    val timeFormatted: String get() = "%02d:%02d".format(time / 60, time % 60)

    fun serialise(): String {
        val base = "$name|$time|$difficulty"
        return if (medals.isEmpty()) base else "$base|${medals.joinToString(",") { it.name }}"
    }

    companion object {
        fun deserialise(raw: String): HighScore? = try {
            val parts = raw.split("|")
            val medals = if (parts.size >= 4 && parts[3].isNotBlank()) {
                parts[3].split(",").mapNotNull {
                    try { Medal.valueOf(it.trim()) } catch (e: Exception) { null }
                }
            } else emptyList()
            HighScore(parts[0], parts[1].toInt(), parts[2], medals)
        } catch (e: Exception) { null }
    }
}