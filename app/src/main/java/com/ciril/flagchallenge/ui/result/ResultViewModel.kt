package com.ciril.flagchallenge.ui.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ciril.flagchallenge.data.repository.ScheduleDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val scheduleRepo: ScheduleDataSource
) : ViewModel() {
    fun clearSchedule() = viewModelScope.launch {
        scheduleRepo.clearScheduledAt()
    }
}