package com.pemmob.luma.ui.notification

import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun observeNotifications(): Flow<List<NotificationItem>>
}
