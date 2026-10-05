package com.pemmob.luma.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pemmob.luma.data.local.dao.DebtReceivableDao
import com.pemmob.luma.data.local.dao.PaymentDao
import com.pemmob.luma.data.local.dao.SplitBillDao
import com.pemmob.luma.data.local.dao.SplitBillParticipantDao
import com.pemmob.luma.data.local.dao.TransactionDao
import com.pemmob.luma.data.local.entity.DebtReceivableEntity
import com.pemmob.luma.data.local.entity.PaymentEntity
import com.pemmob.luma.data.local.entity.SplitBillEntity
import com.pemmob.luma.data.local.entity.SplitBillParticipantEntity
import com.pemmob.luma.data.local.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        DebtReceivableEntity::class,
        PaymentEntity::class,
        SplitBillEntity::class,
        SplitBillParticipantEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun debtReceivableDao(): DebtReceivableDao
    abstract fun paymentDao(): PaymentDao
    abstract fun splitBillDao(): SplitBillDao
    abstract fun splitBillParticipantDao(): SplitBillParticipantDao

    companion object {
        const val DATABASE_NAME = "luma_database"
    }
}
