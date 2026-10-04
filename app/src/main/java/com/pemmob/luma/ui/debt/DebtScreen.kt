package com.pemmob.luma.ui.debt

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pemmob.luma.data.local.entity.DebtReceivableEntity
import com.pemmob.luma.ui.theme.SakuCanvasBackground
import com.pemmob.luma.ui.theme.SakuCardBackground
import com.pemmob.luma.ui.theme.SakuInputBackground
import com.pemmob.luma.ui.theme.SakuTextDark
import com.pemmob.luma.ui.theme.SakuTextMuted
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAddDebt: () -> Unit,
    onNavigateToSplitBill: () -> Unit,
    viewModel: DebtViewModel = hiltViewModel()
) {
    val uiState by viewModel.listUiState.collectAsStateWithLifecycle()
    val currentFilter by viewModel.currentFilter.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Utang & Piutang",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Split Bill FAB (kecil)
                SmallFloatingActionButton(
                    onClick = onNavigateToSplitBill,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.CallSplit, contentDescription = "Split Bill")
                }
                // Add Debt FAB (utama)
                FloatingActionButton(
                    onClick = onNavigateToAddDebt,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Utang/Piutang")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Summary Cards
            if (uiState is DebtListUiState.Success) {
                val state = uiState as DebtListUiState.Success
                DebtSummaryCards(
                    totalDebt = state.totalDebt,
                    totalReceivable = state.totalReceivable
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 1 Baris 2 Tombol Dropdown (Kiri: Type, Kanan: Status)
            DebtFiltersRow(
                currentFilter = currentFilter,
                onFilterChange = { viewModel.setFilter(it) },
                currentStatusFilter = statusFilter,
                onStatusFilterChange = { viewModel.setStatusFilter(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Content
            when (val state = uiState) {
                is DebtListUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is DebtListUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                is DebtListUiState.Success -> {
                    val typeFiltered = when (currentFilter) {
                        DebtFilter.ALL -> state.allItems
                        DebtFilter.DEBT -> state.debtItems
                        DebtFilter.RECEIVABLE -> state.receivableItems
                    }

                    val displayedItems = when (statusFilter) {
                        DebtStatusFilter.ALL -> typeFiltered
                        DebtStatusFilter.PAID -> typeFiltered.filter { it.status == "PAID" }
                        DebtStatusFilter.UNPAID -> typeFiltered.filter { it.status != "PAID" }
                    }

                    if (displayedItems.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = SakuTextMuted.copy(alpha = 0.4f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Belum ada data.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = SakuTextMuted
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            items(displayedItems, key = { it.id }) { item ->
                                DebtItemCard(
                                    item = item,
                                    onClick = { onNavigateToDetail(item.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DebtFiltersRow(
    currentFilter: DebtFilter,
    onFilterChange: (DebtFilter) -> Unit,
    currentStatusFilter: DebtStatusFilter,
    onStatusFilterChange: (DebtStatusFilter) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Left Dropdown: Type (Semua, Utang, Piutang)
        Box(modifier = Modifier.weight(1f)) {
            var expandedType by remember { mutableStateOf(false) }
            val typeLabel = when (currentFilter) {
                DebtFilter.ALL -> "Semua"
                DebtFilter.DEBT -> "Utang"
                DebtFilter.RECEIVABLE -> "Piutang"
            }
            Surface(
                onClick = { expandedType = true },
                shape = RoundedCornerShape(20.dp),
                color = if (currentFilter != DebtFilter.ALL) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = typeLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (currentFilter != DebtFilter.ALL) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = if (currentFilter != DebtFilter.ALL) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            DropdownMenu(
                expanded = expandedType,
                onDismissRequest = { expandedType = false }
            ) {
                DropdownMenuItem(text = { Text("Semua") }, onClick = { onFilterChange(DebtFilter.ALL); expandedType = false })
                DropdownMenuItem(text = { Text("Utang") }, onClick = { onFilterChange(DebtFilter.DEBT); expandedType = false })
                DropdownMenuItem(text = { Text("Piutang") }, onClick = { onFilterChange(DebtFilter.RECEIVABLE); expandedType = false })
            }
        }

        // Right Dropdown: Status (Semua Status, Belum Lunas, Sudah Lunas)
        Box(modifier = Modifier.weight(1f)) {
            var expandedStatus by remember { mutableStateOf(false) }
            val statusLabel = when (currentStatusFilter) {
                DebtStatusFilter.ALL -> "Semua Status"
                DebtStatusFilter.UNPAID -> "Belum Lunas"
                DebtStatusFilter.PAID -> "Sudah Lunas"
            }
            Surface(
                onClick = { expandedStatus = true },
                shape = RoundedCornerShape(20.dp),
                color = if (currentStatusFilter != DebtStatusFilter.ALL) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (currentStatusFilter != DebtStatusFilter.ALL) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = if (currentStatusFilter != DebtStatusFilter.ALL) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            DropdownMenu(
                expanded = expandedStatus,
                onDismissRequest = { expandedStatus = false }
            ) {
                DropdownMenuItem(text = { Text("Semua Status") }, onClick = { onStatusFilterChange(DebtStatusFilter.ALL); expandedStatus = false })
                DropdownMenuItem(text = { Text("Belum Lunas") }, onClick = { onStatusFilterChange(DebtStatusFilter.UNPAID); expandedStatus = false })
                DropdownMenuItem(text = { Text("Sudah Lunas") }, onClick = { onStatusFilterChange(DebtStatusFilter.PAID); expandedStatus = false })
            }
        }
    }
}

@Composable
private fun DebtSummaryCards(
    totalDebt: Long,
    totalReceivable: Long
) {
    val fmt = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SakuCardBackground),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Utang (merah)
            SummaryCard(
                label = "Harus Bayar",
                amount = totalDebt,
                amountColor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            // Piutang (hijau)
            SummaryCard(
                label = "Akan Diterima",
                amount = totalReceivable,
                amountColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryCard(
    label: String,
    amount: Long,
    amountColor: Color,
    modifier: Modifier = Modifier
) {
    val fmt = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = SakuInputBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = SakuTextMuted,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = fmt.format(amount),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}

@Composable
fun DebtItemCard(
    item: DebtReceivableEntity,
    onClick: () -> Unit
) {
    val fmt = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID"))

    val isReceivable = item.type == "RECEIVABLE"
    val isPaid = item.status == "PAID"
    val typeColor = if (isReceivable) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
    val typeLabel = if (isReceivable) "Piutang" else "Utang"
    val statusColor = if (isPaid) MaterialTheme.colorScheme.secondary else Color(0xFFF59E0B)
    val statusLabel = if (isPaid) "Lunas" else "Belum Lunas"
    val remainingAmount = (item.amount - item.paidAmount).coerceAtLeast(0L)

    Card(
        colors = CardDefaults.cardColors(containerColor = SakuCardBackground),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(typeColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.personName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = typeColor
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.personName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = SakuTextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = typeColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = typeLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = typeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = fmt.format(remainingAmount),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = typeColor
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.source == "SPLIT_BILL") {
                        Text(
                            text = "Split Bill",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(text = "·", fontSize = 11.sp, color = SakuTextMuted)
                    }
                    Text(
                        text = dateFormat.format(Date(item.date)),
                        fontSize = 11.sp,
                        color = SakuTextMuted
                    )
                }
            }

            // Status chip
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = statusColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = statusLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}
