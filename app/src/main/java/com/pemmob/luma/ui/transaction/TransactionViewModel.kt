package com.pemmob.luma.ui.transaction

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pemmob.luma.data.local.entity.TransactionEntity
import com.pemmob.luma.domain.repository.AuthRepository
import com.pemmob.luma.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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

    private var loadJob: Job? = null

    init {
        loadTransactions()
    }

    fun setFilter(filter: TransactionFilter) {
        _currentFilter.value = filter
        loadTransactions()
    }

    fun loadTransactions() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = TransactionUiState.Loading
            try {
                val currentUser = authRepository.getCurrentUser()
                val userId = currentUser?.id

                if (userId.isNullOrBlank()) {
                    Log.w("TransactionVM", "userId null atau kosong saat loadTransactions()")
                    _uiState.value = TransactionUiState.Success(
                        transactions = emptyList(),
                        totalIncome = 0L,
                        totalExpense = 0L,
                        balance = 0L
                    )
                    return@launch
                }

                // Sinkronisasi background dari Supabase ke Room DB
                launch {
                    runCatching {
                        transactionRepository.syncRemoteTransactions(userId)
                    }.onFailure { e ->
                        Log.e("TransactionVM", "Error syncRemoteTransactions: ${e.message}", e)
                    }
                }

                // Observe Flow langsung dari Room DB secara tunggal (single collector)
                transactionRepository.getAllTransactions(userId)
                    .catch { e ->
                        Log.e("TransactionVM", "Error getAllTransactions Flow: ${e.message}", e)
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
                Log.e("TransactionVM", "Error loadTransactions: ${e.message}", e)
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
                val userId = currentUser?.id
                if (userId.isNullOrBlank()) {
                    Log.e("TransactionVM", "Gagal menambah transaksi: userId null/kosong!")
                    _uiState.value = TransactionUiState.Error("Sesi pengguna tidak ditemukan. Silakan login kembali.")
                    return@launch
                }

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
                Log.d("TransactionVM", "Berhasil menambah transaksi lokal & cloud: ${newTransaction.id}")
            } catch (e: Exception) {
                Log.e("TransactionVM", "Gagal menambah transaksi: ${e.message}", e)
                _uiState.value = TransactionUiState.Error(e.message ?: "Gagal menambah transaksi.")
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
                Log.e("TransactionVM", "Gagal update transaksi: ${e.message}", e)
            }
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            try {
                transactionRepository.deleteTransaction(transaction)
            } catch (e: Exception) {
                Log.e("TransactionVM", "Gagal delete transaksi: ${e.message}", e)
            }
        }
    }
}
