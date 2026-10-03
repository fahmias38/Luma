package com.pemmob.luma.ui.statistics

import kotlinx.coroutines.flow.Flow

interface StatisticsRepository {
    fun getStatistics(year: Int, month: Int): Flow<StatisticsData>
}
