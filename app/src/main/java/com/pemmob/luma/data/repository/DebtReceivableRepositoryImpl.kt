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

    override fun observeAllPayments(userId: String): Flow<List<PaymentEntity>> =
        paymentDao.observeAll()

    override fun observeBySplitBillId(splitBillId: String): Flow<List<DebtReceivableEntity>> =
        debtDao.observeBySplitBillId(splitBillId)

    // ===== CRUD DEBT =====

    override suspend fun insertDebt(entity: DebtReceivableEntity): Result<String> {
        return runCatching {
            debtDao.insert(entity)

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
            }

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

            val payment = PaymentEntity(
                debtReceivableId = debtId,
                amount = amount,
                paymentDate = paymentDate,
                note = note
            )
            paymentDao.insert(payment)

            runCatching {
                postgrest["payments"].upsert(
                    PaymentRemote(
                        id = payment.id,
                        debtReceivableId = payment.debtReceivableId,
                        amount = payment.amount,
                        paymentDate = payment.paymentDate,
                        note = note,
                        createdAt = payment.createdAt
                    )
                )
            }

            val newPaidAmount = paymentDao.getTotalPaidForDebt(debtId) ?: 0L
            val newStatus = if (newPaidAmount >= debt.amount) "PAID" else "UNPAID"

            val updatedDebt = debt.copy(
                paidAmount = newPaidAmount,
                status = newStatus,
                updatedAt = System.currentTimeMillis()
            )
            debtDao.update(updatedDebt)

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

    override suspend fun syncRemoteDebts(userId: String): Result<Unit> {
        return runCatching {
            // 1. UPLOAD: Unggah utang/piutang lokal di HP ke Supabase Cloud
            val localDebts = debtDao.getDebtsListByUser(userId)
            if (localDebts.isNotEmpty()) {
                val remoteDebtsToUpload = localDebts.map { d ->
                    DebtReceivableRemote(
                        id = d.id,
                        userId = d.userId,
                        personName = d.personName,
                        type = d.type,
                        amount = d.amount,
                        paidAmount = d.paidAmount,
                        description = d.description,
                        date = d.date,
                        source = d.source,
                        splitBillId = d.splitBillId,
                        status = d.status,
                        createdAt = d.createdAt,
                        updatedAt = d.updatedAt
                    )
                }
                runCatching {
                    postgrest["debt_receivables"].upsert(remoteDebtsToUpload)
                }
            }

            // 2. DOWNLOAD: Unduh utang/piutang pengguna dari Supabase Cloud
            val remoteDebts = postgrest["debt_receivables"]
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<DebtReceivableRemote>()

            val localEntities = remoteDebts.map { remote ->
                DebtReceivableEntity(
                    id = remote.id,
                    userId = remote.userId, // Pertahankan userId asli milik objek remote
                    personName = remote.personName,
                    type = remote.type,
                    amount = remote.amount,
                    paidAmount = remote.paidAmount,
                    description = remote.description,
                    date = remote.date,
                    source = remote.source,
                    splitBillId = remote.splitBillId,
                    status = remote.status,
                    createdAt = remote.createdAt,
                    updatedAt = remote.updatedAt
                )
            }

            if (localEntities.isNotEmpty()) {
                debtDao.insertAll(localEntities)
            }

            val remotePayments = postgrest["payments"]
                .select()
                .decodeList<PaymentRemote>()

            val paymentEntities = remotePayments.map { p ->
                PaymentEntity(
                    id = p.id,
                    debtReceivableId = p.debtReceivableId,
                    amount = p.amount,
                    paymentDate = p.paymentDate,
                    note = p.note,
                    createdAt = p.createdAt
                )
            }

            if (paymentEntities.isNotEmpty()) {
                paymentDao.insertAll(paymentEntities)
            }
        }
    }
}
