package com.pemmob.luma.di

import com.pemmob.luma.data.repository.AuthRepositoryImpl
import com.pemmob.luma.data.repository.TransactionRepositoryImpl
import com.pemmob.luma.domain.repository.AuthRepository
import com.pemmob.luma.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        transactionRepositoryImpl: TransactionRepositoryImpl
    ): TransactionRepository
}
