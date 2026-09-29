package com.pemmob.luma.di

import android.content.Context
import androidx.room.Room
import com.pemmob.luma.data.local.dao.DebtReceivableDao
import com.pemmob.luma.data.local.dao.PaymentDao
import com.pemmob.luma.data.local.dao.SplitBillDao
import com.pemmob.luma.data.local.dao.SplitBillParticipantDao
import com.pemmob.luma.data.local.dao.TransactionDao
import com.pemmob.luma.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration()
         .build()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: AppDatabase): TransactionDao =
        database.transactionDao()

    @Provides
    @Singleton
    fun provideDebtReceivableDao(database: AppDatabase): DebtReceivableDao =
        database.debtReceivableDao()

    @Provides
    @Singleton
    fun providePaymentDao(database: AppDatabase): PaymentDao =
        database.paymentDao()

    @Provides
    @Singleton
    fun provideSplitBillDao(database: AppDatabase): SplitBillDao =
        database.splitBillDao()

    @Provides
    @Singleton
    fun provideSplitBillParticipantDao(database: AppDatabase): SplitBillParticipantDao =
        database.splitBillParticipantDao()
}
