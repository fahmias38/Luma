package com.pemmob.luma.ui.splitbill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pemmob.luma.domain.repository.AuthRepository
import com.pemmob.luma.domain.repository.SplitBillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplitBillViewModel @Inject constructor(
    private val splitBillRepository: SplitBillRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // ===== LIST STATE =====
    private val _listUiState = MutableStateFlow<SplitBillListUiState>(SplitBillListUiState.Loading)
    val listUiState: StateFlow<SplitBillListUiState> = _listUiState.asStateFlow()

    // ===== CREATE STATE =====
    private val _createUiState = MutableStateFlow<SplitBillCreateUiState>(SplitBillCreateUiState.Editing)
    val createUiState: StateFlow<SplitBillCreateUiState> = _createUiState.asStateFlow()

    // Form fields state
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _totalAmount = MutableStateFlow(0L)
    val totalAmount: StateFlow<Long> = _totalAmount.asStateFlow()

    private val _participants = MutableStateFlow<List<String>>(emptyList())
    val participants: StateFlow<List<String>> = _participants.asStateFlow()

    private val _payerName = MutableStateFlow("")
    val payerName: StateFlow<String> = _payerName.asStateFlow()

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    // ===== DETAIL STATE =====
    private val _detailUiState = MutableStateFlow<SplitBillDetailUiState>(SplitBillDetailUiState.Loading)
    val detailUiState: StateFlow<SplitBillDetailUiState> = _detailUiState.asStateFlow()

    // ===== VALIDATION ERRORS =====
    private val _validationError = MutableStateFlow<String?>(null)
    val validationError: StateFlow<String?> = _validationError.asStateFlow()

    init {
        loadList()
    }

    // ===== LIST =====

    fun loadList() {
        viewModelScope.launch {
            _listUiState.value = SplitBillListUiState.Loading
            try {
                val userId = authRepository.getCurrentUser()?.id ?: "local_test_user_id"
                splitBillRepository.observeAll(userId)
                    .catch { e ->
                        _listUiState.value = SplitBillListUiState.Error(
                            e.message ?: "Gagal memuat Split Bill."
                        )
                    }
                    .collect { bills ->
                        _listUiState.value = SplitBillListUiState.Success(bills)
                    }
            } catch (e: Exception) {
                _listUiState.value = SplitBillListUiState.Error(
                    e.message ?: "Gagal memuat Split Bill."
                )
            }
        }
    }

    // ===== FORM =====

    fun setTitle(value: String) { _title.value = value }
    fun setTotalAmount(value: Long) { _totalAmount.value = value }
    fun setPayerName(value: String) { _payerName.value = value }
    fun setDate(value: Long) { _selectedDate.value = value }

    fun addParticipant(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        if (_participants.value.any { it.equals(trimmed, ignoreCase = true) }) {
            _validationError.value = "Peserta \"$trimmed\" sudah ditambahkan."
            return
        }
        _participants.value = _participants.value + trimmed
        _validationError.value = null
    }

    fun removeParticipant(name: String) {
        _participants.value = _participants.value.filter { it != name }
        // Reset payer jika yang dihapus adalah payer
        if (_payerName.value == name) {
            _payerName.value = ""
        }
    }

    fun clearValidationError() {
        _validationError.value = null
    }

    // ===== CALCULATION =====

    /**
     * Memvalidasi input dan menghitung equal split.
     * Jika valid, state berubah ke Calculated dengan data preview.
     */
    fun calculate() {
        val titleVal = _title.value.trim()
        val amount = _totalAmount.value
        val parts = _participants.value
        val payer = _payerName.value.trim()

        when {
            titleVal.isBlank() -> {
                _validationError.value = "Judul Split Bill tidak boleh kosong."
                return
            }
            amount <= 0L -> {
                _validationError.value = "Total nominal harus lebih dari 0."
                return
            }
            parts.size < 2 -> {
                _validationError.value = "Minimal 2 peserta diperlukan."
                return
            }
            payer.isBlank() -> {
                _validationError.value = "Silakan pilih siapa yang membayar."
                return
            }
            payer !in parts -> {
                _validationError.value = "Payer harus merupakan peserta."
                return
            }
        }

        _validationError.value = null

        val count = parts.size
        val baseShare = amount / count
        val remainder = amount % count

        // Payer mendapat (baseShare + remainder) agar total konsisten
        val participantShares = parts.mapIndexed { index, name ->
            val share = if (name == payer) baseShare + remainder else baseShare
            ParticipantShare(
                name = name,
                shareAmount = share,
                isPayer = name == payer
            )
        }

        // Settlement: setiap non-payer berutang shareAmount kepada payer
        val settlements = participantShares
            .filter { !it.isPayer }
            .map { p ->
                SettlementItem(
                    from = p.name,
                    to = payer,
                    amount = p.shareAmount
                )
            }

        _createUiState.value = SplitBillCreateUiState.Calculated(
            SplitBillCalculation(
                title = titleVal,
                totalAmount = amount,
                payerName = payer,
                participants = participantShares,
                settlementItems = settlements
            )
        )
    }

    // ===== SAVE =====

    fun saveSplitBill() {
        val currentState = _createUiState.value
        if (currentState !is SplitBillCreateUiState.Calculated) return

        viewModelScope.launch {
            _createUiState.value = currentState.copy(isSaving = true, error = null)
            try {
                val userId = authRepository.getCurrentUser()?.id ?: "local_test_user_id"
                val calc = currentState.calculation
                splitBillRepository.createSplitBill(
                    userId = userId,
                    title = calc.title,
                    totalAmount = calc.totalAmount,
                    payerName = calc.payerName,
                    participantNames = calc.participants.map { it.name },
                    date = _selectedDate.value
                )
                    .onSuccess { id ->
                        _createUiState.value = SplitBillCreateUiState.Success(id)
                        resetForm()
                    }
                    .onFailure { e ->
                        _createUiState.value = currentState.copy(
                            isSaving = false,
                            error = e.message ?: "Gagal menyimpan Split Bill."
                        )
                    }
            } catch (e: Exception) {
                _createUiState.value = currentState.copy(
                    isSaving = false,
                    error = e.message ?: "Terjadi kesalahan."
                )
            }
        }
    }

    fun resetCreateState() {
        _createUiState.value = SplitBillCreateUiState.Editing
        _validationError.value = null
    }

    private fun resetForm() {
        _title.value = ""
        _totalAmount.value = 0L
        _participants.value = emptyList()
        _payerName.value = ""
        _selectedDate.value = System.currentTimeMillis()
    }

    // ===== DETAIL =====

    fun loadDetail(splitBillId: String) {
        viewModelScope.launch {
            _detailUiState.value = SplitBillDetailUiState.Loading
            try {
                kotlinx.coroutines.flow.combine(
                    splitBillRepository.observeById(splitBillId),
                    splitBillRepository.observeParticipants(splitBillId)
                ) { bill, participants ->
                    if (bill == null) {
                        SplitBillDetailUiState.Error("Split Bill tidak ditemukan.")
                    } else {
                        SplitBillDetailUiState.Success(
                            bill = bill,
                            participants = participants
                        )
                    }
                }
                    .catch { e ->
                        _detailUiState.value = SplitBillDetailUiState.Error(
                            e.message ?: "Gagal memuat detail Split Bill."
                        )
                    }
                    .collect { state ->
                        _detailUiState.value = state
                    }
            } catch (e: Exception) {
                _detailUiState.value = SplitBillDetailUiState.Error(
                    e.message ?: "Terjadi kesalahan."
                )
            }
        }
    }
}

