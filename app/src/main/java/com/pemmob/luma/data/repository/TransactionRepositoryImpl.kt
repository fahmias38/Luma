package com.pemmob.luma.data.repository

import android.util.Log
import com.pemmob.luma.data.local.dao.TransactionDao
import com.pemmob.luma.data.local.entity.TransactionEntity
import com.pemmob.luma.data.remote.model.TransactionRemote
import com.pemmob.luma.domain.repository.TransactionRepository
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val postgrest: Postgrest
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
        // 1. Simpan ke Room (lokal)
        transactionDao.insertTransaction(transaction)

        // 2. Sinkronkan ke Supabase Postgrest (cloud)
        runCatching {
            postgrest["transactions"].upsert(
                TransactionRemote(
                    id = transaction.id,
                    userId = transaction.userId,
                    type = transaction.type,
                    amount = transaction.amount,
                    category = transaction.category,
                    wallet = transaction.wallet,
                    note = transaction.note,
                    date = transaction.date,
                    createdAt = transaction.createdAt
                )
            )
        }.onFailure { e ->
            Log.e("SupabaseSync", "Gagal sinkronisasi transaksi ke Supabase: ${e.message}", e)
        }
    }

    override suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)

        runCatching {
            postgrest["transactions"].upsert(
                TransactionRemote(
                    id = transaction.id,
                    userId = transaction.userId,
                    type = transaction.type,
                    amount = transaction.amount,
                    category = transaction.category,
                    wallet = transaction.wallet,
                    note = transaction.note,
                    date = transaction.date,
                    createdAt = transaction.createdAt
                )
            )
        }.onFailure { e ->
            Log.e("SupabaseSync", "Gagal update transaksi ke Supabase: ${e.message}", e)
        }
    }

    override suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)

        runCatching {
            postgrest["transactions"].delete {
                filter { eq("id", transaction.id) }
            }
        }.onFailure { e ->
            Log.e("SupabaseSync", "Gagal hapus transaksi dari Supabase: ${e.message}", e)
        }
    }

    override suspend fun deleteTransactionById(transactionId: String) {
        val entity = transactionDao.getTransactionById(transactionId)
        transactionDao.deleteTransactionById(transactionId)

        if (entity != null) {
            runCatching {
                postgrest["transactions"].delete {
                    filter { eq("id", transactionId) }
                }
            }.onFailure { e ->
                Log.e("SupabaseSync", "Gagal hapus transaksi dari Supabase: ${e.message}", e)
            }
        }
    }

    override suspend fun syncRemoteTransactions(userId: String): Result<Unit> {
        return runCatching {
            // 1. UPLOAD: Unggah seluruh transaksi lokal milik pengguna aktif di HP ke Supabase Cloud
            val localList = transactionDao.getTransactionsListByUser(userId)
            if (localList.isNotEmpty()) {
                val remoteToUpload = localList.map { tx ->
                    TransactionRemote(
                        id = tx.id,
                        userId = tx.userId,
                        type = tx.type,
                        amount = tx.amount,
                        category = tx.category,
                        wallet = tx.wallet,
                        note = tx.note,
                        date = tx.date,
                        createdAt = tx.createdAt
                    )
                }
                runCatching {
                    postgrest["transactions"].upsert(remoteToUpload)
                }.onFailure { e ->
                    Log.e("SupabaseSync", "Gagal upload transaksi lokal ke Supabase: ${e.message}", e)
                }
            }

            // 2. DOWNLOAD: Unduh seluruh transaksi pengguna ini dari Supabase Cloud ke Room DB
            val remoteList = postgrest["transactions"]
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<TransactionRemote>()

            val localEntities = remoteList.map { remote ->
                TransactionEntity(
                    id = remote.id,
                    userId = remote.userId, // Pertahankan userId asli milik objek remote dari Supabase
                    type = remote.type,
                    amount = remote.amount,
                    category = remote.category,
                    wallet = remote.wallet,
                    note = remote.note,
                    date = remote.date,
                    createdAt = remote.createdAt
                )
            }

            if (localEntities.isNotEmpty()) {
                transactionDao.insertTransactions(localEntities)
            }
        }
    }
}
