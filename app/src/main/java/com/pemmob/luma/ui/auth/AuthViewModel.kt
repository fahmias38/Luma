package com.pemmob.luma.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pemmob.luma.domain.model.UserDomainModel
import com.pemmob.luma.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<UserDomainModel?>(null)
    val currentUser: StateFlow<UserDomainModel?> = _currentUser.asStateFlow()

    fun onLoginClick(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Email dan password tidak boleh kosong.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.login(email = email.trim(), password = password)
                .onSuccess { user ->
                    _currentUser.value = user
                    _uiState.value = AuthUiState.Success(
                        user = user,
                        message = "Login berhasil."
                    )
                }
                .onFailure { exception ->
                    _uiState.value = AuthUiState.Error(
                        exception.localizedMessage ?: "Terjadi kesalahan saat login."
                    )
                }
        }
    }

    fun onRegisterClick(email: String, password: String, fullName: String) {
        if (email.isBlank() || password.isBlank() || fullName.isBlank()) {
            _uiState.value = AuthUiState.Error("Semua kolom pendaftaran wajib diisi.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.register(email = email.trim(), password = password, fullName = fullName.trim())
                .onSuccess { user ->
                    _currentUser.value = user
                    _uiState.value = AuthUiState.Success(
                        user = user,
                        message = "Pendaftaran berhasil."
                    )
                }
                .onFailure { exception ->
                    _uiState.value = AuthUiState.Error(
                        exception.localizedMessage ?: "Terjadi kesalahan saat mendaftar."
                    )
                }
        }
    }

    fun checkSession() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val hasActiveSession = authRepository.checkSession()
                if (hasActiveSession) {
                    val user = authRepository.getCurrentUser()
                    _currentUser.value = user
                    _uiState.value = AuthUiState.Success(
                        user = user,
                        message = "Sesi aktif ditemukan."
                    )
                } else {
                    _currentUser.value = null
                    _uiState.value = AuthUiState.LoggedOut
                }
            } catch (e: Exception) {
                _currentUser.value = null
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Gagal memeriksa sesi.")
            }
        }
    }

    fun onLogoutClick() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.logout()
                .onSuccess {
                    _currentUser.value = null
                    _uiState.value = AuthUiState.LoggedOut
                }
                .onFailure { exception ->
                    _uiState.value = AuthUiState.Error(
                        exception.localizedMessage ?: "Gagal melakukan logout."
                    )
                }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
