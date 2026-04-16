package com.rekluzgames.nikakudorimahjong.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.rekluzgames.nikakudorimahjong.data.audio.SoundManager
import com.rekluzgames.nikakudorimahjong.data.audio.MusicManager
import com.rekluzgames.nikakudorimahjong.data.haptic.HapticManager
import com.rekluzgames.nikakudorimahjong.domain.engine.GameEngine
import com.rekluzgames.nikakudorimahjong.domain.engine.GameSessionController
import com.rekluzgames.nikakudorimahjong.domain.engine.GameSessionStatePrep
import com.rekluzgames.nikakudorimahjong.domain.engine.LayeredGameEngine
import com.rekluzgames.nikakudorimahjong.domain.engine.PostMatchProcessor
import com.rekluzgames.nikakudorimahjong.domain.model.Difficulty
import com.rekluzgames.nikakudorimahjong.domain.model.GameState
import com.rekluzgames.nikakudorimahjong.presentation.score.ScoreManager
import com.rekluzgames.nikakudorimahjong.presentation.timer.GameTimer
import com.rekluzgames.nikakudorimahjong.presentation.usecase.ShuffleUseCase
import com.rekluzgames.nikakudorimahjong.presentation.usecase.HintUseCase
import com.rekluzgames.nikakudorimahjong.presentation.usecase.UndoUseCase
import com.rekluzgames.nikakudorimahjong.presentation.usecase.InteractionCoordinator
import com.rekluzgames.nikakudorimahjong.presentation.usecase.AutoCompleteUseCase
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import kotlinx.coroutines.flow.MutableStateFlow

class GameViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock lateinit var soundManager: SoundManager
    @Mock lateinit var musicManager: MusicManager
    @Mock lateinit var hapticManager: HapticManager
    @Mock lateinit var engine: GameEngine
    @Mock lateinit var layeredEngine: LayeredGameEngine
    @Mock lateinit var controller: GameSessionController
    @Mock lateinit var scoreManager: ScoreManager
    @Mock lateinit var gameTimer: GameTimer
    @Mock lateinit var postMatchProcessor: PostMatchProcessor
    @Mock lateinit var interactionCoordinator: InteractionCoordinator
    @Mock lateinit var autoCompleteUseCase: AutoCompleteUseCase

    private lateinit var viewModel: GameViewModel
    private lateinit var shuffleUseCase: ShuffleUseCase
    private lateinit var hintUseCase: HintUseCase
    private lateinit var undoUseCase: UndoUseCase

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        shuffleUseCase = ShuffleUseCase(layeredEngine)
        hintUseCase = HintUseCase(layeredEngine)
        undoUseCase = UndoUseCase()

        whenever(scoreManager.getAllHighScores()).thenReturn(emptyMap())
        whenever(gameTimer.timeSeconds).thenReturn(MutableStateFlow(0))
        whenever(controller.prepareNewGameState("bg_001", false)).thenReturn(
            GameSessionStatePrep(
                backgroundImageName = "bg_1",
                currentQuote = "Test Quote"
            )
        )

        viewModel = GameViewModel(
            controller = controller,
            soundManager = soundManager,
            musicManager = musicManager,
            hapticManager = hapticManager,
            engine = engine,
            layeredEngine = layeredEngine,
            scoreManager = scoreManager,
            gameTimer = gameTimer,
            postMatchProcessor = postMatchProcessor,
            shuffleUseCase = shuffleUseCase,
            hintUseCase = hintUseCase,
            undoUseCase = undoUseCase,
            interactionCoordinator = interactionCoordinator,
            autoCompleteUseCase = autoCompleteUseCase
        )
    }

    @Test
    fun testPlayerNameSanitizationTrimmed() {
        viewModel.updatePlayerName("  ABC  ")
        val state = viewModel.uiState.value
        assertEquals("ABC", state.playerName)
    }

    @Test
    fun testPlayerNameCapAtThreeCharacters() {
        viewModel.updatePlayerName("TOOLONG")
        val state = viewModel.uiState.value
        assertEquals(3, state.playerName.length)
        assertEquals("TOO", state.playerName)
    }

    @Test
    fun testPlayerNameEmptyAfterTrimming() {
        viewModel.updatePlayerName("   ")
        val state = viewModel.uiState.value
        assertEquals("", state.playerName)
    }

    @Test
    fun testChangeStateUpdateGameState() {
        viewModel.changeState(GameState.PAUSED)
        val state = viewModel.uiState.value
        assertEquals(GameState.PAUSED, state.gameState)
    }

    @Test
    fun testSelectScoreTabUpdatesTab() {
        viewModel.selectScoreTab("HARD")
        val state = viewModel.uiState.value
        assertEquals("HARD", state.selectedScoreTab)
    }

    @Test
    fun testClearLastSavedScoreSetsToNull() {
        viewModel.clearLastSavedScore()
        val state = viewModel.uiState.value
        assertNull(state.lastSavedScore)
    }

    @Test
    fun testGoBackFromPausedReturnsToPlaying() {
        viewModel.changeState(GameState.PAUSED)
        viewModel.goBack()
        val state = viewModel.uiState.value
        assertEquals(GameState.PLAYING, state.gameState)
    }

    @Test
    fun testGoBackFromOptionsReturnsToPrevious() {
        viewModel.changeState(GameState.OPTIONS)
        val previousState = viewModel.uiState.value.previousState
        viewModel.goBack()
        val newState = viewModel.uiState.value
        assertEquals(previousState, newState.gameState)
    }
}