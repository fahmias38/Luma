package com.pemmob.luma.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Loading)
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            try {
                repository.observeNotifications().collect { list ->
                    _uiState.value = NotificationUiState.Success(list)
                }
            } catch (e: Exception) {
                _uiState.value = NotificationUiState.Error(e.message ?: "Terjadi kesalahan yang tidak diketahui.")
            }
        }
    }

    fun markAsRead() {
        repository.markAsRead()
    }
}
