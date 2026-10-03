package com.pemmob.luma.ui.dashboard

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DashboardModule {
    @Binds
    abstract fun bindDashboardRepository(
        impl: DummyDashboardRepository
    ): DashboardRepository
}
