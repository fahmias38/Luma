package com.pemmob.luma.data.repository

import com.pemmob.luma.data.local.dao.DebtReceivableDao
import com.pemmob.luma.data.local.dao.PaymentDao
import com.pemmob.luma.data.local.entity.DebtReceivableEntity
import com.pemmob.luma.data.local.entity.PaymentEntity
import com.pemmob.luma.data.remote.model.DebtReceivableRemote
import com.pemmob.luma.data.remote.model.PaymentRemote
import com.pemmob.luma.domain.repository.DebtReceivableRepository
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebtReceivableRepositoryImpl @Inject constructor(
    private val debtDao: DebtReceivableDao,
    private val paymentDao: PaymentDao,
    private val postgrest: Postgrest
) : DebtReceivableRepository {

    // ===== OBSERVASI =====

    override fun observeAll(userId: String): Flow<List<DebtReceivableEntity>> =
        debtDao.observeAllByUser(userId)

    override fun observeByType(userId: String, type: String): Flow<List<DebtReceivableEntity>> =
        debtDao.observeByType(userId, type)

    override fun observeById(id: String): Flow<DebtReceivableEntity?> =
        debtDao.observeById(id)

    override fun observePayments(debtId: String): Flow<List<PaymentEntity>> =
        paymentDao.observeByDebtId(debtId)

    override fun observeBySplitBillId(splitBillId: String): Flow<List<DebtReceivableEntity>> =
        debtDao.observeBySplitBillId(splitBillId)

    // ===== CRUD DEBT =====

    override suspend fun insertDebt(entity: DebtReceivableEntity): Result<String> {
        return runCatching {
            // 1. Simpan ke Room (lokal, langsung)
            debtDao.insert(entity)

            // 2. Sinkronisasi ke Supabase (background, non-blocking untuk UX)
            runCatching {
                postgrest["debt_receivables"].upsert(
                    DebtReceivableRemote(
                        id = entity.id,
                        userId = entity.userId,
                        personName = entity.personName,
                        type = entity.type,
                        amount = entity.amount,
                        paidAmount = entity.paidAmount,
                        description = entity.description,
                        date = entity.date,
                        source = entity.source,
                        splitBillId = entity.splitBillId,
                        status = entity.status,
                        createdAt = entity.createdAt,
                        updatedAt = entity.updatedAt
                    )
                )
            } // Supabase error tidak di-throw, Room menjadi fallback

            entity.id
        }
    }

    override suspend fun updateDebt(entity: DebtReceivableEntity): Result<Unit> {
        return runCatching {
            val updated = entity.copy(updatedAt = System.currentTimeMillis())
            debtDao.update(updated)

            runCatching {
                postgrest["debt_receivables"].upsert(
                    DebtReceivableRemote(
                        id = updated.id,
                        userId = updated.userId,
                        personName = updated.personName,
                        type = updated.type,
                        amount = updated.amount,
                        paidAmount = updated.paidAmount,
                        description = updated.description,
                        date = updated.date,
                        source = updated.source,
                        splitBillId = updated.splitBillId,
                        status = updated.status,
                        createdAt = updated.createdAt,
                        updatedAt = updated.updatedAt
                    )
                )
            }
        }
    }

    override suspend fun deleteDebt(id: String): Result<Unit> {
        return runCatching {
            debtDao.deleteById(id)
            runCatching {
                postgrest["debt_receivables"].delete {
                    filter { eq("id", id) }
                }
            }
        }
    }

    // ===== PAYMENT =====

    /**
     * Alur catat pembayaran:
     * 1. Ambil data debt existing
     * 2. Validasi: payment tidak boleh melebihi remaining
     * 3. Insert payment baru ke Room + Supabase
     * 4. Hitung ulang total paidAmount dari semua payments
     * 5. Derive status (PAID jika paidAmount >= amount)
     * 6. Update debt entity di Room + Supabase
     */
    override suspend fun addPayment(
        debtId: String,
        amount: Long,
        paymentDate: Long,
        note: String
    ): Result<Unit> {
        return runCatching {
            val debt = debtDao.getById(debtId)
                ?: throw IllegalArgumentException("Debt tidak ditemukan dengan id: $debtId")

            val currentPaid = paymentDao.getTotalPaidForDebt(debtId) ?: 0L
            val remaining = debt.amount - currentPaid

            require(amount > 0) { "Nominal pembayaran harus lebih dari 0." }
            require(amount <= remaining) {
                "Nominal pembayaran (Rp$amount) melebihi sisa kewajiban (Rp$remaining)."
            }

            // Insert payment baru
            val payment = PaymentEntity(
                debtReceivableId = debtId,
                amount = amount,
                paymentDate = paymentDate,
                note = note
            )
            paymentDao.insert(payment)

            // Sync payment ke Supabase
            runCatching {
                postgrest["payments"].upsert(
                    PaymentRemote(
                        id = payment.id,
                        debtReceivableId = payment.debtReceivableId,
                        amount = payment.amount,
                        paymentDate = payment.paymentDate,
                        note = payment.note,
                        createdAt = payment.createdAt
                    )
                )
            }

            // Recalculate paidAmount
            val newPaidAmount = paymentDao.getTotalPaidForDebt(debtId) ?: 0L
            val newStatus = if (newPaidAmount >= debt.amount) "PAID" else "UNPAID"

            val updatedDebt = debt.copy(
                paidAmount = newPaidAmount,
                status = newStatus,
                updatedAt = System.currentTimeMillis()
            )
            debtDao.update(updatedDebt)

            // Sync updated debt ke Supabase
            runCatching {
                postgrest["debt_receivables"].upsert(
                    DebtReceivableRemote(
                        id = updatedDebt.id,
                        userId = updatedDebt.userId,
                        personName = updatedDebt.personName,
                        type = updatedDebt.type,
                        amount = updatedDebt.amount,
                        paidAmount = updatedDebt.paidAmount,
                        description = updatedDebt.description,
                        date = updatedDebt.date,
                        source = updatedDebt.source,
                        splitBillId = updatedDebt.splitBillId,
                        status = updatedDebt.status,
                        createdAt = updatedDebt.createdAt,
                        updatedAt = updatedDebt.updatedAt
                    )
                )
            }
        }
    }
}
