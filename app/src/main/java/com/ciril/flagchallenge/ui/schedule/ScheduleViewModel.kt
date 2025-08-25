package com.ciril.flagchallenge.ui.schedule

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ciril.flagchallenge.data.repository.ScheduleDataSource
import com.ciril.flagchallenge.domain.usecase.ScheduleChallengeUseCase
import com.ciril.flagchallenge.utils.AppClock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil


sealed class ScheduleUiState {
    data object Idle : ScheduleUiState()                       // nothing scheduled yet
    data class Waiting(val secondsUntilPrestart: Long) : ScheduleUiState() // Scheduled but yet to reach last 20 sec
    data class Prestart(val secondsLeft: Long) : ScheduleUiState() // last 20 sec window 20..1
    data object StartNow : ScheduleUiState()                   // reached the start time
}

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repo: ScheduleDataSource,
    private val scheduleUseCase: ScheduleChallengeUseCase,
    private val clock: AppClock
) : ViewModel() {

    private val _ui = MutableStateFlow<ScheduleUiState>(ScheduleUiState.Idle)
    val ui: StateFlow<ScheduleUiState> = _ui.asStateFlow()

    private var tickerJob: Job? = null
    private var scheduledAtCache: Long? = null

    init {
        // Collect the stored target time and restart a 1s ticker whenever it changes
        viewModelScope.launch {
            repo.scheduledAt().collect { ts ->
                scheduledAtCache = ts
                restartTickerIfNeeded()
            }
        }
    }

    /***
     * To add up the entered time by user to current time and save it on pref by the SAVE button click*/
    fun saveSchedule(hours: Int, minutes: Int, seconds: Int) {
        val totalSeconds = scheduleUseCase(hours, minutes, seconds)
        val target = clock.now() + totalSeconds * 1000L
        viewModelScope.launch { repo.setScheduledAt(target) }
    }

    /***
     * Method to calculate the remaining time if scheduled
     * Based on the remaining time set the state accordingly*/
    private fun restartTickerIfNeeded() {
        tickerJob?.cancel()
        val target = scheduledAtCache ?: run { _ui.value = ScheduleUiState.Idle; return }
        tickerJob = viewModelScope.launch {
            while (true) {
                val remainingMs = target - clock.now()
                when {
                    remainingMs <= 0L -> {
                        _ui.value = ScheduleUiState.StartNow
                        repo.clearScheduledAt()
                        break
                    }
                    remainingMs <= 20_000L -> {
                        val sec = ceil(remainingMs / 1000.0).toLong() // ceil to show 20..1
                        _ui.value = ScheduleUiState.Prestart(sec)
                    }
                    else -> {
                        val secUntilPre = ceil((remainingMs - 20_000L) / 1000.0).toLong()
                        _ui.value = ScheduleUiState.Waiting(secUntilPre)
                    }
                }
                delay(1000)
            }
        }
    }

    @VisibleForTesting
    internal fun stopTicker() {
        tickerJob?.cancel()
    }
}