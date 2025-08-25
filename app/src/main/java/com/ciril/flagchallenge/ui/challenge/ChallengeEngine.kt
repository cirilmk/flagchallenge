package com.ciril.flagchallenge.ui.challenge

import com.ciril.flagchallenge.model.FlagQuestion
import com.ciril.flagchallenge.utils.ChallengeConfig
import kotlin.math.min

class ChallengeEngine(
    questionsAll: List<FlagQuestion>,
    private val startAtMillis: Long
) {
    private val cap = min(ChallengeConfig.TOTAL_QUESTIONS, questionsAll.size)
    private val questions = questionsAll.take(cap)
    private val selections = IntArray(cap) { -1 }

    fun selectOption(questionIndex: Int, countryId: Int) {
        if (questionIndex in 0 until cap) selections[questionIndex] = countryId
    }

    fun derive(nowMillis: Long): ChallengeState {
        if (startAtMillis <= 0L || questions.isEmpty()) {
            return ChallengeState.Finished(score())
        }

        val qSec = ChallengeConfig.QUESTION_SEC
        val iSec = ChallengeConfig.INTERVAL_SEC
        val perQ = qSec + iSec

        // no interval after last question
        val totalTimeline = if (cap <= 0) 0
        else (cap - 1) * perQ + qSec

        val elapsedSec = ((nowMillis - startAtMillis) / 1000L).toInt().coerceAtLeast(0)
        if (elapsedSec >= totalTimeline) return ChallengeState.Finished(score())

        val qIndex = (elapsedSec / perQ).coerceIn(0, cap - 1)
        val tInBlock = elapsedSec % perQ
        val q = questions[qIndex]

        return if (tInBlock < qSec) {
            val remaining = qSec - tInBlock
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

    private fun score(): Int = (0 until cap).count { selections[it] == questions[it].answer_id }
}