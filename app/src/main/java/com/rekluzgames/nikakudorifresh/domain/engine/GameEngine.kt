/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 * This code and its assets are the exclusive property of Rekluz Games.
 * Unauthorized copying, distribution, or commercial use is strictly prohibited.
 */

package com.rekluzgames.nikakudorifresh.domain.engine

import com.rekluzgames.nikakudorifresh.domain.model.Tile
import com.rekluzgames.nikakudorifresh.domain.rules.PathFinder

class GameEngine {
    fun attemptMatch(p1: Pair<Int, Int>, p2: Pair<Int, Int>, board: List<List<Tile>>): List<List<Tile>>? {
        if (PathFinder.canConnect(p1, p2, board)) {
            return board.mapIndexed { r, row ->
                row.mapIndexed { c, tile ->
                    if ((r == p1.first && c == p1.second) || (r == p2.first && c == p2.second)) {
                        tile.copy(isRemoved = true)
                    } else tile
                }
            }
        }
        return null
    }

    fun applyGravity(board: List<List<Tile>>): List<List<Tile>> {
        val rows = board.size
        val cols = board[0].size
        val newBoard = List(rows) { MutableList(cols) { Tile(-1, 0, true) } }

        for (c in 0 until cols) {
            val activeTiles = mutableListOf<Tile>()
            for (r in 0 until rows) {
                if (!board[r][c].isRemoved) activeTiles.add(board[r][c])
            }
            var writeIdx = rows - 1
            for (i in activeTiles.size - 1 downTo 0) {
                newBoard[writeIdx][c] = activeTiles[i]
                writeIdx--
            }
        }
        return newBoard
    }

    fun isGameOver(board: List<List<Tile>>): Boolean {
        return board.flatten().all { it.isRemoved }
    }
}