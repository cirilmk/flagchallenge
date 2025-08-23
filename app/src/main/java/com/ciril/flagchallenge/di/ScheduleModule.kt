package com.ciril.flagchallenge.di

import com.ciril.flagchallenge.domain.usecase.ScheduleChallengeUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object ScheduleModule {
    @Provides @Singleton
    fun provideScheduleUseCase() = ScheduleChallengeUseCase()

}