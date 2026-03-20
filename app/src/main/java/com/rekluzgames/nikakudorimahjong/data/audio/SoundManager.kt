/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 */

package com.rekluzgames.nikakudorimahjong.data.audio

import android.content.Context
import android.media.SoundPool

class SoundManager(private val context: Context) {
    private var soundPool: SoundPool? = SoundPool.Builder().setMaxStreams(6).build()
    private val sounds = mutableMapOf<String, Int>()
    var isEnabled: Boolean = true

    init {
        // Added "tile_tada" to the load list
        listOf("tile_click", "tile_error", "tile_match", "tile_tada", "secret_unlocked", "thankyousomuch").forEach { name ->
            val resId = context.resources.getIdentifier(name, "raw", context.packageName)
            if (resId != 0) sounds[name] = soundPool?.load(context, resId, 1) ?: 0
        }
    }

    fun play(name: String) {
        if (isEnabled) sounds[name]?.let { soundPool?.play(it, 1f, 1f, 1, 0, 1f) }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}