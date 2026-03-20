/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 */

package com.rekluzgames.nikakudorimahjong.data.repository

import com.rekluzgames.nikakudorimahjong.data.preference.PreferenceManager

class GameRepository(private val prefs: PreferenceManager) {
    fun getSettings() = prefs
}