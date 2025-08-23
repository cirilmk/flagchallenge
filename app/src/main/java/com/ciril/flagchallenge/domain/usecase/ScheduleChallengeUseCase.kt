package com.ciril.flagchallenge.domain.usecase

class ScheduleChallengeUseCase {
    operator fun invoke(hours: Int, minutes: Int, seconds: Int): Int {
        val hh = hours.coerceAtLeast(0)
        val mm = minutes.coerceIn(0, 59)
        val ss = seconds.coerceIn(0, 59)
        return (hh * 3600) + (mm * 60) + ss
    }
}