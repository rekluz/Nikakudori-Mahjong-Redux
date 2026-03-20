/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 * This code and its assets are the exclusive property of Rekluz Games.
 * Unauthorized copying, distribution, or commercial use is strictly prohibited.
 */

package com.rekluzgames.nikakudorimahjong.presentation.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.rekluzgames.nikakudorimahjong.presentation.viewmodel.GameUIState

@Composable
fun BoardGrid(uiState: GameUIState, onTileClick: (Int, Int) -> Unit) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 45.dp, vertical = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        val availableWidth = maxWidth.value
        val availableHeight = maxHeight.value

        val tileAspectRatio = 0.74f
        val hOverlap = 0.88f
        val vOverlap = 0.80f

        val maxTileWidth = availableWidth / (1f + (uiState.difficulty.cols - 1) * hOverlap)
        val maxTileHeight = availableHeight / (1f + (uiState.difficulty.rows - 1) * vOverlap)

        val tileHeight = if (maxTileWidth / tileAspectRatio < maxTileHeight) {
            maxTileWidth / tileAspectRatio
        } else {
            maxTileHeight
        }
        val tileWidth = tileHeight * tileAspectRatio

        val xStep = tileWidth * hOverlap
        val yStep = tileHeight * vOverlap

        val gridWidth = xStep * (uiState.difficulty.cols - 1) + tileWidth
        val gridHeight = yStep * (uiState.difficulty.rows - 1) + tileHeight

        Box(modifier = Modifier.size(width = gridWidth.dp, height = gridHeight.dp)) {

            // --- Tile layer ---
            uiState.board.forEachIndexed { r, row ->
                row.forEachIndexed { c, tile ->
                    key(tile.id) {
                        val isHint = uiState.activeHint?.let {
                            (it.first == r to c) || (it.second == r to c)
                        } ?: false

                        val isExploding = uiState.lastMatchedPair?.let {
                            (it.first == r to c) || (it.second == r to c)
                        } ?: false

                        val zPos = (r * 100 + c).toFloat()

                        Box(modifier = Modifier.zIndex(if (tile.isRemoved) 0f else zPos)) {
                            TileView(
                                tile = tile,
                                isSelected = uiState.selectedTile == r to c,
                                isHinted = isHint,
                                isExploding = isExploding,
                                width = tileWidth,
                                height = tileHeight,
                                xOffset = xStep * c,
                                yOffset = yStep * r
                            ) {
                                onTileClick(r, c)
                            }
                        }
                    }
                }
            }

            // --- Connection line overlay ---
            val pathPoints = uiState.lastMatchPath
            if (pathPoints != null && pathPoints.size >= 2) {
                Canvas(
                    modifier = Modifier
                        .size(width = gridWidth.dp, height = gridHeight.dp)
                        .zIndex(Float.MAX_VALUE)
                ) {
                    val d = density
                    val rows = uiState.difficulty.rows
                    val cols = uiState.difficulty.cols

                    val outsideMarginX = (tileWidth * (1f - hOverlap)) * d
                    val outsideMarginY = (tileHeight * (1f - vOverlap)) * d

                    fun tileCentrePx(row: Int, col: Int): Offset {
                        val px = when {
                            col < 0     -> -outsideMarginX
                            col >= cols -> (xStep * (cols - 1) + tileWidth) * d + outsideMarginX
                            else        -> (xStep * col + tileWidth / 2f) * d
                        }
                        val py = when {
                            row < 0     -> -outsideMarginY
                            row >= rows -> (yStep * (rows - 1) + tileHeight) * d + outsideMarginY
                            else        -> (yStep * row + tileHeight / 2f) * d
                        }
                        return Offset(px, py)
                    }

                    fun gapBetweenRows(upperRow: Int): Float {
                        return when {
                            upperRow < 0         -> -outsideMarginY
                            upperRow >= rows - 1 -> (yStep * (rows - 1) + tileHeight) * d + outsideMarginY
                            else                 -> (yStep * upperRow + yStep / 2f + tileHeight / 2f) * d
                        }
                    }

                    fun gapBetweenCols(leftCol: Int): Float {
                        return when {
                            leftCol < 0         -> -outsideMarginX
                            leftCol >= cols - 1 -> (xStep * (cols - 1) + tileWidth) * d + outsideMarginX
                            else                -> (xStep * leftCol + xStep / 2f + tileWidth / 2f) * d
                        }
                    }

                    val pixelPoints = pathPoints.mapIndexed { i, (row, col) ->
                        when {
                            i == 0 || i == pathPoints.size - 1 -> tileCentrePx(row, col)
                            else -> {
                                val prev = pathPoints[i - 1]
                                val next = pathPoints[i + 1]

                                val comingFromRow = prev.first != row
                                val comingFromCol = prev.second != col
                                val goingToRow = next.first != row
                                val goingToCol = next.second != col

                                if (comingFromRow && goingToRow) {
                                    val gapX = gapBetweenCols(minOf(col, next.second))
                                    val centreY = tileCentrePx(row, col).y
                                    Offset(gapX, centreY)
                                } else if (comingFromCol && goingToCol) {
                                    val centreX = tileCentrePx(row, col).x
                                    val gapY = gapBetweenRows(minOf(row, next.first))
                                    Offset(centreX, gapY)
                                } else {
                                    val horizontalRow = if (comingFromCol) prev.first else next.first
                                    val verticalCol = if (comingFromRow) prev.second else next.second
                                    val gapY = gapBetweenRows(minOf(row, horizontalRow))
                                    val gapX = gapBetweenCols(minOf(col, verticalCol))
                                    Offset(gapX, gapY)
                                }
                            }
                        }
                    }

                    val linePath = Path().apply {
                        moveTo(pixelPoints[0].x, pixelPoints[0].y)
                        for (i in 1 until pixelPoints.size) {
                            lineTo(pixelPoints[i].x, pixelPoints[i].y)
                        }
                    }

                    drawPath(
                        path = linePath,
                        color = Color(0xFF00BFFF),
                        style = Stroke(
                            width = 3f * d,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }
    }
}