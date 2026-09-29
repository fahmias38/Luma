package com.pemmob.luma.ui.splitbill

import com.pemmob.luma.data.local.entity.SplitBillEntity
import com.pemmob.luma.data.local.entity.SplitBillParticipantEntity

/**
 * Hasil kalkulasi equal split sebelum disimpan.
 */
data class SplitBillCalculation(
    val title: String,
    val totalAmount: Long,
    val payerName: String,
    val participants: List<ParticipantShare>,
    val settlementItems: List<SettlementItem>
)

data class ParticipantShare(
    val name: String,
    val shareAmount: Long,
    val isPayer: Boolean
)

/**
 * Satu baris hasil "siapa bayar siapa?".
 * from: orang yang berutang
 * to: orang yang menerima pembayaran (payer)
 * amount: nominal yang harus dibayar
 */
data class SettlementItem(
    val from: String,
    val to: String,
    val amount: Long
)

sealed interface SplitBillListUiState {
    data object Loading : SplitBillListUiState
    data class Success(val bills: List<SplitBillEntity>) : SplitBillListUiState
    data class Error(val message: String) : SplitBillListUiState
}

sealed interface SplitBillCreateUiState {
    data object Editing : SplitBillCreateUiState
    data class Calculated(
        val calculation: SplitBillCalculation,
        val isSaving: Boolean = false,
        val error: String? = null
    ) : SplitBillCreateUiState
    data class Success(val splitBillId: String) : SplitBillCreateUiState
}

sealed interface SplitBillDetailUiState {
    data object Loading : SplitBillDetailUiState
    data class Success(
        val bill: SplitBillEntity,
        val participants: List<SplitBillParticipantEntity>
    ) : SplitBillDetailUiState
    data class Error(val message: String) : SplitBillDetailUiState
}
