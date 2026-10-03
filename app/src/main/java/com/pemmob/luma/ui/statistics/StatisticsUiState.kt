package com.pemmob.luma.ui.statistics

sealed interface StatisticsUiState {
    data object Loading : StatisticsUiState
    data class Success(val data: StatisticsData) : StatisticsUiState
    data class Error(val message: String) : StatisticsUiState
}

data class StatisticsData(
    val periodLabel: String,
    val totalIncome: Double,
    val totalExpense: Double,
    val topCategory: CategoryExpenseData?,
    val expenseByCategory: List<CategoryExpenseData>
)

data class CategoryExpenseData(
    val name: String,
    val amount: Double,
    val percentage: Int
)
