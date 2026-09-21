package com.pemmob.luma.data.repository

import com.pemmob.luma.data.local.dao.TransactionDao
import com.pemmob.luma.data.local.entity.TransactionEntity
import com.pemmob.luma.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(userId: String): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactionsByUser(userId)
    }

    override fun getTransactionsByType(userId: String, type: String): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByType(userId, type)
    }

    override suspend fun getTransactionById(transactionId: String): TransactionEntity? {
        return transactionDao.getTransactionById(transactionId)
    }

    override suspend fun insertTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
    }

    override suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    override suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    override suspend fun deleteTransactionById(transactionId: String) {
        transactionDao.deleteTransactionById(transactionId)
    }
}
