package com.pemmob.luma.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity untuk mencatat setiap transaksi pembayaran pada Debt/Receivable.
 * Setiap payment mengurangi remaining amount secara kumulatif.
 * Status PAID/UNPAID pada DebtReceivableEntity dihitung dari SUM(payments).
 */
@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = DebtReceivableEntity::class,
            parentColumns = ["id"],
            childColumns = ["debtReceivableId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["debtReceivableId"])]
)
data class PaymentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val debtReceivableId: String,   // FK ke DebtReceivableEntity
    val amount: Long,               // Nominal pembayaran kali ini
    val paymentDate: Long,          // Tanggal pembayaran (epoch millis)
    val note: String = "",          // Catatan pembayaran (opsional)
    val createdAt: Long = System.currentTimeMillis()
)
