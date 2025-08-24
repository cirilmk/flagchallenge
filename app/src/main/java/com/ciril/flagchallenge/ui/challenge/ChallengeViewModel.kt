package com.ciril.flagchallenge.ui.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ciril.flagchallenge.data.repository.ChallengeRepository
import com.ciril.flagchallenge.data.repository.ScheduleDataSource
import com.ciril.flagchallenge.model.FlagQuestion
import com.ciril.flagchallenge.utils.AppClock
import com.ciril.flagchallenge.utils.ChallengeConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ChallengeViewModel @Inject constructor(
    private val questionsRepo: ChallengeRepository,
    private val scheduleRepo: ScheduleDataSource,
    private val clock: AppClock,
    private val dispatcher: CoroutineDispatcher // Provide Main.immediate via Hilt
) : ViewModel() {

    private val _ui = MutableStateFlow<ChallengeState?>(null)
    val ui: StateFlow<ChallengeState?> = _ui

    private var questions: List<FlagQuestion> = emptyList()
    private var engine: ChallengeEngine? = null
    private var tickerJob: Job? = null

    init {
        // Load questions once, then respond to schedule changes.
        viewModelScope.launch(dispatcher) {
            questions = questionsRepo.loadQuestions().take(ChallengeConfig.TOTAL_QUESTIONS)

            scheduleRepo.scheduledAt()
                .distinctUntilChanged()
                .collect { startAt ->
                    tickerJob?.cancel()
                    engine = null

                    if (startAt == null || startAt <= 0L || questions.isEmpty()) {
                        _ui.value = null
                        return@collect
                    }
                    startTicker(startAt)
                }
        }
    }

    private fun startTicker(startMillis: Long) {
        engine = ChallengeEngine(questions, startMillis)
        tickerJob = viewModelScope.launch(dispatcher) {
            // Emit immediately (no initial delay).
            _ui.value = engine!!.derive(clock.now())
            while (isActive) {
                delay(1_000)
                _ui.value = engine!!.derive(clock.now())
            }
        }
    }

    fun selectOption(questionIndex: Int, countryId: Int) {
        val eng = engine ?: return
        val current = _ui.value
        if (current is ChallengeState.Question && !current.isLocked && current.index == questionIndex) {
            eng.selectOption(questionIndex, countryId)
            // Reflect immediately (don’t wait for next tick)
            _ui.value = eng.derive(clock.now())
        }
    }
}