package com.ciril.flagchallenge.utils

interface AppClock { fun now(): Long }

object SystemClock : AppClock {
    override fun now(): Long = System.currentTimeMillis()
}