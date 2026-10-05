package com.pemmob.luma.ui.dashboard

import com.pemmob.luma.domain.repository.AuthRepository
import com.pemmob.luma.domain.repository.DebtReceivableRepository
import com.pemmob.luma.domain.repository.TransactionRepository
import com.pemmob.luma.ui.notification.NotificationRepository
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
    private val debtReceivableRepository: DebtReceivableRepository,
    private val notificationRepository: NotificationRepository
) : DashboardRepository {

    override fun getDashboardData(filter: DashboardFilter): Flow<DashboardData> {
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

            val allTransactionsFlow = transactionRepository.getAllTransactions(userId)
            val debtsFlow = debtReceivableRepository.observeByType(userId, "DEBT")
            val receivablesFlow = debtReceivableRepository.observeByType(userId, "RECEIVABLE")
            val paymentsFlow = debtReceivableRepository.observeAllPayments(userId)
            val hasUnreadFlow = notificationRepository.hasUnread()

            combine(
                allTransactionsFlow,
                debtsFlow,
                receivablesFlow,
                paymentsFlow,
                hasUnreadFlow
            ) { transactions, debts, receivables, payments, hasUnread ->

                val currentCalendar = Calendar.getInstance()
                val currentMonth = currentCalendar.get(Calendar.MONTH)
                val currentYear = currentCalendar.get(Calendar.YEAR)

                val formatter = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("id-ID"))
                val monthYear = formatter.format(Date())

                // =============================================
                // SALDO: All-time, dipengaruhi utang/piutang
                // =============================================
                val totalIncomeAllTime = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
                val totalExpenseAllTime = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }

                val paidDebtsSum = payments.filter { payment ->
                    val debt = debts.find { it.id == payment.debtReceivableId }
                    debt != null
                }.sumOf { it.amount }

                val receivableOutSum = receivables.sumOf { it.amount }
                val paidReceivablesSum = payments.filter { payment ->
                    val rec = receivables.find { it.id == payment.debtReceivableId }
                    rec != null
                }.sumOf { it.amount }

                val balance = ((totalIncomeAllTime + paidReceivablesSum) - (totalExpenseAllTime + receivableOutSum)).toDouble()

                // =============================================
                // FILTERED DATA (UNTUK STATISTIK & GRAFIK)
                // =============================================
                val filteredTransactions = when (filter) {
                    DashboardFilter.MONTH -> transactions.filter {
                        val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                        cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
                    }
                    DashboardFilter.ALL -> transactions
                }

                val filteredIncome = filteredTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }.toDouble()
                val filteredExpense = filteredTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }.toDouble()

                // =============================================
                // TOP CATEGORIES (Termasuk Utang & Piutang)
                // =============================================
                val expenseByCategory = mutableMapOf<String, Double>()
                filteredTransactions.filter { it.type == "EXPENSE" }.forEach { tx ->
                    expenseByCategory[tx.category] = (expenseByCategory[tx.category] ?: 0.0) + tx.amount.toDouble()
                }

                val filteredPaidDebt = when (filter) {
                    DashboardFilter.MONTH -> payments.filter {
                        val cal = Calendar.getInstance().apply { timeInMillis = it.paymentDate }
                        cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
                    }.filter { payment ->
                        debts.any { it.id == payment.debtReceivableId }
                    }.sumOf { it.amount }.toDouble()
                    DashboardFilter.ALL -> paidDebtsSum.toDouble()
                }

                val filteredReceivableOut = when (filter) {
                    DashboardFilter.MONTH -> receivables.filter {
                        val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                        cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
                    }.sumOf { it.amount }.toDouble()
                    DashboardFilter.ALL -> receivableOutSum.toDouble()
                }

                if (filteredPaidDebt > 0) expenseByCategory["Utang"] = filteredPaidDebt
                if (filteredReceivableOut > 0) expenseByCategory["Piutang"] = filteredReceivableOut

                val totalFilteredExpenseForChart = expenseByCategory.values.sum()

                val topCategories = expenseByCategory.entries
                    .sortedByDescending { it.value }
                    .take(3)
                    .map { (name, amount) ->
                        CategoryData(
                            name = name,
                            amount = amount,
                            percentage = if (totalFilteredExpenseForChart > 0) {
                                ((amount / totalFilteredExpenseForChart) * 100).toInt()
                            } else 0
                        )
                    }

                // =============================================
                // DEBT SUMMARY (tetap all-status untuk card catatan teman)
                // =============================================
                val unpaidDebts = debts.filter { it.status == "UNPAID" }
                val totalDebt = unpaidDebts.sumOf { it.amount - it.paidAmount }.toDouble()
                val pendingDebtCount = unpaidDebts.size

                val unpaidReceivables = receivables.filter { it.status == "UNPAID" }
                val totalReceivable = unpaidReceivables.sumOf { it.amount - it.paidAmount }.toDouble()
                val pendingReceivableCount = unpaidReceivables.size

                // =============================================
                // RECENT TRANSACTIONS (filtered)
                // =============================================
                val dateFormat = SimpleDateFormat("dd MMM", Locale.forLanguageTag("id-ID"))
                val recentTransactions = filteredTransactions
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
                    income = filteredIncome,
                    expense = filteredExpense,
                    debt = totalDebt,
                    receivable = totalReceivable,
                    pendingDebtCount = pendingDebtCount,
                    pendingReceivableCount = pendingReceivableCount,
                    topCategories = topCategories,
                    recentTransactions = recentTransactions,
                    hasNotification = hasUnread
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
        topCategories = emptyList(),
        recentTransactions = emptyList(),
        hasNotification = false
    )
}
