/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 * This code and its assets are the exclusive property of Rekluz Games.
 * Unauthorized copying, distribution, or commercial use is strictly prohibited.
 */

package com.rekluzgames.nikakudorimahjong.presentation.usecase

import com.rekluzgames.nikakudorimahjong.domain.engine.LayeredGameEngine
import com.rekluzgames.nikakudorimahjong.domain.model.GameUIState
import com.rekluzgames.nikakudorimahjong.domain.model.Tile
import com.rekluzgames.nikakudorimahjong.presentation.ui.component.TileInteractionHandler
import javax.inject.Inject

/**
 * Result of a tile interaction in flat mode.
 * The ViewModel decides what to do with these results.
 */
data class FlatInteractionResult(
    val newState: GameUIState,
    val soundToPlay: String? = null,
    val hapticFeedback: String? = null,  // "select", "match", or "error"
    val matchPath: List<Pair<Int, Int>>? = null,
    val matchedPair: Pair<Pair<Int, Int>, Pair<Int, Int>>? = null,
    val matchedBoard: List<List<Tile>>? = null
)

/**
 * Result of a tile interaction in layered mode.
 * The ViewModel decides what to do with these results.
 */
data class LayeredInteractionResult(
    val newState: GameUIState,
    val soundToPlay: String? = null,
    val hapticFeedback: String? = null,  // "select", "match", or "error"
    val shouldCheckWin: Boolean = false,
    val shouldCheckStalemate: Boolean = false
)

class InteractionCoordinator @Inject constructor(
    private val tileHandler: TileInteractionHandler,
    private val layeredEngine: LayeredGameEngine
) {

    /**
     * Handle a flat mode tile click.
     * Returns all information needed for the ViewModel to orchestrate effects.
     */
    fun handleFlatTileClick(
        r: Int,
        c: Int,
        state: GameUIState
    ): FlatInteractionResult {
        // Step 1: Get handler result (contains state update + match info)
        val result = tileHandler.handleClick(state, r, c)

        // Step 2: Determine haptic feedback
        val hapticFeedback = when (result.playSound) {
            "tile_error" -> "error"
            "tile_match" -> "match"
            else -> "select"  // For any other case
        }

        // Step 3: Extract match line info if available
        val (matchPath, matchedPair) = if (result.matchPath != null && result.matchedPair != null) {
            result.matchPath to result.matchedPair
        } else if (result.matchPath != null) {
            val first = result.matchPath.firstOrNull()
            val last = result.matchPath.lastOrNull()
            if (first != null && last != null) {
                result.matchPath to (first to last)
            } else {
                null to null
            }
        } else {
            null to null
        }

        return FlatInteractionResult(
            newState = result.newState,
            soundToPlay = result.playSound,
            hapticFeedback = hapticFeedback,
            matchPath = matchPath,
            matchedPair = matchedPair,
            matchedBoard = result.matchedBoard
        )
    }

    /**
     * Handle a layered mode tile click.
     * Returns all information needed for the ViewModel to orchestrate effects.
     */
    fun handleLayeredTileClick(
        id: Int,
        state: GameUIState
    ): LayeredInteractionResult {
        // Step 1: Find the tapped tile
        val tapped = state.layeredTiles.firstOrNull { it.id == id && !it.isRemoved }
            ?: return LayeredInteractionResult(
                newState = state,
                soundToPlay = null,
                hapticFeedback = null
            )

        // Step 2: Check if tile is free
        if (!layeredEngine.isFree(tapped, state.layeredTiles)) {
            return LayeredInteractionResult(
                newState = state,
                soundToPlay = "tile_error",
                hapticFeedback = "error"
            )
        }

        // Step 3: Handle selection/match logic
        return when (state.selectedLayeredTileId) {
            id -> {
                // Deselect
                LayeredInteractionResult(
                    newState = state.copy(selectedLayeredTileId = null),
                    soundToPlay = null,
                    hapticFeedback = null
                )
            }
            null -> {
                // Select
                LayeredInteractionResult(
                    newState = state.copy(selectedLayeredTileId = id),
                    soundToPlay = null,
                    hapticFeedback = "select"
                )
            }
            else -> {
                // Attempt match
                val newTiles = layeredEngine.attemptMatch(
                    state.selectedLayeredTileId,
                    id,
                    state.layeredTiles
                )

                if (newTiles != null) {
                    // Match succeeded
                    val snapshot = state.layeredTiles
                    LayeredInteractionResult(
                        newState = state.copy(
                            layeredTiles = newTiles,
                            selectedLayeredTileId = null,
                            layeredHints = emptyList(),
                            currentLayeredHintIndex = -1,
                            layeredUndoHistory = state.layeredUndoHistory + listOf(snapshot)
                        ),
                        soundToPlay = "tile_match",
                        hapticFeedback = "match",
                        shouldCheckWin = true,
                        shouldCheckStalemate = true
                    )
                } else {
                    // Match failed, select this tile instead
                    LayeredInteractionResult(
                        newState = state.copy(selectedLayeredTileId = id),
                        soundToPlay = null,
                        hapticFeedback = "select"
                    )
                }
            }
        }
    }
}