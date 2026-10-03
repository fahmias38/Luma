package com.pemmob.luma.ui.dashboard

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

interface DashboardRepository {
    suspend fun getDashboardData(): DashboardData
}

@Singleton
class DummyDashboardRepository @Inject constructor() : DashboardRepository {
    override suspend fun getDashboardData(): DashboardData {
        delay(1000) // Simulasi loading jaringan
        return DashboardData(
            userName = "Fahmi",
            monthYear = "September 2026",
            balance = 1250000.0,
            income = 2000000.0,
            expense = 750000.0,
            debt = 100000.0,
            receivable = 175000.0,
            pendingDebtCount = 2,
            pendingReceivableCount = 3,
            topCategoryName = "Makanan",
            topCategoryPercentage = 45,
            budgetUsedPercentage = 37.5f,
            remainingBudget = 1250000.0,
            budgetStatus = "Aman",
            recentTransactions = listOf(
                TransactionData("1", "Makan Siang Kantin", "10 Sep", "Makanan", 25000.0, false, "QRIS"),
                TransactionData("2", "Ojek ke Kampus", "10 Sep", "Transportasi", 15000.0, false, "E-Wallet"),
                TransactionData("3", "Transfer Uang Saku", "9 Sep", "Pemasukan", 500000.0, true, "Transfer Bank"),
                TransactionData("4", "Kopi Belajar Nugas", "8 Sep", "Cafe & Nongkrong", 22000.0, false, null)
            )
        )
    }
}
