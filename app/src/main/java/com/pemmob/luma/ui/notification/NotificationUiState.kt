package com.pemmob.luma.ui.notification

sealed interface NotificationUiState {
    data object Loading : NotificationUiState
    data class Success(val notifications: List<NotificationItem>) : NotificationUiState
    data class Error(val message: String) : NotificationUiState
}

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timeLabel: String,
    val type: String
)
