package com.pemmob.luma.domain.repository

import com.pemmob.luma.data.local.entity.SplitBillEntity
import com.pemmob.luma.data.local.entity.SplitBillParticipantEntity
import kotlinx.coroutines.flow.Flow

interface SplitBillRepository {

    fun observeAll(userId: String): Flow<List<SplitBillEntity>>
    fun observeById(id: String): Flow<SplitBillEntity?>
    fun observeParticipants(splitBillId: String): Flow<List<SplitBillParticipantEntity>>

    /**
     * Menyimpan Split Bill secara atomik:
     * 1. Insert SplitBillEntity ke Room + Supabase
     * 2. Hitung equal share per participant
     *    - Remainder diberikan ke participant pertama (non-payer) untuk menjaga total konsisten
     * 3. Insert semua SplitBillParticipantEntity
     * 4. Untuk setiap non-payer participant, otomatis buat DebtReceivableEntity
     *    dengan source = "SPLIT_BILL" dan link ke split bill ini
     * 5. Return splitBillId jika berhasil
     */
    suspend fun createSplitBill(
        userId: String,
        title: String,
        totalAmount: Long,
        payerName: String,
        participantNames: List<String>,
        date: Long
    ): Result<String>

    suspend fun deleteSplitBill(id: String): Result<Unit>
}
