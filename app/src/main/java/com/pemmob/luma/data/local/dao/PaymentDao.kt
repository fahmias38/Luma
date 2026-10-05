package com.pemmob.luma.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pemmob.luma.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {

    @Query("SELECT * FROM payments WHERE debtReceivableId = :debtId ORDER BY paymentDate DESC")
    fun observeByDebtId(debtId: String): Flow<List<PaymentEntity>>

    @Query("SELECT p.* FROM payments p INNER JOIN debt_receivables d ON p.debtReceivableId = d.id WHERE d.userId = :userId ORDER BY p.paymentDate DESC")
    fun observeAllByUser(userId: String): Flow<List<PaymentEntity>>

    @Query("SELECT SUM(amount) FROM payments WHERE debtReceivableId = :debtId")
    suspend fun getTotalPaidForDebt(debtId: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PaymentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<PaymentEntity>): List<Long>

    @Query("DELETE FROM payments WHERE debtReceivableId = :debtId")
    suspend fun deleteAllForDebt(debtId: String): Int
}
