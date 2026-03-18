/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 * This code and its assets are the exclusive property of Rekluz Games.
 * Unauthorized copying, distribution, or commercial use is strictly prohibited.
 */

package com.rekluzgames.nikakudorifresh.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekluzgames.nikakudorifresh.data.audio.SoundManager
import com.rekluzgames.nikakudorifresh.data.repository.GameRepository
import com.rekluzgames.nikakudorifresh.domain.engine.GameEngine
import com.rekluzgames.nikakudorifresh.domain.model.*
import com.rekluzgames.nikakudorifresh.domain.rules.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class GameViewModel(
    private val soundManager: SoundManager,
    private val repository: GameRepository
) : ViewModel() {

    private val engine = GameEngine()
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState = _uiState.asStateFlow()
    private var timerJob: Job? = null

    // Tracks if we need to restart the board when closing settings
    private var modeWasChanged = false

    init {
        val settings = repository.getSettings()
        _uiState.update { it.copy(
            isSoundEnabled = settings.isSoundEnabled(),
            boardScale = settings.getScale(),
            gameMode = settings.getGameMode()
        ) }
        soundManager.isEnabled = _uiState.value.isSoundEnabled
        startNewGame(Difficulty.NORMAL)
    }

    fun startNewGame(diff: Difficulty) {
        timerJob?.cancel()
        modeWasChanged = false // Reset change tracker
        _uiState.update { it.copy(
            gameState = GameState.LOADING,
            difficulty = diff,
            selectedTile = null,
            allAvailableHints = emptyList(),
            currentHintIndex = -1,
            undoHistory = emptyList()
        ) }
        viewModelScope.launch(Dispatchers.Default) {
            val board = BoardGenerator.createBoard(diff)
            _uiState.update { it.copy(board = board, gameState = GameState.PLAYING, timeSeconds = 0, shufflesRemaining = diff.shuffles) }
            startTimer()
        }
    }

    fun toggleGameMode() {
        val currentMode = _uiState.value.gameMode
        val newMode = if (currentMode == GameMode.REGULAR) GameMode.GRAVITY else GameMode.REGULAR

        repository.getSettings().setGameMode(newMode)
        _uiState.update { it.copy(gameMode = newMode) }

        // Mark that a restart is required when the user hits "DONE"
        modeWasChanged = true
    }

    fun applySettingsAndResume() {
        if (modeWasChanged) {
            // Restart board because gravity/regular physics changed
            startNewGame(_uiState.value.difficulty)
        } else {
            // Just go back to play mode
            changeState(GameState.PLAYING)
        }
    }

    fun handleTileClick(r: Int, c: Int) {
        val state = _uiState.value
        if (state.gameState != GameState.PLAYING || state.board[r][c].isRemoved) return

        if (state.selectedTile == null) {
            _uiState.update { it.copy(selectedTile = r to c, allAvailableHints = emptyList(), currentHintIndex = -1) }
            soundManager.play("tile_click")
        } else {
            val p1 = state.selectedTile!!
            if (p1 == r to c) {
                _uiState.update { it.copy(selectedTile = null) }
            } else {
                var nextBoard = engine.attemptMatch(p1, r to c, state.board)
                if (nextBoard != null) {
                    soundManager.play("tile_match")
                    if (state.gameMode == GameMode.GRAVITY) {
                        nextBoard = engine.applyGravity(nextBoard)
                    }
                    _uiState.update { it.copy(
                        board = nextBoard,
                        selectedTile = null,
                        allAvailableHints = emptyList(),
                        currentHintIndex = -1,
                        undoHistory = it.undoHistory + (p1 to (r to c))
                    ) }
                    if (engine.isGameOver(nextBoard)) {
                        _uiState.update { it.copy(gameState = GameState.WON) }
                        timerJob?.cancel()
                    }
                } else {
                    _uiState.update { it.copy(selectedTile = r to c) }
                    soundManager.play("tile_error")
                }
            }
        }
    }

    fun getHint() {
        val state = _uiState.value
        if (state.allAvailableHints.isEmpty()) {
            val hints = HintFinder.findAllMatches(state.board)
            if (hints.isNotEmpty()) {
                _uiState.update { it.copy(allAvailableHints = hints, currentHintIndex = 0) }
            }
        } else {
            val nextIndex = (state.currentHintIndex + 1) % state.allAvailableHints.size
            _uiState.update { it.copy(currentHintIndex = nextIndex) }
        }
    }

    fun shuffle() {
        val state = _uiState.value
        if (state.shufflesRemaining <= 0) return
        val activeTypes = state.board.flatten().filter { !it.isRemoved }.map { it.type }.shuffled()
        var idx = 0
        val newBoard = state.board.map { row ->
            row.map { tile -> if (!tile.isRemoved) tile.copy(type = activeTypes[idx++]) else tile }
        }
        _uiState.update { it.copy(board = newBoard, shufflesRemaining = it.shufflesRemaining - 1, selectedTile = null, allAvailableHints = emptyList(), currentHintIndex = -1) }
    }

    fun undo() {
        val state = _uiState.value
        if (state.undoHistory.isEmpty()) return
        val lastMatch = state.undoHistory.last()
        val restoredBoard = state.board.mapIndexed { r, row ->
            row.mapIndexed { c, tile ->
                if ((r == lastMatch.first.first && c == lastMatch.first.second) ||
                    (r == lastMatch.second.first && c == lastMatch.second.second)) {
                    tile.copy(isRemoved = false)
                } else tile
            }
        }
        _uiState.update { it.copy(board = restoredBoard, undoHistory = it.undoHistory.dropLast(1), selectedTile = null, allAvailableHints = emptyList(), currentHintIndex = -1) }
    }

    fun onAboutTileClick(id: Int, target: Int) {
        _uiState.update { state ->
            val newSet = state.clearedAboutTiles + id
            if (newSet.size >= target) {
                soundManager.play("secret_unlocked")
                state.copy(aboutStage = 1, clearedAboutTiles = emptySet())
            } else state.copy(clearedAboutTiles = newSet)
        }
    }

    fun resetAbout() { _uiState.update { it.copy(aboutStage = 0, clearedAboutTiles = emptySet()) } }
    fun changeState(s: GameState) { _uiState.update { it.copy(gameState = s) } }

    fun updateSoundEnabled(enabled: Boolean) {
        repository.getSettings().setSoundEnabled(enabled)
        soundManager.isEnabled = enabled
        _uiState.update { it.copy(isSoundEnabled = enabled) }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (_uiState.value.gameState == GameState.PLAYING) _uiState.update { it.copy(timeSeconds = it.timeSeconds + 1) }
            }
        }
    }

    fun releaseResources() { timerJob?.cancel(); soundManager.release() }
    override fun onCleared() { super.onCleared(); releaseResources() }
}