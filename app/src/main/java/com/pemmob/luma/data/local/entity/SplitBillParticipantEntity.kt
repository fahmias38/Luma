package com.pemmob.luma.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity untuk setiap peserta Split Bill.
 *
 * isPayer: true jika participant ini yang membayar total tagihan terlebih dahulu.
 * shareAmount: bagian yang harus ditanggung participant ini (equal split).
 * debtReceivableId: FK ke DebtReceivableEntity yang dibuat otomatis untuk participant ini.
 *   - null untuk payer (karena payer tidak berutang kepada siapapun dalam bill ini)
 *   - non-null untuk non-payer (link ke debt record mereka)
 */
@Entity(
    tableName = "split_bill_participants",
    foreignKeys = [
        ForeignKey(
            entity = SplitBillEntity::class,
            parentColumns = ["id"],
            childColumns = ["splitBillId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["splitBillId"])]
)
data class SplitBillParticipantEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val splitBillId: String,            // FK ke SplitBillEntity
    val name: String,                   // Nama peserta
    val shareAmount: Long,              // Bagian yang harus dibayar (equal split)
    val isPayer: Boolean = false,       // true jika participant ini adalah payer
    val debtReceivableId: String? = null // FK ke DebtReceivableEntity (null untuk payer)
)
