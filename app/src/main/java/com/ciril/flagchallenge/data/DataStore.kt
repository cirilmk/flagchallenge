package com.ciril.flagchallenge.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.scheduleDataStore by preferencesDataStore("schedule_prefs")
