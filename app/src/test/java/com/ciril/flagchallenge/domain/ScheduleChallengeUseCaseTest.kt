package com.ciril.flagchallenge.domain

import com.ciril.flagchallenge.domain.usecase.ScheduleChallengeUseCase
import com.ciril.flagchallenge.utils.AppClock
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ScheduleChallengeUseCaseTest {
    private val useCase = ScheduleChallengeUseCase()


    private class FakeClock(var nowMs: Long) : AppClock {
        override fun now(): Long = nowMs
    }
    @Test fun converts_hms_to_seconds() {
        assertThat(useCase(1, 0, 0)).isEqualTo(3600)
        assertThat(useCase(0, 1, 0)).isEqualTo(60)
        assertThat(useCase(0, 0, 45)).isEqualTo(45)
        assertThat(useCase(1, 30, 15)).isEqualTo(5415)
    }

    @Test fun clamps_minutes_seconds() {
        assertThat(useCase(0, 99, 99)).isEqualTo(59 * 60 + 59)
        assertThat(useCase(-3, -5, -7)).isEqualTo(0)
    }
}