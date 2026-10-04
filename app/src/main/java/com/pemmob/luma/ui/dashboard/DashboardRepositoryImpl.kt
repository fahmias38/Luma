package com.pemmob.luma.ui.dashboard

import com.pemmob.luma.domain.repository.AuthRepository
import com.pemmob.luma.domain.repository.DebtReceivableRepository
import com.pemmob.luma.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository,
    private val debtReceivableRepository: DebtReceivableRepository
) : DashboardRepository {

    override fun getDashboardData(): Flow<DashboardData> {
        return flow {
            val user = authRepository.getCurrentUser()
            if (user == null) {
                emit(getEmptyDashboardData())
                return@flow
            }
            
            val userId = user.id
            val userName = user.fullName ?: user.email.substringBefore("@")

            // Sync data dari Supabase Cloud ke Room Local DB otomatis saat masuk dashboard
            runCatching { transactionRepository.syncRemoteTransactions(userId) }
            runCatching { debtReceivableRepository.syncRemoteDebts(userId) }

            // Observe Transactions and Debts dari Room DB
            val allTransactionsFlow = transactionRepository.getAllTransactions(userId)
            val debtsFlow = debtReceivableRepository.observeByType(userId, "DEBT")
            val receivablesFlow = debtReceivableRepository.observeByType(userId, "RECEIVABLE")

            combine(
                allTransactionsFlow,
                debtsFlow,
                receivablesFlow
            ) { transactions, debts, receivables ->
                
                val currentCalendar = Calendar.getInstance()
                val currentMonth = currentCalendar.get(Calendar.MONTH)
                val currentYear = currentCalendar.get(Calendar.YEAR)
                
                val formatter = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("id-ID"))
                val monthYear = formatter.format(Date())

                val totalIncomeAllTime = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
                val totalExpenseAllTime = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                val balance = (totalIncomeAllTime - totalExpenseAllTime).toDouble()

                val currentMonthTransactions = transactions.filter {
                    val txCalendar = Calendar.getInstance().apply { timeInMillis = it.date }
                    txCalendar.get(Calendar.MONTH) == currentMonth && txCalendar.get(Calendar.YEAR) == currentYear
                }
                
                val currentMonthIncome = currentMonthTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }.toDouble()
                val currentMonthExpense = currentMonthTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }.toDouble()

                val unpaidDebts = debts.filter { it.status == "UNPAID" }
                val totalDebt = unpaidDebts.sumOf { it.amount - it.paidAmount }.toDouble()
                val pendingDebtCount = unpaidDebts.size
                
                val unpaidReceivables = receivables.filter { it.status == "UNPAID" }
                val totalReceivable = unpaidReceivables.sumOf { it.amount - it.paidAmount }.toDouble()
                val pendingReceivableCount = unpaidReceivables.size

                val expenseByCategory = currentMonthTransactions
                    .filter { it.type == "EXPENSE" }
                    .groupBy { it.category }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }
                
                val topCategoryEntry = expenseByCategory.maxByOrNull { it.value }
                val topCategoryName = topCategoryEntry?.key ?: "Belum ada"
                val topCategoryPercentage = if (currentMonthExpense > 0 && topCategoryEntry != null) {
                    ((topCategoryEntry.value / currentMonthExpense) * 100).toInt()
                } else {
                    0
                }

                val dateFormat = SimpleDateFormat("dd MMM", Locale.forLanguageTag("id-ID"))
                val recentTransactions = transactions
                    .sortedByDescending { it.date }
                    .take(4)
                    .map { tx ->
                        TransactionData(
                            id = tx.id,
                            title = tx.note.ifBlank { tx.category },
                            date = dateFormat.format(Date(tx.date)),
                            category = tx.category,
                            amount = tx.amount.toDouble(),
                            isIncome = tx.type == "INCOME",
                            paymentMethod = tx.wallet
                        )
                    }

                DashboardData(
                    userName = userName,
                    monthYear = monthYear,
                    balance = balance,
                    income = currentMonthIncome,
                    expense = currentMonthExpense,
                    debt = totalDebt,
                    receivable = totalReceivable,
                    pendingDebtCount = pendingDebtCount,
                    pendingReceivableCount = pendingReceivableCount,
                    topCategoryName = topCategoryName,
                    topCategoryPercentage = topCategoryPercentage,
                    recentTransactions = recentTransactions
                )
            }.collect {
                emit(it)
            }
        }
    }
    
    private fun getEmptyDashboardData() = DashboardData(
        userName = "Pengguna",
        monthYear = "",
        balance = 0.0,
        income = 0.0,
        expense = 0.0,
        debt = 0.0,
        receivable = 0.0,
        pendingDebtCount = 0,
        pendingReceivableCount = 0,
        topCategoryName = "-",
        topCategoryPercentage = 0,
        recentTransactions = emptyList()
    )
}
