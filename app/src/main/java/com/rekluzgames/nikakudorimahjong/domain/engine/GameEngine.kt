/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 */

package com.rekluzgames.nikakudorimahjong.domain.engine

import com.rekluzgames.nikakudorimahjong.domain.model.Tile
import com.rekluzgames.nikakudorimahjong.domain.rules.PathFinder

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

    /**
     * Re-calculates tile positions based on vertical gravity.
     * Logic: For every column, push all active tiles to the bottom.
     * Reuses existing 'removed' tiles to fill the top, maintaining ID stability.
     */
    fun applyGravity(board: List<List<Tile>>): List<List<Tile>> {
        if (board.isEmpty()) return board
        val rows = board.size
        val cols = board[0].size

        // 1. Process each column independently
        val shiftedColumns = (0 until cols).map { c ->
            val columnTiles = (0 until rows).map { r -> board[r][c] }

            val active = columnTiles.filter { !it.isRemoved }
            val removed = columnTiles.filter { it.isRemoved }

            // Result: Removed tiles at the top, active tiles at the bottom
            removed + active
        }

        // 2. Reconstruct the 2D grid from the shifted columns
        return List(rows) { r ->
            List(cols) { c ->
                shiftedColumns[c][r]
            }
        }
    }

    fun isGameOver(board: List<List<Tile>>): Boolean {
        return board.flatten().all { it.isRemoved }
    }
}