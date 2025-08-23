package com.ciril.flagchallenge.utils

object TimeFormatter { fun mmSs(sec: Int) = "%02d:%02d".format(sec/60, sec%60) }