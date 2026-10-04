package com.pemmob.luma.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pemmob.luma.domain.repository.AuthRepository
import com.pemmob.luma.domain.repository.DebtReceivableRepository
import com.pemmob.luma.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository,
    private val debtReceivableRepository: DebtReceivableRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val user = authRepository.getCurrentUser()
            val userId = user?.id ?: "local_test_user_id"

            // Observe real-time stats from Transaction Repository
            launch {
                try {
                    transactionRepository.getAllTransactions(userId).collect { transactions ->
                        val totalTx = transactions.size
                        val earliestDate = transactions.minOfOrNull { it.date } ?: System.currentTimeMillis()
                        val diffMonths = ((System.currentTimeMillis() - earliestDate) / (1000L * 60 * 60 * 24 * 30)).toInt().coerceAtLeast(1)

                        _uiState.update { it.copy(totalTransactions = totalTx, activeMonths = diffMonths) }
                    }
                } catch (_: Exception) {}
            }

            // Observe real-time stats from Debt & Receivable Repository
            launch {
                try {
                    debtReceivableRepository.observeAll(userId).collect { debts ->
                        val total = debts.size
                        val paid = debts.count { it.status == "PAID" }
                        val percentage = if (total == 0) 100 else (paid * 100) / total

                        _uiState.update { it.copy(paidDebtPercentage = percentage) }
                    }
                } catch (_: Exception) {}
            }

            _uiState.update {
                it.copy(
                    user = user,
                    isLoading = false
                )
            }
        }
    }

    fun updateFullName(newFullName: String) {
        if (newFullName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nama lengkap tidak boleh kosong.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            authRepository.updateFullName(newFullName.trim())
                .onSuccess { updatedUser ->
                    _uiState.update {
                        it.copy(
                            user = updatedUser,
                            isLoading = false,
                            successMessage = "Nama lengkap berhasil diperbarui!"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Gagal memperbarui nama."
                        )
                    }
                }
        }
    }

    fun updateEmail(newEmail: String) {
        if (newEmail.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email tidak boleh kosong.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            authRepository.updateEmail(newEmail.trim())
                .onSuccess { updatedUser ->
                    _uiState.update {
                        it.copy(
                            user = updatedUser,
                            isLoading = false,
                            successMessage = "Email berhasil diperbarui!"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Gagal memperbarui email."
                        )
                    }
                }
        }
    }

    fun updatePassword(newPassword: String, confirmPassword: String) {
        if (newPassword.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Kata sandi baru tidak boleh kosong.") }
            return
        }
        if (newPassword.length < 6) {
            _uiState.update { it.copy(errorMessage = "Kata sandi minimal 6 karakter.") }
            return
        }
        if (newPassword != confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Konfirmasi kata sandi tidak cocok.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            authRepository.updatePassword(newPassword)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Kata sandi berhasil diperbarui!"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Gagal memperbarui kata sandi."
                        )
                    }
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.logout()
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            user = null,
                            isLoading = false,
                            isLoggedOut = true
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Gagal keluar."
                        )
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}
