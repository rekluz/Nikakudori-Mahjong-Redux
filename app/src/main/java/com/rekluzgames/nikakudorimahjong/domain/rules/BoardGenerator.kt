/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 * This code and its assets are the exclusive property of Rekluz Games.
 * Unauthorized copying, distribution, or commercial use is strictly prohibited.
 */

package com.rekluzgames.nikakudorimahjong.domain.rules

import com.rekluzgames.nikakudorimahjong.domain.model.Difficulty
import com.rekluzgames.nikakudorimahjong.domain.model.Tile

object BoardGenerator {

    // Maximum number of full generation attempts before falling back
    private const val MAX_ATTEMPTS = 10

    fun createBoard(difficulty: Difficulty): List<List<Tile>> {
        // Attempt backward generation + solver pass up to MAX_ATTEMPTS times
        repeat(MAX_ATTEMPTS) {
            val candidate = tryBackwardGeneration(difficulty)
            if (candidate != null && isBoardFullySolvable(candidate)) {
                return candidate
            }
        }

        // Fallback: random shuffle with increased retry attempts
        return fallbackGeneration(difficulty)
    }

    // -------------------------------------------------------------------------
    // BACKWARD GENERATION
    // Builds the board by placing matched pairs onto an initially empty board.
    // Each pair is only placed when PathFinder confirms a valid path exists
    // between the two chosen cells at placement time.
    // -------------------------------------------------------------------------
    private fun tryBackwardGeneration(difficulty: Difficulty): List<List<Tile>>? {
        val rows = difficulty.rows
        val cols = difficulty.cols
        val total = rows * cols

        if (total % 2 != 0) return null

        val pairsNeeded = total / 2

        // Build a shuffled pool of tile types (each type appears exactly twice)
        val typePool = mutableListOf<Int>()
        for (i in 0 until pairsNeeded) {
            val type = i % 34
            typePool.add(type)
            typePool.add(type)
        }
        typePool.shuffle()

        // Start with a fully empty board — all tiles removed
        var board = List(rows) { r ->
            List(cols) { c ->
                Tile(r * cols + c, 0, isRemoved = true)
            }
        }

        // Track which cells are still empty (available for placement)
        val emptyCells = (0 until rows).flatMap { r ->
            (0 until cols).map { c -> r to c }
        }.toMutableList()
        emptyCells.shuffle()

        var typeIndex = 0

        // Place pairs one at a time
        while (emptyCells.size >= 2 && typeIndex < typePool.size - 1) {
            val type = typePool[typeIndex]

            var placed = false
            val shuffledCells = emptyCells.shuffled()

            outer@ for (i in shuffledCells.indices) {
                for (j in i + 1 until shuffledCells.size) {
                    val p1 = shuffledCells[i]
                    val p2 = shuffledCells[j]

                    // Temporarily place the type on both cells to test connectivity
                    val testBoard = board.mapIndexed { r, row ->
                        row.mapIndexed { c, tile ->
                            when (r to c) {
                                p1 -> tile.copy(type = type, isRemoved = false)
                                p2 -> tile.copy(type = type, isRemoved = false)
                                else -> tile
                            }
                        }
                    }

                    if (PathFinder.canConnect(p1, p2, testBoard)) {
                        board = testBoard
                        emptyCells.remove(p1)
                        emptyCells.remove(p2)
                        typeIndex += 2
                        placed = true
                        break@outer
                    }
                }
            }

            // Couldn't place this pair — backward generation failed
            if (!placed) return null
        }

        return board
    }

    // -------------------------------------------------------------------------
    // SOLVER PASS
    // Simulates a full solve of the board by greedily removing available matches
    // one at a time until either all tiles are cleared (fully solvable) or no
    // matches remain with tiles still on the board (stuck — reject this board).
    //
    // This catches cases where backward generation placed all pairs with valid
    // paths at placement time, but later removals blocked earlier paths — making
    // the board unwinnable without a shuffle.
    // -------------------------------------------------------------------------
    private fun isBoardFullySolvable(board: List<List<Tile>>): Boolean {
        var currentBoard = board

        while (true) {
            val remaining = currentBoard.flatten().count { !it.isRemoved }

            // All tiles cleared — board is fully solvable
            if (remaining == 0) return true

            val matches = HintFinder.findAllMatches(currentBoard)

            // Tiles remain but no matches exist — board is stuck
            if (matches.isEmpty()) return false

            // Remove the first available match and continue simulating
            val (p1, p2) = matches.first()
            currentBoard = currentBoard.mapIndexed { r, row ->
                row.mapIndexed { c, tile ->
                    if ((r == p1.first && c == p1.second) ||
                        (r == p2.first && c == p2.second)
                    ) tile.copy(isRemoved = true)
                    else tile
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // FALLBACK GENERATION
    // Original shuffle approach with increased retry attempts.
    // Only reached if all MAX_ATTEMPTS backward generation attempts fail.
    // -------------------------------------------------------------------------
    private fun fallbackGeneration(difficulty: Difficulty): List<List<Tile>> {
        val total = difficulty.rows * difficulty.cols
        val types = mutableListOf<Int>()
        for (i in 0 until total / 2) {
            types.add(i % 34)
            types.add(i % 34)
        }

        var attempts = 0
        while (attempts < 200) {
            types.shuffle()
            val board = List(difficulty.rows) { r ->
                List(difficulty.cols) { c ->
                    Tile(r * difficulty.cols + c, types[r * difficulty.cols + c])
                }
            }
            if (HintFinder.findAllMatches(board).isNotEmpty()) return board
            attempts++
        }

        // Last resort — return whatever we have
        types.shuffle()
        return List(difficulty.rows) { r ->
            List(difficulty.cols) { c ->
                Tile(r * difficulty.cols + c, types[r * difficulty.cols + c])
            }
        }
    }
}