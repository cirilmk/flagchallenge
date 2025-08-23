package com.ciril.flagchallenge.di

import com.ciril.flagchallenge.utils.AppClock
import com.ciril.flagchallenge.utils.SystemClock
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object TimeModule {
    @Provides @Singleton
    fun provideClock(): AppClock = SystemClock
}