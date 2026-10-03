package com.pemmob.luma.ui.statistics

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class StatisticsModule {
    @Binds
    abstract fun bindStatisticsRepository(
        impl: StatisticsRepositoryImpl
    ): StatisticsRepository
}
