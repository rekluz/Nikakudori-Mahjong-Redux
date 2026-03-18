/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 * This code and its assets are the exclusive property of Rekluz Games.
 * Unauthorized copying, distribution, or commercial use is strictly prohibited.
 */

package com.rekluzgames.nikakudorifresh.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rekluzgames.nikakudorifresh.data.audio.SoundManager
import com.rekluzgames.nikakudorifresh.data.repository.GameRepository

class GameViewModelFactory(
    private val soundManager: SoundManager,
    private val repository: GameRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(soundManager, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}