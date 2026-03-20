/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 * This code and its assets are the exclusive property of Rekluz Games.
 * Unauthorized copying, distribution, or commercial use is strictly prohibited.
 */

package com.rekluzgames.nikakudorimahjong.domain.model

/**
 * Difficulty Enum
 * Standardized to 5 shuffles for all game modes.
 */
enum class Difficulty(val rows: Int, val cols: Int, val shuffles: Int, val label: String) {
    EASY(5, 14, 5, "EASY"),
    NORMAL(7, 16, 5, "NORMAL"),
    HARD(8, 17, 5, "HARD"),
    EXTREME(8, 22, 5, "EXTREME")
}