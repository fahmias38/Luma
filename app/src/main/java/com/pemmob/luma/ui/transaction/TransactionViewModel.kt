package com.pemmob.luma.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pemmob.luma.data.local.entity.TransactionEntity
import com.pemmob.luma.domain.repository.AuthRepository
import com.pemmob.luma.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TransactionUiState>(TransactionUiState.Loading)
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    private val _currentFilter = MutableStateFlow(TransactionFilter.ALL)
    val currentFilter: StateFlow<TransactionFilter> = _currentFilter.asStateFlow()

    init {
        loadTransactions()
    }

    fun setFilter(filter: TransactionFilter) {
        _currentFilter.value = filter
        loadTransactions()
    }

    fun loadTransactions() {
        viewModelScope.launch {
            _uiState.value = TransactionUiState.Loading
            try {
                val currentUser = authRepository.getCurrentUser()
                val userId = currentUser?.id ?: "local_test_user_id"

                transactionRepository.getAllTransactions(userId)
                    .catch { e ->
                        _uiState.value = TransactionUiState.Error(e.message ?: "Terjadi kesalahan saat memuat transaksi.")
                    }
                    .collect { allTransactions ->
                        val totalIncome = allTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
                        val totalExpense = allTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                        val balance = totalIncome - totalExpense

                        val filteredTransactions = when (_currentFilter.value) {
                            TransactionFilter.ALL -> allTransactions
                            TransactionFilter.INCOME -> allTransactions.filter { it.type == "INCOME" }
                            TransactionFilter.EXPENSE -> allTransactions.filter { it.type == "EXPENSE" }
                        }

                        _uiState.value = TransactionUiState.Success(
                            transactions = filteredTransactions,
                            totalIncome = totalIncome,
                            totalExpense = totalExpense,
                            balance = balance
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = TransactionUiState.Error(e.message ?: "Gagal memuat data transaksi.")
            }
        }
    }

    fun addTransaction(
        type: String,
        amount: Long,
        category: String,
        wallet: String,
        note: String,
        date: Long
    ) {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                val userId = currentUser?.id ?: "local_test_user_id"
                val newTransaction = TransactionEntity(
                    userId = userId,
                    type = type,
                    amount = amount,
                    category = category,
                    wallet = wallet,
                    note = note,
                    date = date
                )
                transactionRepository.insertTransaction(newTransaction)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateTransaction(
        transaction: TransactionEntity,
        type: String,
        amount: Long,
        category: String,
        wallet: String,
        note: String,
        date: Long
    ) {
        viewModelScope.launch {
            try {
                val updated = transaction.copy(
                    type = type,
                    amount = amount,
                    category = category,
                    wallet = wallet,
                    note = note,
                    date = date
                )
                transactionRepository.updateTransaction(updated)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            try {
                transactionRepository.deleteTransaction(transaction)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
