package com.pemmob.luma.ui.transaction

import com.pemmob.luma.data.local.entity.TransactionEntity

sealed interface TransactionUiState {
    data object Loading : TransactionUiState
    data class Success(
        val transactions: List<TransactionEntity>,
        val totalIncome: Long = 0L,
        val totalExpense: Long = 0L,
        val balance: Long = 0L
    ) : TransactionUiState
    data class Error(val message: String) : TransactionUiState
}

enum class TransactionFilter {
    ALL, INCOME, EXPENSE
}
