package com.ciril.flagchallenge.ui.schedule

import com.ciril.flagchallenge.data.repository.ScheduleDataSource
import com.ciril.flagchallenge.domain.usecase.ScheduleChallengeUseCase
import com.ciril.flagchallenge.utils.AppClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test


private class FakeRepo : ScheduleDataSource {
    val flow = MutableStateFlow<Long?>(null)
    override suspend fun setScheduledAt(epochMillis: Long) { flow.value = epochMillis }
    override fun scheduledAt() = flow
}

private class FakeClock(var nowMs: Long) : AppClock {
    override fun now(): Long = nowMs
}

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeRepo
    private lateinit var clock: FakeClock
    private lateinit var vm: ScheduleViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeRepo()
        clock = FakeClock(nowMs = 1_000_000L) // fixed "now"
        vm = ScheduleViewModel(
            repo = repo,
            scheduleUseCase = ScheduleChallengeUseCase(),
            clock = clock
        )
    }

    @After
    fun tearDown() {
        // ensure ticker job is cancelled so runTest can finish
        vm.stopTicker()
        Dispatchers.resetMain()
    }

    @Test
    fun `idle when nothing scheduled`() = runTest(dispatcher) {
        // VM collects repo(null) in init; first state is Idle
        // No need to advance time; first tick sets immediately
        assertThat(vm.ui.value).isInstanceOf(ScheduleUiState.Idle::class.java)
    }

    @Test
    fun `start now when duration is zero`() = runTest(dispatcher) {
        // Sets scheduledAt = now; remaining <= 0 -> StartNow
        vm.saveSchedule(0, 0, 0)

        // Process pending coroutines once
        dispatcher.scheduler.runCurrent()

        assertThat(vm.ui.value).isInstanceOf(ScheduleUiState.StartNow::class.java)
    }

    @Test
    fun `prestart shown immediately for 15 seconds`() = runTest(dispatcher) {
        try {
            // 15s total is within prestart window (<= 20s)
            vm.saveSchedule(0, 0, 15)

            // Let pending coroutines run once
            dispatcher.scheduler.runCurrent()

            val state = vm.ui.value
            assertThat(state).isInstanceOf(ScheduleUiState.Prestart::class.java)
            val pre = state as ScheduleUiState.Prestart
            // allow tiny rounding variance (ceil)
            assertThat(pre.secondsLeft).isAtLeast(14L)
            assertThat(pre.secondsLeft).isAtMost(15L)
        } finally {
            vm.stopTicker()                 // <-- ensure the infinite loop is cancelled
            dispatcher.scheduler.runCurrent()
        }
    }

    @Test
    fun `waiting state for long durations`() = runTest(dispatcher) {
        try {
            // 60s total -> not in prestart yet -> Waiting
            vm.saveSchedule(0, 1, 0)

            dispatcher.scheduler.runCurrent()

            val state = vm.ui.value
            assertThat(state).isInstanceOf(ScheduleUiState.Waiting::class.java)
            val waiting = state as ScheduleUiState.Waiting
            // ~40 seconds until prestart (60 - 20); allow ±1s for ceil/floor
            assertThat(waiting.secondsUntilPrestart).isAtLeast(39L)
            assertThat(waiting.secondsUntilPrestart).isAtMost(40L)
        } finally {
            vm.stopTicker()
            dispatcher.scheduler.runCurrent()
        }
    }
}