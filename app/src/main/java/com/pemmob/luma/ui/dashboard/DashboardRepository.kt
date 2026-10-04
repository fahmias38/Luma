package com.pemmob.luma.ui.dashboard

import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun getDashboardData(filter: DashboardFilter = DashboardFilter.MONTH): Flow<DashboardData>
}
