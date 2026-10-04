package com.pemmob.luma.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Model untuk sinkronisasi Transaksi (Pemasukan / Pengeluaran) ke Supabase.
 */
@Serializable
data class TransactionRemote(
    val id: String,
    @SerialName("user_id") val userId: String,
    val type: String,
    val amount: Long,
    val category: String,
    val wallet: String = "Tunai / Cash",
    val note: String = "",
    val date: Long,
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis()
)

/**
 * Model untuk sinkronisasi DebtReceivable ke Supabase.
 * Nama kolom menggunakan snake_case sesuai konvensi PostgreSQL.
 */
@Serializable
data class DebtReceivableRemote(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("person_name") val personName: String,
    val type: String,
    val amount: Long,
    @SerialName("paid_amount") val paidAmount: Long = 0L,
    val description: String = "",
    val date: Long,
    val source: String = "MANUAL",
    @SerialName("split_bill_id") val splitBillId: String? = null,
    val status: String = "UNPAID",
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis(),
    @SerialName("updated_at") val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class PaymentRemote(
    val id: String,
    @SerialName("debt_receivable_id") val debtReceivableId: String,
    val amount: Long,
    @SerialName("payment_date") val paymentDate: Long,
    val note: String = "",
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class SplitBillRemote(
    val id: String,
    @SerialName("user_id") val userId: String,
    val title: String,
    @SerialName("total_amount") val totalAmount: Long,
    @SerialName("payer_name") val payerName: String,
    val date: Long,
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis(),
    @SerialName("updated_at") val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class SplitBillParticipantRemote(
    val id: String,
    @SerialName("split_bill_id") val splitBillId: String,
    val name: String,
    @SerialName("share_amount") val shareAmount: Long,
    @SerialName("is_payer") val isPayer: Boolean = false,
    @SerialName("debt_receivable_id") val debtReceivableId: String? = null
)
