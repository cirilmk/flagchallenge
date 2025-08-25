package com.ciril.flagchallenge.utils

import android.os.SystemClock

interface AppClock { fun now(): Long }

object ElapsedRealtimeClock : AppClock {
    override fun now(): Long = SystemClock.elapsedRealtime()
}