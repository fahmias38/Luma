package com.pemmob.luma.ui.profile

import com.pemmob.luma.domain.model.UserDomainModel

data class ProfileUiState(
    val user: UserDomainModel? = null,
    val isLoading: Boolean = false,
    val isLoggedOut: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
