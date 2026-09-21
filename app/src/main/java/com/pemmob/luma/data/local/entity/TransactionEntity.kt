package com.pemmob.luma.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,          // ID pengguna pemilik transaksi (dari Supabase Auth)
    val type: String,            // "INCOME" atau "EXPENSE"
    val amount: Long,            // Nominal uang dalam bentuk satuan rupiah bulat
    val category: String,        // Kategori transaksi (Fixed)
    val wallet: String = "Tunai / Cash", // Dompet / Akun (Tunai, QRIS, Transfer, E-Wallet)
    val note: String,            // Catatan atau deskripsi transaksi
    val date: Long,              // Tanggal transaksi dalam timestamp (epoch millis)
    val createdAt: Long = System.currentTimeMillis()
)
