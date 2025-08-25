package com.ciril.flagchallenge.ui.challenge

import com.ciril.flagchallenge.model.Country
import com.ciril.flagchallenge.model.FlagQuestion
import org.junit.Test
import com.google.common.truth.Truth.assertThat


class ChallengeEngineTest {

    private fun sampleQuestions(count: Int = 2): List<FlagQuestion> {
        val q1 = FlagQuestion(
            answer_id = 1,
            country_code = "IN",
            countries = listOf(
                Country("India", 1),
                Country("USA", 2),
                Country("France", 3),
                Country("Japan", 4)
            )
        )
        val q2 = FlagQuestion(
            answer_id = 30,
            country_code = "FR",
            countries = listOf(
                Country("Italy", 25),
                Country("Spain", 26),
                Country("Germany", 27),
                Country("France", 30)
            )
        )
        return listOf(q1, q2).take(count)
    }

    @Test
    fun `loads first question when started 5s ago`() {
        val base = 1_000_000L
        val startAt = base - 5_000L
        val engine = ChallengeEngine(sampleQuestions(), startAt)

        val s = engine.derive(base)
        assertThat(s is ChallengeState.Question).isTrue()
        val q = s as ChallengeState.Question
        assertThat(q.index).isEqualTo(0)
        assertThat(q.question.country_code).isEqualTo("IN")
        // ~25s remaining (30 - 5) with integer math tolerance
        assertThat(q.remainingSec).isAtMost(30)
        assertThat(q.remainingSec).isAtLeast(20)
    }

    @Test
    fun `selection is reflected in question state`() {
        val now = 1_000_000L
        val engine = ChallengeEngine(sampleQuestions(), now - 1_000L)

        val before = engine.derive(now) as ChallengeState.Question
        engine.selectOption(before.index, 2)

        val after = engine.derive(now) as ChallengeState.Question
        assertThat(after.selectionId).isEqualTo(2)
        assertThat(after.index).isEqualTo(0)
    }

    @Test
    fun `after 30s moves to Interval`() {
        val base = 1_000_000L
        val startAt = base
        val engine = ChallengeEngine(sampleQuestions(), startAt)

        val s = engine.derive(base + 30_100L)
        assertThat(s is ChallengeState.Interval).isTrue()
        assertThat((s as ChallengeState.Interval).index).isEqualTo(0)
    }

    @Test
    fun `after 40s moves to next Question`() {
        val base = 1_000_000L
        val startAt = base
        val engine = ChallengeEngine(sampleQuestions(), startAt)

        val s = engine.derive(base + 40_100L) // 30s question + 10s interval
        assertThat(s is ChallengeState.Question).isTrue()
        val q = s as ChallengeState.Question
        assertThat(q.index).isEqualTo(1)
        assertThat(q.question.country_code).isEqualTo("FR")
    }

    @Test
    fun `finishes and reports score (single question wrong)`() {
        val base = 1_000_000L
        val engine = ChallengeEngine(sampleQuestions(count = 1), base)

        // choose wrong
        engine.selectOption(0, 2)

        val s = engine.derive(base + 40_100L) // 30s + 10s
        assertThat(s is ChallengeState.Finished).isTrue()
        assertThat((s as ChallengeState.Finished).score).isEqualTo(0)
    }

    @Test
    fun `finishes and reports score (single question correct)`() {
        val base = 1_000_000L
        val engine = ChallengeEngine(sampleQuestions(count = 1), base)

        // correct for q0 is id = 1
        engine.selectOption(0, 1)

        val s = engine.derive(base + 40_100L)
        assertThat(s is ChallengeState.Finished).isTrue()
        assertThat((s as ChallengeState.Finished).score).isEqualTo(1)
    }
}