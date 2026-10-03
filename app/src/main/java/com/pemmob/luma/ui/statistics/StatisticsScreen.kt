package com.pemmob.luma.ui.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pemmob.luma.ui.components.AmountText
import com.pemmob.luma.ui.components.EmptyState
import com.pemmob.luma.ui.components.LumaCard
import com.pemmob.luma.ui.components.SectionHeader
import com.pemmob.luma.ui.theme.LUMATheme

@Composable
fun StatisticsScreen(
    uiState: StatisticsUiState,
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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = MaterialTheme.colorScheme.onBackground)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Statistik",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (uiState is StatisticsUiState.Success) {
                        Text(
                            text = uiState.data.periodLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
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
                is StatisticsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is StatisticsUiState.Error -> {
                    EmptyState(
                        icon = Icons.Default.ErrorOutline,
                        title = "Terjadi Kesalahan",
                        description = uiState.message,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is StatisticsUiState.Success -> {
                    StatisticsContentList(data = uiState.data)
                }
            }
        }
    }
}

@Composable
private fun StatisticsContentList(data: StatisticsData) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            IncomeExpenseComparisonCard(income = data.totalIncome, expense = data.totalExpense)
        }

        if (data.expenseByCategory.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.PieChart,
                    title = "Belum Ada Pengeluaran",
                    description = "Data statistik akan muncul setelah kamu mencatat pengeluaran bulan ini."
                )
            }
        } else {
            data.topCategory?.let { top ->
                item {
                    TopCategoryCard(topCategory = top)
                }
            }

            item {
                SectionHeader(title = "Pengeluaran per Kategori")
            }

            val maxExpense = data.expenseByCategory.maxOfOrNull { it.amount } ?: 0.0

            items(data.expenseByCategory) { category ->
                CategoryExpenseItem(category = category, maxExpense = maxExpense)
            }
        }
    }
}

@Composable
private fun IncomeExpenseComparisonCard(income: Double, expense: Double) {
    LumaCard {
        Text(
            text = "Pemasukan vs Pengeluaran",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))

        val maxVal = maxOf(income, expense)
        val incomeRatio = if (maxVal > 0) (income / maxVal).toFloat() else 0f
        val expenseRatio = if (maxVal > 0) (expense / maxVal).toFloat() else 0f

        // Income Row
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Masuk",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.width(60.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            ) {
                if (incomeRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(incomeRatio)
                            .height(12.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            AmountText(amount = income, isIncome = true, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Expense Row
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Keluar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.width(60.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            ) {
                if (expenseRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(expenseRatio)
                            .height(12.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            AmountText(amount = expense, isIncome = false, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun TopCategoryCard(topCategory: CategoryExpenseData) {
    LumaCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Kategori Terbesar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = topCategory.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                AmountText(amount = topCategory.amount, isIncome = false, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${topCategory.percentage}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun CategoryExpenseItem(category: CategoryExpenseData, maxExpense: Double) {
    val ratio = if (maxExpense > 0) (category.amount / maxExpense).toFloat() else 0f
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${category.name} (${category.percentage}%)",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
            )
            AmountText(
                amount = category.amount,
                isIncome = false,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        ) {
            if (ratio > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(ratio)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

// ===== PREVIEWS =====

private fun getDummyData() = StatisticsData(
    periodLabel = "Oktober 2026",
    totalIncome = 3500000.0,
    totalExpense = 1750000.0,
    topCategory = CategoryExpenseData("Makanan", 750000.0, 42),
    expenseByCategory = listOf(
        CategoryExpenseData("Makanan", 750000.0, 42),
        CategoryExpenseData("Transportasi", 500000.0, 28),
        CategoryExpenseData("Hiburan", 300000.0, 17),
        CategoryExpenseData("Belanja", 200000.0, 11)
    )
)

@Preview(showBackground = true)
@Composable
private fun PreviewStatisticsSuccess() {
    LUMATheme {
        StatisticsScreen(
            uiState = StatisticsUiState.Success(getDummyData()),
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewStatisticsEmpty() {
    LUMATheme {
        StatisticsScreen(
            uiState = StatisticsUiState.Success(getDummyData().copy(expenseByCategory = emptyList(), topCategory = null)),
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewStatisticsLoading() {
    LUMATheme {
        StatisticsScreen(
            uiState = StatisticsUiState.Loading,
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewStatisticsError() {
    LUMATheme {
        StatisticsScreen(
            uiState = StatisticsUiState.Error("Gagal memuat data statistik."),
            onNavigateBack = {}
        )
    }
}
