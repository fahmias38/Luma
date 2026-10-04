package com.pemmob.luma.ui.dashboard

enum class DashboardFilter { MONTH, ALL }

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
    val topCategories: List<CategoryData>,         // Top 3 kategori pengeluaran
    val recentTransactions: List<TransactionData>,
    val hasNotification: Boolean = false
)

data class CategoryData(
    val name: String,
    val amount: Double,
    val percentage: Int
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
