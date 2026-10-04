package com.pemmob.luma.ui.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pemmob.luma.ui.components.EmptyState
import com.pemmob.luma.ui.components.LumaCard
import com.pemmob.luma.ui.components.LumaWarning
import com.pemmob.luma.ui.theme.LUMATheme

@Composable
fun NotificationScreen(
    uiState: NotificationUiState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                        contentDescription = "Kembali", 
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Notifikasi",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is NotificationUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is NotificationUiState.Error -> {
                    EmptyState(
                        icon = Icons.Default.ErrorOutline,
                        title = "Terjadi Kesalahan",
                        description = uiState.message,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is NotificationUiState.Success -> {
                    if (uiState.notifications.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.Notifications,
                            title = "Belum ada notifikasi",
                            description = "Pemberitahuan tagihan, peringatan saldo, dan aktivitas akunmu akan muncul di sini.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.notifications, key = { it.id }) { item ->
                                NotificationItemCard(item = item)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItemCard(item: NotificationItem) {
    val icon: ImageVector
    val iconTint: Color
    val iconBg: Color

    when (item.type.lowercase()) {
        "warning" -> {
            icon = Icons.Default.WarningAmber
            iconTint = LumaWarning // Warning color #F59E0B
            iconBg = LumaWarning.copy(alpha = 0.1f)
        }
        "success" -> {
            icon = Icons.Default.CheckCircleOutline
            iconTint = MaterialTheme.colorScheme.secondary
            iconBg = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
        }
        "error" -> {
            icon = Icons.Default.ErrorOutline
            iconTint = MaterialTheme.colorScheme.tertiary
            iconBg = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
        }
        else -> { // "info"
            icon = Icons.Default.Info
            iconTint = MaterialTheme.colorScheme.primary
            iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        }
    }

    LumaCard {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ===== PREVIEWS =====

@Preview(showBackground = true)
@Composable
private fun PreviewNotificationEmpty() {
    LUMATheme {
        NotificationScreen(
            uiState = NotificationUiState.Success(emptyList()),
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewNotificationLoading() {
    LUMATheme {
        NotificationScreen(
            uiState = NotificationUiState.Loading,
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewNotificationSuccess() {
    LUMATheme {
        val dummyData = listOf(
            NotificationItem("1", "Tagihan Masuk", "Fahmi menagih uang patungan Netflix sebesar Rp45.000.", "10 mnt", "warning"),
            NotificationItem("2", "Pembayaran Berhasil", "Kamu berhasil membayar utang ke Nindy sejumlah Rp25.000.", "1 jam", "success"),
            NotificationItem("3", "Pengingat Saldo", "Saldo kamu sisa Rp150.000.", "Kemarin", "info"),
        )
        NotificationScreen(
            uiState = NotificationUiState.Success(dummyData),
            onNavigateBack = {}
        )
    }
}
