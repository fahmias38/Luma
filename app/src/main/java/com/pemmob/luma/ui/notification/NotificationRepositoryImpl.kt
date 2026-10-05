package com.pemmob.luma.ui.notification

import com.pemmob.luma.domain.repository.AuthRepository
import com.pemmob.luma.domain.repository.DebtReceivableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val debtReceivableRepository: DebtReceivableRepository
) : NotificationRepository {

    private var lastViewedTime = 0L

    override fun markAsRead() {
        lastViewedTime = System.currentTimeMillis()
    }

    override fun hasUnread(): Flow<Boolean> = observeNotifications().map { list ->
        list.any { it.updatedAt > lastViewedTime }
    }

    override fun observeNotifications(): Flow<List<NotificationItem>> = flow {
        val user = authRepository.getCurrentUser()
        if (user == null) {
            emit(emptyList())
        } else {
            emitAll(
                debtReceivableRepository.observeAll(user.id).map { list ->
                    list.sortedByDescending { it.updatedAt }
                        .take(50)
                        .map { entity ->
                            val sisa = entity.amount - entity.paidAmount
                            val formatRp = { amount: Long ->
                                NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
                                    maximumFractionDigits = 0
                                }.format(amount).replace("Rp", "Rp")
                            }

                            val isSplitBill = entity.source == "SPLIT_BILL"
                            val prefix = if (isSplitBill) "Split Bill: " else ""

                            val type: String
                            val title: String
                            val message: String

                            if (entity.status == "UNPAID") {
                                if (entity.type == "DEBT") {
                                    type = "warning"
                                    title = "${prefix}Utang belum lunas"
                                    message = "Kamu berutang ${formatRp(sisa)} ke ${entity.personName}."
                                } else { // RECEIVABLE
                                    type = "info"
                                    title = "${prefix}Piutang belum lunas"
                                    if (entity.paidAmount == 0L) {
                                        message = "${entity.personName} belum membayar ${formatRp(entity.amount)}."
                                    } else {
                                        message = "${entity.personName} sudah mencicil ${formatRp(entity.paidAmount)}, sisa ${formatRp(sisa)}."
                                    }
                                }
                            } else { // PAID
                                type = "success"
                                title = "${prefix}Lunas"
                                if (entity.type == "DEBT") {
                                    message = "Utang ke ${entity.personName} sebesar ${formatRp(entity.amount)} sudah lunas."
                                } else {
                                    message = "Piutang dari ${entity.personName} sebesar ${formatRp(entity.amount)} sudah lunas."
                                }
                            }

                            NotificationItem(
                                id = entity.id,
                                title = title,
                                message = message,
                                timeLabel = getTimeLabel(entity.updatedAt),
                                type = type,
                                updatedAt = entity.updatedAt
                            )
                        }
                }
            )
        }
    }

    private fun getTimeLabel(timeInMillis: Long): String {
        val now = Calendar.getInstance()
        val time = Calendar.getInstance().apply { this.timeInMillis = timeInMillis }
        val diffMs = now.timeInMillis - timeInMillis

        val oneMin = 60 * 1000L
        val oneHour = 60 * oneMin

        return when {
            diffMs < oneMin -> "Baru saja"
            diffMs < oneHour -> "${diffMs / oneMin} mnt"
            diffMs < 24 * oneHour && now.get(Calendar.DAY_OF_YEAR) == time.get(Calendar.DAY_OF_YEAR) -> "${diffMs / oneHour} jam"
            else -> {
                now.add(Calendar.DAY_OF_YEAR, -1)
                if (now.get(Calendar.DAY_OF_YEAR) == time.get(Calendar.DAY_OF_YEAR) && now.get(Calendar.YEAR) == time.get(Calendar.YEAR)) {
                    "Kemarin"
                } else {
                    SimpleDateFormat("dd MMM", Locale.forLanguageTag("id-ID")).format(Date(timeInMillis))
                }
            }
        }
    }
}
