package com.pemmob.luma.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pemmob.luma.data.local.dao.TransactionDao
import com.pemmob.luma.data.local.entity.TransactionEntity

@Database(entities = [TransactionEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "luma_database"
    }
}
