/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 * This code and its assets are the exclusive property of Rekluz Games.
 * Unauthorized copying, distribution, or commercial use is strictly prohibited.
 */
package com.rekluzgames.nikakudorifresh.domain.rules

import com.rekluzgames.nikakudorifresh.domain.model.Difficulty
import com.rekluzgames.nikakudorifresh.domain.model.Tile

object BoardGenerator {
    fun createBoard(difficulty: Difficulty): List<List<Tile>> {
        val total = difficulty.rows * difficulty.cols
        val types = mutableListOf<Int>()
        for (i in 0 until total / 2) {
            types.add(i % 34)
            types.add(i % 34)
        }

        var attempts = 0
        while (attempts < 100) {
            types.shuffle()
            val board = List(difficulty.rows) { r ->
                List(difficulty.cols) { c ->
                    Tile(r * difficulty.cols + c, types[r * difficulty.cols + c])
                }
            }
            // Check if any move exists using the updated HintFinder
            if (HintFinder.findAllMatches(board).isNotEmpty()) {
                return board
            }
            attempts++
        }

        return List(difficulty.rows) { r ->
            List(difficulty.cols) { c ->
                Tile(r * difficulty.cols + c, types[r * difficulty.cols + c])
            }
        }
    }
}