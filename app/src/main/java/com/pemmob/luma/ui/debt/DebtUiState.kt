package com.pemmob.luma.ui.debt

import com.pemmob.luma.data.local.entity.DebtReceivableEntity
import com.pemmob.luma.data.local.entity.PaymentEntity

sealed interface DebtListUiState {
    data object Loading : DebtListUiState
    data class Success(
        val allItems: List<DebtReceivableEntity>,
        val debtItems: List<DebtReceivableEntity>,
        val receivableItems: List<DebtReceivableEntity>,
        val totalDebt: Long = 0L,
        val totalReceivable: Long = 0L
    ) : DebtListUiState
    data class Error(val message: String) : DebtListUiState
}

sealed interface DebtDetailUiState {
    data object Loading : DebtDetailUiState
    data class Success(
        val debt: DebtReceivableEntity,
        val payments: List<PaymentEntity>,
        val remainingAmount: Long
    ) : DebtDetailUiState
    data class Error(val message: String) : DebtDetailUiState
}

sealed interface AddDebtUiState {
    data object Idle : AddDebtUiState
    data object Loading : AddDebtUiState
    data object Success : AddDebtUiState
    data class Error(val message: String) : AddDebtUiState
}

sealed interface AddPaymentUiState {
    data object Idle : AddPaymentUiState
    data object Loading : AddPaymentUiState
    data object Success : AddPaymentUiState
    data class Error(val message: String) : AddPaymentUiState
}

enum class DebtFilter {
    ALL, DEBT, RECEIVABLE
}
