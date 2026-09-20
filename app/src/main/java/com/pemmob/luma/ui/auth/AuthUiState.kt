package com.pemmob.luma.ui.auth

import com.pemmob.luma.domain.model.UserDomainModel

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Success(
        val user: UserDomainModel?,
        val message: String? = null
    ) : AuthUiState
    data object LoggedOut : AuthUiState
    data class Error(val message: String) : AuthUiState
}
