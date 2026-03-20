/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 */

package com.rekluzgames.nikakudorimahjong

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rekluzgames.nikakudorimahjong.data.audio.SoundManager
import com.rekluzgames.nikakudorimahjong.data.preference.PreferenceManager
import com.rekluzgames.nikakudorimahjong.data.repository.GameRepository
import com.rekluzgames.nikakudorimahjong.presentation.viewmodel.GameViewModel
import com.rekluzgames.nikakudorimahjong.presentation.viewmodel.GameViewModelFactory
import com.rekluzgames.nikakudorimahjong.presentation.ui.theme.NikakudoriTheme
import com.rekluzgames.nikakudorimahjong.presentation.ui.screen.GameScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferenceManager = PreferenceManager(applicationContext)
        val repository = GameRepository(preferenceManager)
        val soundManager = SoundManager(applicationContext)
        val factory = GameViewModelFactory(soundManager, repository)

        enableEdgeToEdge()

        setContent {
            val gameViewModel: GameViewModel = viewModel(factory = factory)
            val uiState by gameViewModel.uiState.collectAsState()

            // Inject the version from BuildConfig so AboutScreen always
            // reflects the current versionName from build.gradle.kts
            LaunchedEffect(Unit) {
                gameViewModel.setVersion(BuildConfig.VERSION_NAME)
            }

            // Handle Full Screen / Immersive Mode based on UI State
            LaunchedEffect(uiState.isFullScreen) {
                val window = this@MainActivity.window
                val controller = WindowCompat.getInsetsController(window, window.decorView)

                if (uiState.isFullScreen) {
                    controller.hide(WindowInsetsCompat.Type.systemBars())
                    controller.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } else {
                    controller.show(WindowInsetsCompat.Type.systemBars())
                }
            }

            NikakudoriTheme {
                GameScreen(viewModel = gameViewModel)
            }
        }
    }
}