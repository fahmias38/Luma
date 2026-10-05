package com.pemmob.luma.ui.debt

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pemmob.luma.data.local.entity.DebtReceivableEntity
import com.pemmob.luma.domain.repository.AuthRepository
import com.pemmob.luma.domain.repository.DebtReceivableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebtViewModel @Inject constructor(
    private val debtRepository: DebtReceivableRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // ===== LIST STATE =====
    private val _listUiState = MutableStateFlow<DebtListUiState>(DebtListUiState.Loading)
    val listUiState: StateFlow<DebtListUiState> = _listUiState.asStateFlow()

    private val _currentFilter = MutableStateFlow(DebtFilter.ALL)
    val currentFilter: StateFlow<DebtFilter> = _currentFilter.asStateFlow()

    private val _statusFilter = MutableStateFlow(DebtStatusFilter.ALL)
    val statusFilter: StateFlow<DebtStatusFilter> = _statusFilter.asStateFlow()

    // ===== DETAIL STATE =====
    private val _detailUiState = MutableStateFlow<DebtDetailUiState>(DebtDetailUiState.Loading)
    val detailUiState: StateFlow<DebtDetailUiState> = _detailUiState.asStateFlow()

    private val _selectedDebtId = MutableStateFlow<String?>(null)

    // ===== ADD DEBT STATE =====
    private val _addDebtUiState = MutableStateFlow<AddDebtUiState>(AddDebtUiState.Idle)
    val addDebtUiState: StateFlow<AddDebtUiState> = _addDebtUiState.asStateFlow()

    // ===== ADD PAYMENT STATE =====
    private val _addPaymentUiState = MutableStateFlow<AddPaymentUiState>(AddPaymentUiState.Idle)
    val addPaymentUiState: StateFlow<AddPaymentUiState> = _addPaymentUiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadList()
    }

    // ===== LIST =====

    fun setFilter(filter: DebtFilter) {
        _currentFilter.value = filter
    }

    fun setStatusFilter(filter: DebtStatusFilter) {
        _statusFilter.value = filter
    }

    fun loadList() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _listUiState.value = DebtListUiState.Loading
            try {
                val currentUser = authRepository.getCurrentUser()
                val userId = currentUser?.id

                if (userId.isNullOrBlank()) {
                    Log.w("DebtVM", "userId null/kosong saat loadList()")
                    _listUiState.value = DebtListUiState.Success(
                        allItems = emptyList(),
                        debtItems = emptyList(),
                        receivableItems = emptyList(),
                        totalDebt = 0L,
                        totalReceivable = 0L
                    )
                    return@launch
                }

                // Sinkronisasi background dari Supabase ke Room DB
                launch {
                    runCatching {
                        debtRepository.syncRemoteDebts(userId)
                    }.onFailure { e ->
                        Log.e("DebtVM", "Error syncRemoteDebts: ${e.message}", e)
                    }
                }

                // Observe Flow langsung dari Room DB secara tunggal (single collector)
                debtRepository.observeAll(userId)
                    .catch { e ->
                        Log.e("DebtVM", "Error observeAll: ${e.message}", e)
                        _listUiState.value = DebtListUiState.Error(
                            e.message ?: "Gagal memuat data utang/piutang."
                        )
                    }
                    .collect { items ->
                        val debts = items.filter { it.type == "DEBT" }
                        val receivables = items.filter { it.type == "RECEIVABLE" }
                        _listUiState.value = DebtListUiState.Success(
                            allItems = items,
                            debtItems = debts,
                            receivableItems = receivables,
                            totalDebt = debts.sumOf { it.amount - it.paidAmount },
                            totalReceivable = receivables.sumOf { it.amount - it.paidAmount }
                        )
                    }
            } catch (e: Exception) {
                Log.e("DebtVM", "Error loadList: ${e.message}", e)
                _listUiState.value = DebtListUiState.Error(
                    e.message ?: "Gagal memuat data utang/piutang."
                )
            }
        }
    }

    // ===== DETAIL =====

    fun loadDetail(debtId: String) {
        _selectedDebtId.value = debtId
        viewModelScope.launch {
            _detailUiState.value = DebtDetailUiState.Loading
            try {
                kotlinx.coroutines.flow.combine(
                    debtRepository.observeById(debtId),
                    debtRepository.observePayments(debtId)
                ) { debt, payments ->
                    if (debt == null) {
                        DebtDetailUiState.Error("Data tidak ditemukan.")
                    } else {
                        val remaining = (debt.amount - debt.paidAmount).coerceAtLeast(0L)
                        DebtDetailUiState.Success(
                            debt = debt,
                            payments = payments,
                            remainingAmount = remaining
                        )
                    }
                }
                    .catch { e ->
                        _detailUiState.value = DebtDetailUiState.Error(
                            e.message ?: "Gagal memuat detail."
                        )
                    }
                    .collect { state ->
                        _detailUiState.value = state
                    }
            } catch (e: Exception) {
                _detailUiState.value = DebtDetailUiState.Error(
                    e.message ?: "Gagal memuat detail."
                )
            }
        }
    }

    // ===== ADD DEBT =====

    fun addDebt(
        personName: String,
        type: String,
        amount: Long,
        description: String,
        date: Long
    ) {
        viewModelScope.launch {
            _addDebtUiState.value = AddDebtUiState.Loading
            try {
                val currentUser = authRepository.getCurrentUser()
                val userId = currentUser?.id
                if (userId.isNullOrBlank()) {
                    Log.e("DebtVM", "Gagal menambah utang/piutang: userId null/kosong!")
                    _addDebtUiState.value = AddDebtUiState.Error("Sesi pengguna tidak terdeteksi. Silakan login kembali.")
                    return@launch
                }
                val entity = DebtReceivableEntity(
                    userId = userId,
                    personName = personName.trim(),
                    type = type,
                    amount = amount,
                    description = description.trim(),
                    date = date,
                    source = "MANUAL"
                )
                debtRepository.insertDebt(entity)
                    .onSuccess {
                        Log.d("DebtVM", "Berhasil menambah utang/piutang: ${entity.id}")
                        _addDebtUiState.value = AddDebtUiState.Success
                    }
                    .onFailure { e ->
                        Log.e("DebtVM", "Gagal menyimpan utang/piutang: ${e.message}", e)
                        _addDebtUiState.value = AddDebtUiState.Error(
                            e.message ?: "Gagal menyimpan data."
                        )
                    }
            } catch (e: Exception) {
                Log.e("DebtVM", "Error addDebt: ${e.message}", e)
                _addDebtUiState.value = AddDebtUiState.Error(
                    e.message ?: "Terjadi kesalahan."
                )
            }
        }
    }

    fun resetAddDebtState() {
        _addDebtUiState.value = AddDebtUiState.Idle
    }

    // ===== PAYMENT =====

    fun addPayment(
        debtId: String,
        amount: Long,
        paymentDate: Long,
        note: String
    ) {
        viewModelScope.launch {
            _addPaymentUiState.value = AddPaymentUiState.Loading
            debtRepository.addPayment(
                debtId = debtId,
                amount = amount,
                paymentDate = paymentDate,
                note = note
            )
                .onSuccess { _addPaymentUiState.value = AddPaymentUiState.Success }
                .onFailure { e ->
                    _addPaymentUiState.value = AddPaymentUiState.Error(
                        e.message ?: "Gagal mencatat pembayaran."
                    )
                }
        }
    }

    fun resetPaymentState() {
        _addPaymentUiState.value = AddPaymentUiState.Idle
    }

    fun deleteDebt(debtId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                debtRepository.deleteDebt(debtId)
                    .onSuccess { onSuccess() }
            } catch (_: Exception) {}
        }
    }
}
