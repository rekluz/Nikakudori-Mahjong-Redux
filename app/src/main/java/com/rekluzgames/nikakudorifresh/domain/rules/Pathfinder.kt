/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 * This code and its assets are the exclusive property of Rekluz Games.
 * Unauthorized copying, distribution, or commercial use is strictly prohibited.
 */
package com.rekluzgames.nikakudorifresh.domain.rules

import com.rekluzgames.nikakudorifresh.domain.model.Tile

object PathFinder {
    fun canConnect(p1: Pair<Int, Int>, p2: Pair<Int, Int>, board: List<List<Tile>>): Boolean {
        if (board[p1.first][p1.second].type != board[p2.first][p2.second].type) return false
        if (isStraightPathClear(p1, p2, board)) return true

        val corners = listOf(Pair(p1.first, p2.second), Pair(p2.first, p1.second))
        for (c in corners) {
            if (isPassable(c, board) && isStraightPathClear(p1, c, board) && isStraightPathClear(c, p2, board)) return true
        }

        val rows = board.size; val cols = board[0].size
        for (r in -1..rows) {
            val c1 = Pair(r, p1.second); val c2 = Pair(r, p2.second)
            if (isPassable(c1, board) && isPassable(c2, board) && isStraightPathClear(p1, c1, board) && isStraightPathClear(c1, c2, board) && isStraightPathClear(c2, p2, board)) return true
        }
        for (c in -1..cols) {
            val c1 = Pair(p1.first, c); val c2 = Pair(p2.first, c)
            if (isPassable(c1, board) && isPassable(c2, board) && isStraightPathClear(p1, c1, board) && isStraightPathClear(c1, c2, board) && isStraightPathClear(c2, p2, board)) return true
        }
        return false
    }

    private fun isPassable(p: Pair<Int, Int>, board: List<List<Tile>>): Boolean {
        if (p.first < 0 || p.first >= board.size || p.second < 0 || p.second >= board[0].size) return true
        return board[p.first][p.second].isRemoved
    }

    private fun isStraightPathClear(p1: Pair<Int, Int>, p2: Pair<Int, Int>, board: List<List<Tile>>): Boolean {
        if (p1.first == p2.first) {
            val (s, e) = if (p1.second < p2.second) p1.second to p2.second else p2.second to p1.second
            for (c in s + 1 until e) if (!isPassable(Pair(p1.first, c), board)) return false
            return true
        }
        if (p1.second == p2.second) {
            val (s, e) = if (p1.first < p2.first) p1.first to p2.first else p2.first to p1.first
            for (r in s + 1 until e) if (!isPassable(Pair(r, p1.second), board)) return false
            return true
        }
        return false
    }
}