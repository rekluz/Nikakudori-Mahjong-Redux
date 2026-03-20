/*
 * Copyright (c) 2026 Rekluz Games. All rights reserved.
 */

package com.rekluzgames.nikakudorimahjong.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rekluzgames.nikakudorimahjong.data.audio.SoundManager
import com.rekluzgames.nikakudorimahjong.data.repository.GameRepository
import com.rekluzgames.nikakudorimahjong.domain.engine.GameEngine
import com.rekluzgames.nikakudorimahjong.domain.model.*
import com.rekluzgames.nikakudorimahjong.domain.rules.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class GameViewModel(
    private val soundManager: SoundManager,
    private val repository: GameRepository
) : ViewModel() {

    private val engine = GameEngine()
    private val _uiState = MutableStateFlow(GameUIState())
    val uiState = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var generationJob: Job? = null
    private var stalemateCheckJob: Job? = null
    private var inactivityJob: Job? = null
    private var autoCompleteJob: Job? = null
    private var matchLineJob: Job? = null

    private var modeWasChanged = false

    init {
        loadSettingsAndScores()
        startNewGame(Difficulty.NORMAL)
    }

    // Called from MainActivity to inject the version from BuildConfig
    fun setVersion(version: String) {
        _uiState.update { it.copy(version = version) }
    }

    private fun loadSettingsAndScores() {
        val settings = repository.getSettings()
        val allScores = Difficulty.entries.associate { diff ->
            val parsed = settings.getHighScores(diff.label)
                .mapNotNull { HighScore.deserialise(it) }
                .sortedBy { it.time }
                .take(5)
            diff.label to parsed
        }
        _uiState.update {
            it.copy(
                isSoundEnabled = settings.isSoundEnabled(),
                isFullScreen = settings.isFullScreen(),
                gameMode = settings.getGameMode(),
                highScores = allScores
            )
        }
        soundManager.isEnabled = _uiState.value.isSoundEnabled
    }

    // --- INACTIVITY HINT TIMER ---
    private fun resetInactivityTimer() {
        inactivityJob?.cancel()
        inactivityJob = viewModelScope.launch {
            delay(10_000L)
            val state = _uiState.value
            if (state.gameState == GameState.PLAYING && state.allAvailableHints.isEmpty() && !state.canFinish) {
                val hints = HintFinder.findAllMatches(state.board)
                if (hints.isNotEmpty()) {
                    _uiState.update { it.copy(allAvailableHints = hints, currentHintIndex = 0) }
                }
            }
        }
    }

    // --- MATCH LINE ---
    private fun showMatchLine(path: List<Pair<Int, Int>>, p1: Pair<Int, Int>, p2: Pair<Int, Int>) {
        matchLineJob?.cancel()
        _uiState.update { it.copy(lastMatchPath = path, lastMatchedPair = p1 to p2) }
        matchLineJob = viewModelScope.launch {
            delay(400L)
            _uiState.update { it.copy(lastMatchPath = null, lastMatchedPair = null) }
        }
    }

    // --- VICTORY ---
    private fun handleWin() {
        timerJob?.cancel()
        inactivityJob?.cancel()
        matchLineJob?.cancel()
        soundManager.play("tile_tada")
        _uiState.update {
            it.copy(
                playerName = "",
                gameState = GameState.SCORE_ENTRY,
                lastMatchPath = null,
                lastMatchedPair = null
            )
        }
    }

    // --- AUTOCOMPLETE ---
    fun autoComplete() {
        val state = _uiState.value
        if (!state.canFinish) return

        timerJob?.cancel()
        inactivityJob?.cancel()
        matchLineJob?.cancel()
        autoCompleteJob?.cancel()

        autoCompleteJob = viewModelScope.launch {
            var currentBoard = _uiState.value.board
            val pairsToPlay = buildSequence(currentBoard)

            for ((p1, p2) in pairsToPlay) {
                if (!isActive) break

                val path = PathFinder.getPath(p1, p2, currentBoard) ?: listOf(p1, p2)
                _uiState.update {
                    it.copy(
                        selectedTile = p1,
                        allAvailableHints = listOf(p1 to p2),
                        currentHintIndex = 0,
                        lastMatchPath = path,
                        lastMatchedPair = p1 to p2
                    )
                }
                delay(250L)

                val snapshot = currentBoard
                currentBoard = currentBoard.mapIndexed { r, row ->
                    row.mapIndexed { c, tile ->
                        if ((r == p1.first && c == p1.second) || (r == p2.first && c == p2.second)) {
                            tile.copy(isRemoved = true)
                        } else tile
                    }
                }

                _uiState.update {
                    it.copy(
                        board = currentBoard,
                        selectedTile = null,
                        allAvailableHints = emptyList(),
                        currentHintIndex = -1,
                        lastMatchPath = null,
                        lastMatchedPair = null,
                        undoHistory = it.undoHistory + listOf(snapshot)
                    )
                }
                delay(250L)
            }

            handleWin()
        }
    }

    private fun buildSequence(startBoard: List<List<Tile>>): List<Pair<Pair<Int, Int>, Pair<Int, Int>>> {
        val sequence = mutableListOf<Pair<Pair<Int, Int>, Pair<Int, Int>>>()
        var board = startBoard
        while (true) {
            val matches = HintFinder.findAllMatches(board)
            if (matches.isEmpty()) break
            val pick = matches.first()
            sequence.add(pick)
            board = board.mapIndexed { r, row ->
                row.mapIndexed { c, tile ->
                    if ((r == pick.first.first && c == pick.first.second) ||
                        (r == pick.second.first && c == pick.second.second)
                    ) tile.copy(isRemoved = true)
                    else tile
                }
            }
        }
        return sequence
    }

    // --- CORE LOGIC ---
    fun handleTileClick(r: Int, c: Int) {
        val state = _uiState.value
        if (autoCompleteJob?.isActive == true) return
        if (state.gameState != GameState.PLAYING || state.board[r][c].isRemoved) return

        resetInactivityTimer()

        if (state.selectedTile == null) {
            _uiState.update {
                it.copy(
                    selectedTile = r to c,
                    allAvailableHints = emptyList(),
                    currentHintIndex = -1
                )
            }
            soundManager.play("tile_click")
        } else {
            val p1 = state.selectedTile!!
            if (p1 == r to c) {
                _uiState.update { it.copy(selectedTile = null) }
            } else {
                val p2 = r to c
                val snapshot = state.board
                val path = PathFinder.getPath(p1, p2, state.board)
                var nextBoard = if (path != null) engine.attemptMatch(p1, p2, state.board) else null

                if (nextBoard != null) {
                    soundManager.play("tile_match")
                    showMatchLine(path!!, p1, p2)
                    if (state.gameMode == GameMode.GRAVITY) nextBoard = engine.applyGravity(nextBoard)

                    _uiState.update {
                        it.copy(
                            board = nextBoard,
                            selectedTile = null,
                            allAvailableHints = emptyList(),
                            currentHintIndex = -1,
                            undoHistory = it.undoHistory + listOf(snapshot)
                        )
                    }

                    if (engine.isGameOver(nextBoard)) {
                        viewModelScope.launch {
                            delay(400L)
                            handleWin()
                        }
                    } else {
                        checkForStalemate(nextBoard)
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
        if (state.gameState != GameState.PLAYING) return
        if (state.canFinish) {
            autoComplete()
            return
        }
        _uiState.update { it.copy(usedHint = true) }
        if (state.allAvailableHints.isEmpty()) {
            val hints = HintFinder.findAllMatches(state.board)
            if (hints.isNotEmpty()) {
                _uiState.update { it.copy(allAvailableHints = hints, currentHintIndex = 0) }
            } else {
                changeState(GameState.NO_MOVES)
            }
        } else {
            val nextIndex = (state.currentHintIndex + 1) % state.allAvailableHints.size
            _uiState.update { it.copy(currentHintIndex = nextIndex) }
        }
    }

    fun startNewGame(diff: Difficulty) {
        timerJob?.cancel()
        generationJob?.cancel()
        stalemateCheckJob?.cancel()
        inactivityJob?.cancel()
        autoCompleteJob?.cancel()
        matchLineJob?.cancel()
        modeWasChanged = false
        _uiState.update {
            it.copy(
                gameState = GameState.LOADING,
                difficulty = diff,
                undoHistory = emptyList(),
                playerName = "",
                allAvailableHints = emptyList(),
                currentHintIndex = -1,
                lastMatchPath = null,
                lastMatchedPair = null,
                usedHint = false,
                usedShuffle = false,
                lastSavedScore = null
            )
        }
        generationJob = viewModelScope.launch(Dispatchers.Default) {
            val board = BoardGenerator.createBoard(diff)
            _uiState.update {
                it.copy(
                    board = board,
                    originalBoard = board,
                    gameState = GameState.PLAYING,
                    timeSeconds = 0,
                    shufflesRemaining = diff.shuffles
                )
            }
            startTimer()
            resetInactivityTimer()
        }
    }

    fun retryGame() {
        timerJob?.cancel()
        stalemateCheckJob?.cancel()
        inactivityJob?.cancel()
        autoCompleteJob?.cancel()
        matchLineJob?.cancel()

        val originalBoard = _uiState.value.originalBoard
        _uiState.update {
            it.copy(
                board = originalBoard,
                gameState = GameState.PLAYING,
                timeSeconds = 0,
                shufflesRemaining = it.difficulty.shuffles,
                undoHistory = emptyList(),
                selectedTile = null,
                allAvailableHints = emptyList(),
                currentHintIndex = -1,
                lastMatchPath = null,
                lastMatchedPair = null,
                usedHint = false,
                usedShuffle = false,
                playerName = ""
            )
        }
        startTimer()
        resetInactivityTimer()
    }

    fun undo() {
        val state = _uiState.value
        if (state.undoHistory.isEmpty()) return
        val previousBoard = state.undoHistory.last()
        _uiState.update {
            it.copy(
                board = previousBoard,
                undoHistory = it.undoHistory.dropLast(1),
                selectedTile = null,
                allAvailableHints = emptyList(),
                currentHintIndex = -1,
                gameState = GameState.PLAYING,
                lastMatchPath = null,
                lastMatchedPair = null
            )
        }
        resetInactivityTimer()
        checkForStalemate(previousBoard)
    }

    fun shuffle() {
        val state = _uiState.value
        if (state.shufflesRemaining <= 0) return
        _uiState.update { it.copy(usedShuffle = true) }
        val activeTypes = state.board.flatten().filter { !it.isRemoved }.map { it.type }.shuffled()
        var idx = 0
        val newBoard = state.board.map { row ->
            row.map { if (!it.isRemoved) it.copy(type = activeTypes[idx++]) else it }
        }
        _uiState.update {
            it.copy(
                board = newBoard,
                shufflesRemaining = it.shufflesRemaining - 1,
                selectedTile = null,
                allAvailableHints = emptyList(),
                currentHintIndex = -1,
                gameState = GameState.PLAYING,
                lastMatchPath = null,
                lastMatchedPair = null
            )
        }
        resetInactivityTimer()
        checkForStalemate(newBoard)
    }

    // --- SCOREBOARD ---
    fun saveScoreAndShowBoard() {
        val state = _uiState.value
        val finalName = if (state.playerName.isBlank()) "???" else state.playerName
        val diffLabel = state.difficulty.label
        val medals = state.earnedMedals

        val newScore = HighScore(finalName, state.timeSeconds, diffLabel, medals)
        val settings = repository.getSettings()
        val existing = settings.getHighScores(diffLabel).toMutableSet()
        existing.add(newScore.serialise())

        val trimmed = existing
            .mapNotNull { HighScore.deserialise(it) }
            .sortedBy { it.time }
            .take(5)
            .map { it.serialise() }
            .toSet()

        settings.saveHighScores(diffLabel, trimmed)
        loadSettingsAndScores()
        _uiState.update {
            it.copy(
                selectedScoreTab = diffLabel,
                lastSavedScore = newScore
            )
        }
        changeState(GameState.SCORE)
    }

    fun clearScores(difficulty: String) {
        repository.getSettings().clearHighScores(difficulty)
        loadSettingsAndScores()
    }

    fun selectScoreTab(tab: String) {
        _uiState.update { it.copy(selectedScoreTab = tab) }
    }

    fun clearLastSavedScore() {
        _uiState.update { it.copy(lastSavedScore = null) }
    }

    fun updatePlayerName(name: String) {
        if (name.length <= 3) _uiState.update { it.copy(playerName = name.uppercase()) }
    }

    // --- SETTINGS ---
    fun toggleFullScreen() {
        val next = !_uiState.value.isFullScreen
        repository.getSettings().setFullScreen(next)
        _uiState.update { it.copy(isFullScreen = next) }
    }

    fun toggleGameMode() {
        val currentMode = _uiState.value.gameMode
        val newMode = if (currentMode == GameMode.REGULAR) GameMode.GRAVITY else GameMode.REGULAR
        repository.getSettings().setGameMode(newMode)
        _uiState.update { it.copy(gameMode = newMode) }
        modeWasChanged = true
    }

    fun updateSoundEnabled(enabled: Boolean) {
        repository.getSettings().setSoundEnabled(enabled)
        soundManager.isEnabled = enabled
        _uiState.update { it.copy(isSoundEnabled = enabled) }
    }

    fun applySettingsAndResume() {
        if (modeWasChanged) startNewGame(_uiState.value.difficulty)
        else changeState(GameState.PLAYING)
    }

    fun changeState(s: GameState) {
        _uiState.update { it.copy(gameState = s) }
    }

    // --- ABOUT / EASTER EGG ---
    fun onAboutTileClick(id: Int, target: Int) {
        _uiState.update { state ->
            val newSet = state.clearedAboutTiles + id
            if (newSet.size >= target) {
                soundManager.play("secret_unlocked")
                state.copy(aboutStage = 1, clearedAboutTiles = emptySet())
            } else state.copy(clearedAboutTiles = newSet)
        }
    }

    fun resetAbout() {
        _uiState.update { it.copy(aboutStage = 0, clearedAboutTiles = emptySet()) }
    }

    // --- PRIVATE HELPERS ---
    private fun checkForStalemate(board: List<List<Tile>>) {
        stalemateCheckJob?.cancel()
        stalemateCheckJob = viewModelScope.launch(Dispatchers.Default) {
            if (engine.isGameOver(board)) return@launch
            if (HintFinder.findAllMatches(board).isEmpty()) {
                withContext(Dispatchers.Main) { changeState(GameState.NO_MOVES) }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (_uiState.value.gameState == GameState.PLAYING) {
                    _uiState.update { it.copy(timeSeconds = it.timeSeconds + 1) }
                }
            }
        }
    }

    fun releaseResources() {
        timerJob?.cancel()
        generationJob?.cancel()
        stalemateCheckJob?.cancel()
        inactivityJob?.cancel()
        autoCompleteJob?.cancel()
        matchLineJob?.cancel()
        soundManager.release()
    }

    override fun onCleared() {
        super.onCleared()
        releaseResources()
    }
}