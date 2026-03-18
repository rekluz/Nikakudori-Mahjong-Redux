/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 * This code and its assets are the exclusive property of Rekluz Games.
 * Unauthorized copying, distribution, or commercial use is strictly prohibited.
 */

package com.rekluzgames.nikakudorifresh.data.preference

import android.content.Context
import com.rekluzgames.nikakudorifresh.domain.model.GameMode

class PreferenceManager(context: Context) {
    private val prefs = context.getSharedPreferences("NikakudoriPrefs", Context.MODE_PRIVATE)

    fun isSoundEnabled() = prefs.getBoolean("sound", true)
    fun setSoundEnabled(v: Boolean) = prefs.edit().putBoolean("sound", v).apply()

    fun getScale() = prefs.getFloat("scale", 1.0f)
    fun setScale(v: Float) = prefs.edit().putFloat("scale", v).apply()

    fun getGameMode(): GameMode {
        val modeName = prefs.getString("game_mode", GameMode.REGULAR.name)
        return GameMode.valueOf(modeName ?: GameMode.REGULAR.name)
    }

    fun setGameMode(mode: GameMode) {
        prefs.edit().putString("game_mode", mode.name).apply()
    }
}