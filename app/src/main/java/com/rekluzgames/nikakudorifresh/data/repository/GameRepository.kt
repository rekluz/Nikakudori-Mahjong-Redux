/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 * This code and its assets are the exclusive property of Rekluz Games.
 * Unauthorized copying, distribution, or commercial use is strictly prohibited.
 */

package com.rekluzgames.nikakudorifresh.data.repository

import com.rekluzgames.nikakudorifresh.data.preference.PreferenceManager

class GameRepository(private val prefs: PreferenceManager) {
    fun getSettings() = prefs
}