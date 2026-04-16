/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 */

package com.rekluzgames.nikakudorimahjong.data.audio

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.rekluzgames.nikakudorimahjong.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: com.rekluzgames.nikakudorimahjong.data.repository.GameRepository
) : DefaultLifecycleObserver {

    private var mediaPlayer: MediaPlayer? = null
    var isEnabled: Boolean = repository.isMusicEnabled()
    var isInitialized: Boolean = false
    private var currentTrack: Int? = null
    private var fadeJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val musicTracks = listOf(
        R.raw.gamemusic1,
        R.raw.gamemusic2,
        R.raw.gamemusic3
    )

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    private fun getRandomMusic(): Int {
        val available = musicTracks.filter { it != currentTrack }
        return available.random()
    }

    private fun playTrack(track: Int) {
        currentTrack = track
        mediaPlayer?.release()

        val player = MediaPlayer.create(context, track) ?: return
        player.setOnCompletionListener {
            if (isEnabled) {
                playTrack(getRandomMusic())
            }
        }
        player.start()
        mediaPlayer = player
    }

    fun start() {
        if (isEnabled && mediaPlayer == null) {
            playTrack(getRandomMusic())
            isInitialized = true
        }
    }

    fun resume() {
        if (isEnabled) mediaPlayer?.start()
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    override fun onStop(owner: LifecycleOwner) {
        mediaPlayer?.pause()
    }

    override fun onStart(owner: LifecycleOwner) {
        if (isEnabled) mediaPlayer?.start()
    }

    fun release() {
        fadeJob?.cancel()
        fadeJob = scope.launch {
            fadeOutAndRelease()
        }
    }

    private suspend fun fadeOutAndRelease() {
        val player = mediaPlayer ?: run {
            releaseImmediately()
            return
        }

        val fadeDuration = 1000L
        val steps = 20
        val stepDelay = fadeDuration / steps

        for (i in steps downTo 0) {
            if (player.isPlaying) {
                val volume = i.toFloat() / steps
                player.setVolume(volume, volume)
                delay(stepDelay)
            }
        }

        player.stop()
        player.release()
        mediaPlayer = null
        currentTrack = null
        isInitialized = false
    }

    private fun releaseImmediately() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        currentTrack = null
        isInitialized = false
    }
}