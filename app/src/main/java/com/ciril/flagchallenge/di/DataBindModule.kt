package com.ciril.flagchallenge.di

import com.ciril.flagchallenge.data.repository.AssetsChallengeRepository
import com.ciril.flagchallenge.data.repository.ChallengeRepository
import com.ciril.flagchallenge.data.repository.ScheduleDataSource
import com.ciril.flagchallenge.data.repository.ScheduleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindModule {

    @Binds
    @Singleton
    abstract fun bindScheduleDataSource(
        impl: ScheduleRepository
    ): ScheduleDataSource

    @Binds
    @Singleton
    abstract fun bindChallengeRepository(
        impl: AssetsChallengeRepository
    ): ChallengeRepository
}