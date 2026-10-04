package com.pemmob.luma.domain.repository

import com.pemmob.luma.data.local.entity.SplitBillEntity
import com.pemmob.luma.data.local.entity.SplitBillParticipantEntity
import kotlinx.coroutines.flow.Flow

interface SplitBillRepository {

    fun observeAll(userId: String): Flow<List<SplitBillEntity>>
    fun observeById(id: String): Flow<SplitBillEntity?>
    fun observeParticipants(splitBillId: String): Flow<List<SplitBillParticipantEntity>>

    suspend fun createSplitBill(
        userId: String,
        title: String,
        totalAmount: Long,
        payerName: String,
        participantNames: List<String>,
        date: Long
    ): Result<String>

    suspend fun deleteSplitBill(id: String): Result<Unit>
    suspend fun syncRemoteSplitBills(userId: String): Result<Unit> = Result.success(Unit)
}
