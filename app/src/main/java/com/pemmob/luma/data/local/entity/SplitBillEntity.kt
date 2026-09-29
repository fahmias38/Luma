package com.pemmob.luma.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity untuk Split Bill.
 * Setiap Split Bill memiliki satu payer dan beberapa participants.
 * Setelah disimpan, sistem otomatis membuat DebtReceivable untuk setiap non-payer participant.
 */
@Entity(tableName = "split_bills")
data class SplitBillEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val title: String,           // Nama/judul Split Bill, contoh: "Makan Bareng"
    val totalAmount: Long,       // Total tagihan yang di-split
    val payerName: String,       // Nama orang yang membayar terlebih dahulu
    val date: Long,              // Tanggal transaksi (epoch millis)
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
