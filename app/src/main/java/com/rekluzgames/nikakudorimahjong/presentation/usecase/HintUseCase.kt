/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 * This code and its assets are the exclusive property of Rekluz Games.
 * Unauthorized copying, distribution, or commercial use is strictly prohibited.
 */

package com.rekluzgames.nikakudorimahjong.presentation.usecase

import com.rekluzgames.nikakudorimahjong.domain.engine.LayeredGameEngine
import com.rekluzgames.nikakudorimahjong.domain.model.GameUIState
import com.rekluzgames.nikakudorimahjong.domain.rules.HintFinder
import com.rekluzgames.nikakudorimahjong.domain.rules.LayeredHintFinder
import javax.inject.Inject

class HintUseCase @Inject constructor(
    private val layeredEngine: LayeredGameEngine
) {

    /**
     * Apply hint logic for flat mode.
     * Returns updated state with hints populated or NO_MOVES state.
     *
     * Does NOT handle autoComplete() — that stays in ViewModel.
     */
    fun applyFlatHint(state: GameUIState): GameUIState {
        // Early returns: game state checks
        if (state.gameState != com.rekluzgames.nikakudorimahjong.domain.model.GameState.PLAYING) {
            return state
        }

        // Mark hint as used
        var newState = state.copy(usedHint = true)

        // If no hints cached, find them
        if (newState.allAvailableHints.isEmpty()) {
            val hints = HintFinder.findAllMatches(state.board)
            if (hints.isNotEmpty()) {
                newState = newState.copy(
                    allAvailableHints = hints,
                    currentHintIndex = 0
                )
            } else {
                // No hints available → transition to NO_MOVES
                newState = newState.copy(
                    gameState = com.rekluzgames.nikakudorimahjong.domain.model.GameState.NO_MOVES
                )
            }
        } else {
            // Cycle to next hint
            newState = newState.copy(
                currentHintIndex = (newState.currentHintIndex + 1) % newState.allAvailableHints.size
            )
        }

        return newState
    }

    /**
     * Apply hint logic for layered mode.
     * Returns updated state with hints populated or NO_MOVES state.
     *
     * Does NOT handle autoComplete() — that stays in ViewModel.
     */
    fun applyLayeredHint(state: GameUIState): GameUIState {
        // Early return: game state check
        if (state.gameState != com.rekluzgames.nikakudorimahjong.domain.model.GameState.PLAYING) {
            return state
        }

        // Mark hint as used
        var newState = state.copy(usedHint = true)

        // If no hints cached, find them
        if (newState.layeredHints.isEmpty()) {
            val hints = LayeredHintFinder.findAllMatches(state.layeredTiles, layeredEngine)
            if (hints.isNotEmpty()) {
                newState = newState.copy(
                    layeredHints = hints,
                    currentLayeredHintIndex = 0
                )
            } else {
                // No hints available → transition to NO_MOVES
                newState = newState.copy(
                    gameState = com.rekluzgames.nikakudorimahjong.domain.model.GameState.NO_MOVES
                )
            }
        } else {
            // Cycle to next hint
            newState = newState.copy(
                currentLayeredHintIndex = (newState.currentLayeredHintIndex + 1) % newState.layeredHints.size
            )
        }

        return newState
    }
}