package com.pemmob.luma.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity untuk Utang (DEBT) dan Piutang (RECEIVABLE).
 *
 * type:
 *   - "DEBT"       = kita berutang kepada [personName]
 *   - "RECEIVABLE" = [personName] berutang kepada kita
 *
 * source:
 *   - "MANUAL"     = ditambahkan manual oleh user
 *   - "SPLIT_BILL" = dibuat otomatis oleh sistem Split Bill
 *
 * status:
 *   - "UNPAID"     = paidAmount < amount
 *   - "PAID"       = paidAmount >= amount
 *   Status ini harus selalu DERIVED dari paidAmount, jangan di-set manual.
 */
@Entity(tableName = "debt_receivables")
data class DebtReceivableEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val personName: String,
    val type: String,             // "DEBT" atau "RECEIVABLE"
    val amount: Long,             // Nominal total kewajiban (rupiah)
    val paidAmount: Long = 0L,    // Total yang sudah dibayarkan
    val description: String = "", // Catatan/deskripsi (opsional)
    val date: Long,               // Tanggal kewajiban (epoch millis)
    val source: String = "MANUAL",// "MANUAL" atau "SPLIT_BILL"
    val splitBillId: String? = null, // Foreign key ke SplitBillEntity (nullable)
    val status: String = "UNPAID",   // "UNPAID" atau "PAID"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
