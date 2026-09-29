package com.pemmob.luma.domain.repository

import com.pemmob.luma.data.local.entity.DebtReceivableEntity
import com.pemmob.luma.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

interface DebtReceivableRepository {

    // Observasi (Flow reaktif untuk UI)
    fun observeAll(userId: String): Flow<List<DebtReceivableEntity>>
    fun observeByType(userId: String, type: String): Flow<List<DebtReceivableEntity>>
    fun observeById(id: String): Flow<DebtReceivableEntity?>
    fun observePayments(debtId: String): Flow<List<PaymentEntity>>
    fun observeBySplitBillId(splitBillId: String): Flow<List<DebtReceivableEntity>>

    // CRUD Debt
    suspend fun insertDebt(entity: DebtReceivableEntity): Result<String>
    suspend fun updateDebt(entity: DebtReceivableEntity): Result<Unit>
    suspend fun deleteDebt(id: String): Result<Unit>

    /**
     * Catat pembayaran baru.
     * Akan:
     * 1. Insert PaymentEntity ke Room + Supabase
     * 2. Hitung ulang total paidAmount dari semua payments
     * 3. Derive status (PAID jika paidAmount >= amount)
     * 4. Update DebtReceivableEntity di Room + Supabase
     */
    suspend fun addPayment(
        debtId: String,
        amount: Long,
        paymentDate: Long,
        note: String
    ): Result<Unit>
}
