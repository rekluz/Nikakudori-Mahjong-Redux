/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 * This code and its assets are the exclusive property of Rekluz Games.
 * Unauthorized copying, distribution, or commercial use is strictly prohibited.
 */

package com.rekluzgames.nikakudorifresh.presentation.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.rekluzgames.nikakudorifresh.presentation.viewmodel.GameUiState

@Composable
fun BoardGrid(uiState: GameUiState, onTileClick: (Int, Int) -> Unit) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 45.dp, vertical = 15.dp), // Safe zone for camera/edges
        contentAlignment = Alignment.Center
    ) {
        val availableWidth = maxWidth.value
        val availableHeight = maxHeight.value

        val tileAspectRatio = 0.74f

        // Overlap Multipliers (Tweak these to match your asset depth)
        // 0.88 means tiles overlap by 12% of their width
        // 0.80 means tiles overlap by 20% of their height (standard for 3D look)
        val hOverlap = 0.88f
        val vOverlap = 0.80f

        // Revised Math: Total Width = (Cols - 1) * (Width * hOverlap) + Width
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
            uiState.board.forEachIndexed { r, row ->
                row.forEachIndexed { c, tile ->
                    key(tile.id) {
                        val isHint = uiState.activeHint?.let {
                            (it.first == r to c) || (it.second == r to c)
                        } ?: false

                        // CRITICAL: zIndex ensures tiles overlap correctly
                        // Tiles lower and further right have higher Z-order
                        val zPos = (r * 100 + c).toFloat()

                        Box(modifier = Modifier.zIndex(if (tile.isRemoved) 0f else zPos)) {
                            TileView(
                                tile = tile,
                                isSelected = uiState.selectedTile == r to c,
                                isHinted = isHint,
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
        }
    }
}