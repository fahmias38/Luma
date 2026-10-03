package com.pemmob.luma.ui.dashboard

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(val data: DashboardData) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

data class DashboardData(
    val userName: String,
    val monthYear: String,
    val balance: Double,
    val income: Double,
    val expense: Double,
    val debt: Double,
    val receivable: Double,
    val pendingDebtCount: Int,
    val pendingReceivableCount: Int,
    val topCategoryName: String,
    val topCategoryPercentage: Int,
    val recentTransactions: List<TransactionData>
)

data class TransactionData(
    val id: String,
    val title: String,
    val date: String,
    val category: String,
    val amount: Double,
    val isIncome: Boolean,
    val paymentMethod: String?
)
