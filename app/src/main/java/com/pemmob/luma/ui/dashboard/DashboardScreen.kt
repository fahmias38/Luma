package com.pemmob.luma.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pemmob.luma.ui.components.*
import com.pemmob.luma.ui.theme.LUMATheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    currentFilter: DashboardFilter,
    onFilterChange: (DashboardFilter) -> Unit,
    onAddTransactionClick: () -> Unit,
    onSplitBillClick: () -> Unit,
    onDebtClick: () -> Unit,
    onSeeAllTransactionsClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is DashboardUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is DashboardUiState.Error -> {
                    EmptyState(
                        icon = Icons.Default.ErrorOutline,
                        title = "Terjadi Kesalahan",
                        description = uiState.message,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is DashboardUiState.Success -> {
                    DashboardSuccessContent(
                        data = uiState.data,
                        currentFilter = currentFilter,
                        onFilterChange = onFilterChange,
                        onAddTransactionClick = onAddTransactionClick,
                        onSplitBillClick = onSplitBillClick,
                        onDebtClick = onDebtClick,
                        onSeeAllTransactionsClick = onSeeAllTransactionsClick,
                        onStatisticsClick = onStatisticsClick
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardSuccessContent(
    data: DashboardData,
    currentFilter: DashboardFilter,
    onFilterChange: (DashboardFilter) -> Unit,
    onAddTransactionClick: () -> Unit,
    onSplitBillClick: () -> Unit,
    onDebtClick: () -> Unit,
    onSeeAllTransactionsClick: () -> Unit,
    onStatisticsClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            DashboardHeader(
                userName = data.userName, 
                monthYear = data.monthYear
            )
        }
        item {
            HeroBalanceCard(
                balance = data.balance,
                onAddTransactionClick = onAddTransactionClick,
                onSplitBillClick = onSplitBillClick,
                onDebtClick = onDebtClick
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    val selected = currentFilter == DashboardFilter.ALL
                    Surface(
                        onClick = { onFilterChange(DashboardFilter.ALL) },
                        shape = RoundedCornerShape(24.dp),
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Semua",
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    val selected = currentFilter == DashboardFilter.MONTH
                    Surface(
                        onClick = { onFilterChange(DashboardFilter.MONTH) },
                        shape = RoundedCornerShape(24.dp),
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Bulan Ini",
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IncomeExpenseCard(
                    title = "Pemasukan",
                    amount = data.income,
                    isIncome = true,
                    currentFilter = currentFilter,
                    modifier = Modifier.weight(1f)
                )
                IncomeExpenseCard(
                    title = "Pengeluaran",
                    amount = data.expense,
                    isIncome = false,
                    currentFilter = currentFilter,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            DebtSummaryCard(
                debt = data.debt,
                receivable = data.receivable,
                pendingDebtCount = data.pendingDebtCount,
                pendingReceivableCount = data.pendingReceivableCount,
                onDetailClick = onDebtClick
            )
        }
        item {
            InsightCard(
                topCategories = data.topCategories,
                onCardClick = onStatisticsClick
            )
        }
        item {
            SectionHeader(
                title = "Transaksi Terakhir",
                actionText = "Lihat Semua",
                onActionClick = onSeeAllTransactionsClick
            )
            
            if (data.recentTransactions.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Receipt,
                    title = "Belum Ada Transaksi",
                    description = "Catat pengeluaran atau pemasukan pertamamu di sini."
                )
            } else {
                Column {
                    data.recentTransactions.forEachIndexed { index, tx ->
                        TransactionItem(
                            icon = getIconForCategory(tx.category),
                            title = tx.title,
                            date = tx.date,
                            category = tx.category,
                            amount = tx.amount,
                            isIncome = tx.isIncome,
                            paymentMethod = tx.paymentMethod
                        )
                        if (index < data.recentTransactions.size - 1) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}

@Composable
private fun DashboardHeader(
    userName: String, 
    monthYear: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.pemmob.luma.R.drawable.logo),
                contentDescription = "Logo Luma",
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                val displayName = userName.take(12) + if (userName.length > 12) "…" else ""
                Text(
                    text = "Halo, $displayName",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = monthYear,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun HeroBalanceCard(
    balance: Double,
    onAddTransactionClick: () -> Unit,
    onSplitBillClick: () -> Unit,
    onDebtClick: () -> Unit
) {
    var balanceVisible by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SALDO SAAT INI",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
                Icon(
                    imageVector = if (balanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Sembunyikan saldo",
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { balanceVisible = !balanceVisible }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            val formattedBalance = if (balanceVisible) {
                NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
                    maximumFractionDigits = 0
                }.format(balance).replace("Rp", "Rp")
            } else {
                "Rp *****"
            }
            
            Text(
                text = formattedBalance,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HeroButton(
                    icon = Icons.Default.Add,
                    text = "Catat",
                    onClick = onAddTransactionClick,
                    modifier = Modifier.weight(1f)
                )
                HeroButton(
                    icon = Icons.Default.CallSplit,
                    text = "Split Bill",
                    onClick = onSplitBillClick,
                    modifier = Modifier.weight(1f)
                )
                HeroButton(
                    icon = Icons.Default.ReceiptLong,
                    text = "Tagih",
                    onClick = onDebtClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HeroButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun IncomeExpenseCard(
    title: String,
    amount: Double,
    isIncome: Boolean,
    currentFilter: DashboardFilter,
    modifier: Modifier = Modifier
) {
    LumaCard(modifier = modifier) {
        val icon = if (isIncome) Icons.Default.SouthWest else Icons.Default.NorthEast
        val color = if (isIncome) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
        val bgColor = color.copy(alpha = 0.1f)
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(bgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        AmountText(
            amount = amount,
            isIncome = isIncome,
            style = MaterialTheme.typography.titleMedium
        )
        if (currentFilter == DashboardFilter.MONTH) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Bulan ini",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun DebtSummaryCard(
    debt: Double,
    receivable: Double,
    pendingDebtCount: Int,
    pendingReceivableCount: Int,
    onDetailClick: () -> Unit
) {
    LumaCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Handshake,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Catatan Teman",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Detail",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.clickable { onDetailClick() }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Kamu Berutang",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                AmountText(amount = debt, isIncome = false, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "$pendingDebtCount tagihan tertunda",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(60.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    "Piutangmu",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                AmountText(amount = receivable, isIncome = true, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "$pendingReceivableCount teman belum lunas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun InsightCard(
    topCategories: List<CategoryData>,
    onCardClick: () -> Unit
) {
    LumaCard(modifier = Modifier.clickable { onCardClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PieChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Top 3 Pengeluaran",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Lihat Statistik",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        if (topCategories.isEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Belum ada data pengeluaran.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                topCategories.forEach { category ->
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${category.percentage}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(category.percentage / 100f)
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getIconForCategory(category: String): ImageVector {
    return when (category.lowercase()) {
        "makanan" -> Icons.Default.Fastfood
        "transportasi" -> Icons.Default.DirectionsBike
        "pemasukan" -> Icons.Default.AccountBalanceWallet
        "cafe & nongkrong" -> Icons.Default.LocalCafe
        "utang", "piutang" -> Icons.Default.AccountBalance
        else -> Icons.Default.Receipt
    }
}

private fun getDummyDashboardData() = DashboardData(
    userName = "Fahmi",
    monthYear = "September 2026",
    balance = 1250000.0,
    income = 2000000.0,
    expense = 750000.0,
    debt = 100000.0,
    receivable = 175000.0,
    pendingDebtCount = 2,
    pendingReceivableCount = 3,
    topCategories = listOf(
        CategoryData("Makanan", 337500.0, 45),
        CategoryData("Transportasi", 225000.0, 30),
        CategoryData("Cafe", 112500.0, 15)
    ),
    recentTransactions = listOf(
        TransactionData("1", "Makan Siang Kantin", "10 Sep", "Makanan", 25000.0, false, "QRIS"),
        TransactionData("2", "Ojek ke Kampus", "10 Sep", "Transportasi", 15000.0, false, "E-Wallet"),
        TransactionData("3", "Transfer Uang Saku", "9 Sep", "Pemasukan", 500000.0, true, "Transfer Bank"),
        TransactionData("4", "Kopi Belajar Nugas", "8 Sep", "Cafe & Nongkrong", 22000.0, false, null)
    ),
    hasNotification = false
)

@Preview(showBackground = true)
@Composable
private fun PreviewDashboardSuccess() {
    LUMATheme {
        DashboardScreen(
            uiState = DashboardUiState.Success(getDummyDashboardData()),
            currentFilter = DashboardFilter.MONTH,
            onFilterChange = {},
            onAddTransactionClick = {},
            onSplitBillClick = {},
            onDebtClick = {},
            onSeeAllTransactionsClick = {},
            onStatisticsClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDashboardLoading() {
    LUMATheme {
        DashboardScreen(
            uiState = DashboardUiState.Loading,
            currentFilter = DashboardFilter.MONTH,
            onFilterChange = {},
            onAddTransactionClick = {},
            onSplitBillClick = {},
            onDebtClick = {},
            onSeeAllTransactionsClick = {},
            onStatisticsClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDashboardEmpty() {
    LUMATheme {
        val emptyData = getDummyDashboardData().copy(recentTransactions = emptyList())
        DashboardScreen(
            uiState = DashboardUiState.Success(emptyData),
            currentFilter = DashboardFilter.MONTH,
            onFilterChange = {},
            onAddTransactionClick = {},
            onSplitBillClick = {},
            onDebtClick = {},
            onSeeAllTransactionsClick = {},
            onStatisticsClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDashboardError() {
    LUMATheme {
        DashboardScreen(
            uiState = DashboardUiState.Error("Gagal mengambil data dari server."),
            currentFilter = DashboardFilter.MONTH,
            onFilterChange = {},
            onAddTransactionClick = {},
            onSplitBillClick = {},
            onDebtClick = {},
            onSeeAllTransactionsClick = {},
            onStatisticsClick = {}
        )
    }
}
