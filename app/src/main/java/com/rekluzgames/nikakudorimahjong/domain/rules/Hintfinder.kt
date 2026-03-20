/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 * This code and its assets are the exclusive property of Rekluz Games.
 * Unauthorized copying, distribution, or commercial use is strictly prohibited.
 */

package com.rekluzgames.nikakudorimahjong.domain.rules

import com.rekluzgames.nikakudorimahjong.domain.model.Tile

object HintFinder {
    /**
     * Finds every possible valid match on the current board.
     * Returns a list of pairs of coordinates.
     */
    fun findAllMatches(board: List<List<Tile>>): List<Pair<Pair<Int, Int>, Pair<Int, Int>>> {
        val matches = mutableListOf<Pair<Pair<Int, Int>, Pair<Int, Int>>>()
        if (board.isEmpty()) return matches

        val rows = board.size
        val cols = board[0].size

        for (r1 in 0 until rows) {
            for (c1 in 0 until cols) {
                val t1 = board[r1][c1]
                if (t1.isRemoved) continue

                for (r2 in 0 until rows) {
                    for (c2 in 0 until cols) {
                        // Skip if it's the same tile or already removed
                        if ((r1 == r2 && c1 == c2) || board[r2][c2].isRemoved) continue

                        val t2 = board[r2][c2]
                        // Only calculate path if the tiles are the same type
                        if (t1.type == t2.type) {
                            val p1 = r1 to c1
                            val p2 = r2 to c2

                            // Check if this pair (in either direction) is already in the list
                            val alreadyFound = matches.any {
                                (it.first == p1 && it.second == p2) || (it.first == p2 && it.second == p1)
                            }

                            if (!alreadyFound && PathFinder.canConnect(p1, p2, board)) {
                                matches.add(p1 to p2)
                            }
                        }
                    }
                }
            }
        }
        return matches
    }
}