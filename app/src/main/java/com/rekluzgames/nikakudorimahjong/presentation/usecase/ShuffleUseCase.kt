package com.rekluzgames.nikakudorimahjong.presentation.usecase

import javax.inject.Inject
import com.rekluzgames.nikakudorimahjong.domain.engine.LayeredGameEngine
import com.rekluzgames.nikakudorimahjong.domain.model.GameUIState
import com.rekluzgames.nikakudorimahjong.domain.rules.HintFinder
import com.rekluzgames.nikakudorimahjong.domain.rules.LayeredHintFinder

class ShuffleUseCase @Inject constructor(
    private val layeredEngine: LayeredGameEngine
) {

    fun shuffleFlat(state: GameUIState): GameUIState {
        if (state.shufflesRemaining <= 0) return state

        val activeTiles = state.board.flatten().filter { !it.isRemoved }
        if (activeTiles.isEmpty()) return state

        val shuffledTiles = activeTiles.shuffled()
        var index = 0
        var newBoard = state.board.map { row ->
            row.map { tile -> if (tile.isRemoved) tile else shuffledTiles[index++] }
        }

        repeat(100) {
            if (HintFinder.findAllMatches(newBoard).isNotEmpty()) return@repeat
            val retryTiles = newBoard.flatten().filter { !it.isRemoved }.shuffled()
            index = 0
            newBoard = newBoard.map { row ->
                row.map { tile -> if (tile.isRemoved) tile else retryTiles[index++] }
            }
        }

        return state.copy(
            board = newBoard,
            shufflesRemaining = state.shufflesRemaining - 1,
            usedShuffle = true,
            selectedTile = null,
            allAvailableHints = emptyList(),
            currentHintIndex = -1
        )
    }

    fun shuffleLayered(state: GameUIState): GameUIState {
        if (state.shufflesRemaining <= 0) return state

        val activeTiles = state.layeredTiles.filter { !it.isRemoved }
        if (activeTiles.isEmpty()) return state

        val shuffledTypes = activeTiles.map { it.type }.shuffled()
        var index = 0
        var newLayeredTiles = state.layeredTiles.map { tile ->
            if (tile.isRemoved) tile else tile.copy(type = shuffledTypes[index++])
        }

        repeat(100) {
            if (LayeredHintFinder.findAllMatches(newLayeredTiles, layeredEngine).isNotEmpty()) return@repeat
            val retryTypes = activeTiles.map { it.type }.shuffled()
            index = 0
            newLayeredTiles = state.layeredTiles.map { tile ->
                if (tile.isRemoved) tile else tile.copy(type = retryTypes[index++])
            }
        }

        return state.copy(
            layeredTiles = newLayeredTiles,
            shufflesRemaining = state.shufflesRemaining - 1,
            usedShuffle = true,
            selectedLayeredTileId = null,
            layeredHints = emptyList(),
            currentLayeredHintIndex = -1
        )
    }

    fun undoFlat(state: GameUIState): GameUIState {
        if (state.undoHistory.isEmpty()) return state

        val previousBoard = state.undoHistory.last()
        return state.copy(
            board = previousBoard,
            undoHistory = state.undoHistory.dropLast(1),
            selectedTile = null,
            allAvailableHints = emptyList(),
            currentHintIndex = -1
        )
    }

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