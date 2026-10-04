package com.pemmob.luma.domain.repository

import com.pemmob.luma.data.local.entity.DebtReceivableEntity
import com.pemmob.luma.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

interface DebtReceivableRepository {
    fun observeAll(userId: String): Flow<List<DebtReceivableEntity>>
    fun observeByType(userId: String, type: String): Flow<List<DebtReceivableEntity>>
    fun observeById(id: String): Flow<DebtReceivableEntity?>
    fun observePayments(debtId: String): Flow<List<PaymentEntity>>
    fun observeBySplitBillId(splitBillId: String): Flow<List<DebtReceivableEntity>>

    suspend fun insertDebt(entity: DebtReceivableEntity): Result<String>
    suspend fun updateDebt(entity: DebtReceivableEntity): Result<Unit>
    suspend fun deleteDebt(id: String): Result<Unit>
    suspend fun addPayment(debtId: String, amount: Long, paymentDate: Long, note: String): Result<Unit>
    suspend fun syncRemoteDebts(userId: String): Result<Unit> = Result.success(Unit)
}
