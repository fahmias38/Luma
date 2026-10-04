package com.pemmob.luma.ui.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DashboardRoute(
    onAddTransactionClick: () -> Unit,
    onSplitBillClick: () -> Unit,
    onDebtClick: () -> Unit,
    onSeeAllTransactionsClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onNotificationClick: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()

    DashboardScreen(
        uiState = uiState,
        currentFilter = filter,
        onFilterChange = { viewModel.refreshWithFilter(it) },
        onAddTransactionClick = onAddTransactionClick,
        onSplitBillClick = onSplitBillClick,
        onDebtClick = onDebtClick,
        onSeeAllTransactionsClick = onSeeAllTransactionsClick,
        onStatisticsClick = onStatisticsClick,
        onNotificationClick = onNotificationClick
    )
}
