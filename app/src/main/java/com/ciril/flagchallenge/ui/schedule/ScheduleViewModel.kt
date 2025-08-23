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


sealed class ScheduleUiState {
    data object Idle : ScheduleUiState()                       // nothing scheduled yet
    data class Waiting(val secondsUntilPrestart: Long) : ScheduleUiState()
    data class Prestart(val secondsLeft: Long) : ScheduleUiState() // 20..1
    data object StartNow : ScheduleUiState()                   // time to go!
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
        // Observe persisted scheduled time and (re)start ticker
        viewModelScope.launch {
            repo.scheduledAt().collect { ts ->
                scheduledAtCache = ts
                restartTickerIfNeeded()
            }
        }
    }

    fun saveSchedule(hours: Int, minutes: Int, seconds: Int) {
        val totalSeconds = scheduleUseCase(hours, minutes, seconds)
        val target = clock.now() + totalSeconds * 1000L
        viewModelScope.launch { repo.setScheduledAt(target) }
    }

    private fun restartTickerIfNeeded() {
        tickerJob?.cancel()
        val target = scheduledAtCache ?: run { _ui.value = ScheduleUiState.Idle; return }
        tickerJob = viewModelScope.launch {
            while (true) {
                val remainingMs = target - clock.now()
                when {
                    remainingMs <= 0L -> {
                        _ui.value = ScheduleUiState.StartNow
                        break
                    }
                    remainingMs <= 20_000L -> {
                        val sec = (remainingMs + 999) / 1000 // ceil to show 20..1
                        _ui.value = ScheduleUiState.Prestart(sec)
                    }
                    else -> {
                        val secUntilPre = ((remainingMs - 20_000L) + 999) / 1000
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