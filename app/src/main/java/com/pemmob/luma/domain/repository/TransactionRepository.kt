package com.pemmob.luma.domain.repository

import com.pemmob.luma.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(userId: String): Flow<List<TransactionEntity>>
    fun getTransactionsByType(userId: String, type: String): Flow<List<TransactionEntity>>
    suspend fun getTransactionById(transactionId: String): TransactionEntity?
    suspend fun insertTransaction(transaction: TransactionEntity)
    suspend fun updateTransaction(transaction: TransactionEntity)
    suspend fun deleteTransaction(transaction: TransactionEntity)
    suspend fun deleteTransactionById(transactionId: String)
}
