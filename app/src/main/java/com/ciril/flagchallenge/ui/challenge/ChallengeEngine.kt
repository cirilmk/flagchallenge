package com.ciril.flagchallenge.ui.challenge

import com.ciril.flagchallenge.model.FlagQuestion
import kotlin.math.min

private const val QUESTION_SEC = 5
private const val INTERVAL_SEC = 2
private const val TOTAL_QUESTIONS = 5

class ChallengeEngine(
    questionsAll: List<FlagQuestion>,
    private val startAtMillis: Long
) {
    private val capped = min(TOTAL_QUESTIONS, questionsAll.size)
    private val questions: List<FlagQuestion> = questionsAll.take(capped)

    // -1 = unanswered
    private val selections = IntArray(capped) { -1 }

    fun selectOption(questionIndex: Int, countryId: Int) {
        if (questionIndex in 0 until capped) {
            selections[questionIndex] = countryId
        }
    }

    fun derive(nowMillis: Long): ChallengeState {
        if (questions.isEmpty() || startAtMillis <= 0L) {
            return ChallengeState.Finished(score())
        }

        val elapsedSec = ((nowMillis - startAtMillis) / 1000L).coerceAtLeast(0L).toInt()
        val perQ = QUESTION_SEC + INTERVAL_SEC
        val totalTimeline = capped * perQ

        if (elapsedSec >= totalTimeline) {
            return ChallengeState.Finished(score())
        }

        val qIndex = (elapsedSec / perQ).coerceIn(0, capped - 1)
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

    private fun score(): Int {
        var s = 0
        for (i in 0 until capped) {
            if (selections[i] == questions[i].answer_id) s++
        }
        return s
    }
}