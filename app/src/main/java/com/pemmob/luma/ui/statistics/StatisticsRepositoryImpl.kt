package com.pemmob.luma.ui.statistics

import com.pemmob.luma.domain.repository.AuthRepository
import com.pemmob.luma.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository
) : StatisticsRepository {

    override fun getStatistics(year: Int, month: Int): Flow<StatisticsData> = flow {
        val user = authRepository.getCurrentUser()
        if (user == null) {
            emit(getEmptyData())
        } else {
            transactionRepository.getAllTransactions(user.id)
                .map { transactions ->
                    val filtered = transactions.filter { tx ->
                        val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
                        cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month
                    }

                    val calendarForLabel = Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                    }
                    val formatter = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("id-ID"))
                    val periodLabel = formatter.format(calendarForLabel.time)

                    val totalIncome = filtered.filter { it.type == "INCOME" }.sumOf { it.amount }.toDouble()
                    val totalExpense = filtered.filter { it.type == "EXPENSE" }.sumOf { it.amount }.toDouble()

                    val expenseGrouped = filtered.filter { it.type == "EXPENSE" }
                        .groupBy { it.category }
                        .mapValues { entry -> entry.value.sumOf { it.amount }.toDouble() }

                    val expenseByCategory = expenseGrouped.map { (catName, amount) ->
                        val percentage = if (totalExpense > 0) ((amount / totalExpense) * 100).toInt() else 0
                        CategoryExpenseData(name = catName, amount = amount, percentage = percentage)
                    }.sortedByDescending { it.amount }

                    val topCategory = expenseByCategory.firstOrNull()

                    StatisticsData(
                        periodLabel = periodLabel,
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
                        topCategory = topCategory,
                        expenseByCategory = expenseByCategory
                    )
                }
                .collect { data ->
                    emit(data)
                }
        }
    }

    private fun getEmptyData() = StatisticsData(
        periodLabel = "",
        totalIncome = 0.0,
        totalExpense = 0.0,
        topCategory = null,
        expenseByCategory = emptyList()
    )
}
