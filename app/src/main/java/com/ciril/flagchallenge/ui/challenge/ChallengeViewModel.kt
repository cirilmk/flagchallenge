package com.ciril.flagchallenge.ui.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ciril.flagchallenge.data.repository.ChallengeRepository
import com.ciril.flagchallenge.data.repository.ScheduleDataSource
import com.ciril.flagchallenge.model.FlagQuestion
import com.ciril.flagchallenge.utils.AppClock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class ChallengeViewModel @Inject constructor(
    private val questionsRepo: ChallengeRepository,
    private val scheduleRepo: ScheduleDataSource,
    private val clock: AppClock
) : ViewModel() {

    private val _ui = MutableStateFlow<ChallengeState?>(null)
    val ui: StateFlow<ChallengeState?> = _ui

    private var questions: List<FlagQuestion> = emptyList()
    private val selections = IntArray(TOTAL_QUESTIONS) { -1 } // -1 = unanswered

    init {
        viewModelScope.launch {
            questions = questionsRepo.loadQuestions().take(TOTAL_QUESTIONS)
            // tick every second and derive state from wall-clock + scheduledAt
            combine(
                scheduleRepo.scheduledAt().map { it ?: 0L },
                _ui
            ) { startAt, _ -> startAt }
                .collect { startAt ->
                    if (startAt <= 0L || questions.isEmpty()) return@collect
                    // simple ticker loop
                    while (true) {
                        _ui.value = derive(startAt)
                        delay(1000)
                    }
                }
        }
    }

    private fun derive(startMillis: Long): ChallengeState {
        val now = clock.now()
        val elapsedSec = ((now - startMillis) / 1000L).toInt().coerceAtLeast(0)
        val perQ = QUESTION_SEC + INTERVAL_SEC
        val totalTimeline = TOTAL_QUESTIONS * perQ

        if (elapsedSec >= totalTimeline) {
            val score = (0 until min(TOTAL_QUESTIONS, questions.size))
                .count { selections[it] == questions[it].answer_id }
            return ChallengeState.Finished(score)
        }

        val qIndex = (elapsedSec / perQ).coerceIn(0, questions.lastIndex)
        val tInBlock = elapsedSec % perQ
        val q = questions[qIndex]

        return if (tInBlock < QUESTION_SEC) {
            val remaining = QUESTION_SEC - tInBlock
            ChallengeState.Question(
                index = qIndex,
                remainingSec = remaining,
                question = q,
                selectionId = selections[qIndex].takeIf { it != -1 },
                isLocked = remaining == 0
            )
        } else {
            val remaining = perQ - tInBlock
            val sel = selections[qIndex].takeIf { it != -1 }
            ChallengeState.Interval(
                index = qIndex,
                remainingSec = remaining,
                question = q,
                selectionId = sel,
                isCorrect = sel == q.answer_id
            )
        }
    }

    fun selectOption(questionIndex: Int, countryId: Int) {
        val s = _ui.value
        if (s is ChallengeState.Question && !s.isLocked && s.index == questionIndex) {
            selections[questionIndex] = countryId
        }
    }
}