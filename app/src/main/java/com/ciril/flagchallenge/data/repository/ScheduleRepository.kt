package com.ciril.flagchallenge.data.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.ciril.flagchallenge.data.scheduleDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ScheduleRepository @Inject constructor(@ApplicationContext private val context: Context) : ScheduleDataSource {
    private val KEY_SCHEDULED_AT = longPreferencesKey("scheduled_at")

    override suspend fun setScheduledAt(epochMillis: Long) {
        context.scheduleDataStore.edit { it[KEY_SCHEDULED_AT] = epochMillis }
    }

    override fun scheduledAt(): Flow<Long?> =
        context.scheduleDataStore.data.map { prefs: Preferences -> prefs[KEY_SCHEDULED_AT] }

    override suspend fun clearScheduledAt() {
        context.scheduleDataStore.edit { it.remove(KEY_SCHEDULED_AT) } // <-- new
    }
}