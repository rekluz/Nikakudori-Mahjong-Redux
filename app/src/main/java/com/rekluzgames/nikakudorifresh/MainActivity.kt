/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 * This code and its assets are the exclusive property of Rekluz Games.
 * Unauthorized copying, distribution, or commercial use is strictly prohibited.
 */

package com.rekluzgames.nikakudorifresh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import com.rekluzgames.nikakudorifresh.data.audio.SoundManager
import com.rekluzgames.nikakudorifresh.data.preference.PreferenceManager
import com.rekluzgames.nikakudorifresh.data.repository.GameRepository
import com.rekluzgames.nikakudorifresh.presentation.ui.screen.GameScreen
import com.rekluzgames.nikakudorifresh.presentation.ui.theme.NikakudoriTheme
import com.rekluzgames.nikakudorifresh.presentation.viewmodel.GameViewModel
import com.rekluzgames.nikakudorifresh.presentation.viewmodel.GameViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: GameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Install the Splash Screen
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // 2. ENABLE IMMERSIVE MODE
        // This tells the window NOT to fit system windows, allowing us to draw full-screen.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideSystemUI()

        // 3. Initialize Data Layer
        val soundManager = SoundManager(applicationContext)
        val prefManager = PreferenceManager(applicationContext)
        val repository = GameRepository(prefManager)

        // 4. Initialize ViewModel
        val factory = GameViewModelFactory(soundManager, repository)
        viewModel = ViewModelProvider(this, factory)[GameViewModel::class.java]

        setContent {
            NikakudoriTheme {
                GameScreen(viewModel)
            }
        }
    }

    /**
     * Professional Immersive Mode Implementation
     * Hides both the Status Bar (top) and Navigation Bar (bottom).
     * Behavior is set to "Transient Bars" so a swipe from the edge
     * brings them back briefly without resizing the game layout.
     */
    private fun hideSystemUI() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        // Configure the behavior of how the bars reappear
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Hide both status bars and navigation bars
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    /**
     * Re-apply immersive mode when the app regains focus.
     * This ensures that if a user swipes to see the time/battery,
     * the bars hide again automatically once they stop interacting with them.
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::viewModel.isInitialized) {
            viewModel.releaseResources()
        }
    }
}