/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 * This code and its assets are the exclusive property of Rekluz Games.
 * Unauthorized copying, distribution, or commercial use is strictly prohibited.
 */

package com.rekluzgames.nikakudorimahjong.domain.model

enum class Medal(val label: String, val icon: String, val description: String) {
    SNIPER("SNIPER", "🎯", "Cleared without using a Hint"),
    FLASH("FLASH", "⚡", "Cleared in under 2 minutes"),
    STRATEGIST("STRATEGIST", "🧠", "Cleared without using a Shuffle")
}