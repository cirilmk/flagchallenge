package com.ciril.flagchallenge.domain.usecase

class ScheduleChallengeUseCase {
    /**
     * Sanitize the values,
     * for now instead of validation, convert to 59 Sec/min
     * if entered values beyond that*/
    operator fun invoke(hours: Int, minutes: Int, seconds: Int): Int {
        val hh = hours.coerceAtLeast(0)
        val mm = minutes.coerceIn(0, 59)
        val ss = seconds.coerceIn(0, 59)
        return (hh * 3600) + (mm * 60) + ss
    }
}