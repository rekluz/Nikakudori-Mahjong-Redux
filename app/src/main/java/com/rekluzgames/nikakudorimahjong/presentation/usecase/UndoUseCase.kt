/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 * This code and its assets are the exclusive property of Rekluz Games.
 * Unauthorized copying, distribution, or commercial use is strictly prohibited.
 */

package com.rekluzgames.nikakudorimahjong.presentation.usecase

import com.rekluzgames.nikakudorimahjong.domain.model.GameUIState
import javax.inject.Inject

class UndoUseCase @Inject constructor() {

    /**
     * Apply undo logic for flat mode.
     * Restores previous board state from undo history.
     * Returns the same state if no undo history available.
     */
    fun undoFlat(state: GameUIState): GameUIState {
        if (state.undoHistory.isEmpty()) return state

        val previousBoard = state.undoHistory.last()
        return state.copy(
            board = previousBoard,
            undoHistory = state.undoHistory.dropLast(1),
            selectedTile = null,
            allAvailableHints = emptyList(),
            currentHintIndex = -1,
            lastMatchPath = null,
            lastMatchedPair = null
        )
    }

    /**
     * Apply undo logic for layered mode.
     * Restores previous tile state from undo history.
     * Returns the same state if no undo history available.
     */
    fun undoLayered(state: GameUIState): GameUIState {
        if (state.layeredUndoHistory.isEmpty()) return state

        val previous = state.layeredUndoHistory.last()
        return state.copy(
            layeredTiles = previous,
            layeredUndoHistory = state.layeredUndoHistory.dropLast(1),
            selectedLayeredTileId = null,
            layeredHints = emptyList(),
            currentLayeredHintIndex = -1
        )
    }
}