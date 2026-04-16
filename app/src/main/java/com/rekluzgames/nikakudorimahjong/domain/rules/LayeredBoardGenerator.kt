/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 */

package com.rekluzgames.nikakudorimahjong.domain.rules

import com.rekluzgames.nikakudorimahjong.domain.model.LayeredLayout
import com.rekluzgames.nikakudorimahjong.domain.model.LayeredTile
import com.rekluzgames.nikakudorimahjong.domain.model.TilePosition
import kotlin.math.abs
import kotlin.random.Random
import java.util.BitSet

object LayeredBoardGenerator {

    private const val TOTAL_TILE_TYPES = 34
    private const val MAX_ATTEMPTS = 15
    private const val SLOW_RECURSION_THRESHOLD = 5000

    fun generate(layout: LayeredLayout): List<LayeredTile> {
        val stats = GenerationStats(layout.id)

        repeat(MAX_ATTEMPTS) { attempt ->
            stats.attempts++
            val rng = Random(System.nanoTime() + attempt * 12345)

            val tiles = generateWithSolver(layout, rng, stats)
            if (tiles != null) {
                stats.successPath = "solver"
                logIfInteresting(stats)
                return tiles
            }
        }

        stats.usedTrivial = true
        val fallback = buildTrivial(layout)
        logIfInteresting(stats)
        return fallback
    }

    private fun generateWithSolver(
        layout: LayeredLayout,
        rng: Random,
        stats: GenerationStats
    ): List<LayeredTile>? {
        val positions = layout.positions
        val total = positions.size
        
        val allPositionsSet = positions.toSet()
        
        data class TileInfo(
            val position: TilePosition,
            var isRemoved: Boolean = false
        )
        
        val tileInfos = positions.map { TileInfo(it) }.toMutableList()
        val posToIndex = positions.withIndex().associate { it.value to it.index }
        
        val removed = BitSet(total)
        val solutionPairs = mutableListOf<Pair<Int, Int>>()
        
        fun isFree(index: Int): Boolean {
            val pos = positions[index]
            if (removed.get(index)) return false
            
            val blockedAbove = positions.indices.any { other ->
                !removed.get(other) &&
                positions[other].layer > pos.layer &&
                abs(positions[other].col - pos.col) < 2 &&
                abs(positions[other].row - pos.row) < 2
            }
            if (blockedAbove) return false
            
            val blockedLeft = positions.indices.any { other ->
                !removed.get(other) &&
                positions[other].layer == pos.layer &&
                positions[other].col == pos.col - 2 &&
                abs(positions[other].row - pos.row) < 2
            }
            
            val blockedRight = positions.indices.any { other ->
                !removed.get(other) &&
                positions[other].layer == pos.layer &&
                positions[other].col == pos.col + 2 &&
                abs(positions[other].row - pos.row) < 2
            }
            
            return !blockedLeft || !blockedRight
        }
        
        fun findFreePairs(): List<Pair<Int, Int>> {
            val freeIndices = (0 until total).filter { isFree(it) }
            val pairs = mutableListOf<Pair<Int, Int>>()
            for (i in freeIndices.indices) {
                for (j in i + 1 until freeIndices.size) {
                    pairs.add(freeIndices[i] to freeIndices[j])
                }
            }
            return pairs
        }
        
        fun solve(remaining: Int): Boolean {
            stats.recursions++
            
            if (remaining == 0) return true
            
            val freePairs = findFreePairs()
            if (freePairs.isEmpty()) return false
            
            val shuffled = freePairs.shuffled(rng)
            
            for ((i1, i2) in shuffled) {
                removed.set(i1)
                removed.set(i2)
                solutionPairs.add(i1 to i2)
                
                if (solve(remaining - 2)) return true
                
                removed.clear(i1)
                removed.clear(i2)
                solutionPairs.removeAt(solutionPairs.lastIndex)
            }
            
            return false
        }
        
        if (!solve(total)) return null
        
        val typePool = generateTypePool(total, rng)
        
        return positions.mapIndexed { index, pos ->
            val pairIdx = solutionPairs.indexOfFirst { it.first == index || it.second == index }
            LayeredTile(
                id = index,
                type = typePool[pairIdx.coerceAtLeast(0)],
                col = pos.col,
                row = pos.row,
                layer = pos.layer
            )
        }
    }

    private fun buildTrivial(layout: LayeredLayout): List<LayeredTile> {
        return layout.positions.mapIndexed { index, pos ->
            LayeredTile(
                id = index,
                type = index / 2,
                col = pos.col,
                row = pos.row,
                layer = pos.layer
            )
        }
    }

    private fun generateTypePool(totalTiles: Int, rng: Random): IntArray {
        val pairCount = totalTiles / 2
        val pool = IntArray(pairCount)
        var index = 0

        var types = (0 until TOTAL_TILE_TYPES).shuffled(rng)
        var typeIdx = 0

        fun nextType(): Int {
            if (typeIdx >= types.size) {
                types = (0 until TOTAL_TILE_TYPES).shuffled(rng)
                typeIdx = 0
            }
            return types[typeIdx++]
        }

        while (index + 2 <= pairCount) {
            val t = nextType()
            repeat(2) { pool[index++] = t }
        }

        while (index < pairCount) {
            pool[index++] = nextType()
        }

        pool.shuffle(rng)
        return pool
    }

    private data class GenerationStats(
        val layoutId: String,
        var attempts: Int = 0,
        var recursions: Int = 0,
        var usedTrivial: Boolean = false,
        var successPath: String = "unknown"
    )

    private fun logIfInteresting(stats: GenerationStats) {
        val slow = stats.recursions > SLOW_RECURSION_THRESHOLD

        if (slow || stats.usedTrivial) {
            val message = "LayeredBoardGenerator: layout=${stats.layoutId} " +
                    "path=${stats.successPath} " +
                    "attempts=${stats.attempts} " +
                    "rec=${stats.recursions} " +
                    "trivial=${stats.usedTrivial}"
            if (slow) {
                System.err.println(message)
            } else {
                System.out.println(message)
            }
        }
    }
}