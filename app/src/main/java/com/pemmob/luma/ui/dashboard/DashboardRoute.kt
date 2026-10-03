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
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardScreen(
        uiState = uiState,
        onAddTransactionClick = onAddTransactionClick,
        onSplitBillClick = onSplitBillClick,
        onDebtClick = onDebtClick,
        onSeeAllTransactionsClick = onSeeAllTransactionsClick
    )
}
