package com.ciril.flagchallenge.ui.challenge

import com.ciril.flagchallenge.model.FlagQuestion


sealed class ChallengeState {
    data class Question(
        val index: Int,
        val remainingSec: Int,
        val question: FlagQuestion,
        val selectionId: Int?,
        val isLocked: Boolean
    ) : ChallengeState()

    data class Interval(
        val index: Int,
        val remainingSec: Int,
        val question: FlagQuestion,
        val selectionId: Int?,
        val isCorrect: Boolean
    ) : ChallengeState()

    data class Finished(val score: Int) : ChallengeState()
}