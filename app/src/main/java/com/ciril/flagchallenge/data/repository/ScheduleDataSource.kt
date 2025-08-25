package com.ciril.flagchallenge.data.repository

import kotlinx.coroutines.flow.Flow

interface ScheduleDataSource {
    suspend fun setScheduledAt(epochMillis: Long)
    fun scheduledAt(): Flow<Long?>
    suspend fun clearScheduledAt()
}