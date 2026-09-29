package com.pemmob.luma.data.repository

import com.pemmob.luma.data.local.dao.SplitBillDao
import com.pemmob.luma.data.local.dao.SplitBillParticipantDao
import com.pemmob.luma.data.local.entity.DebtReceivableEntity
import com.pemmob.luma.data.local.entity.SplitBillEntity
import com.pemmob.luma.data.local.entity.SplitBillParticipantEntity
import com.pemmob.luma.data.remote.model.SplitBillParticipantRemote
import com.pemmob.luma.data.remote.model.SplitBillRemote
import com.pemmob.luma.domain.repository.DebtReceivableRepository
import com.pemmob.luma.domain.repository.SplitBillRepository
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SplitBillRepositoryImpl @Inject constructor(
    private val splitBillDao: SplitBillDao,
    private val participantDao: SplitBillParticipantDao,
    private val debtRepository: DebtReceivableRepository,
    private val postgrest: Postgrest
) : SplitBillRepository {

    override fun observeAll(userId: String): Flow<List<SplitBillEntity>> =
        splitBillDao.observeAllByUser(userId)

    override fun observeById(id: String): Flow<SplitBillEntity?> =
        splitBillDao.observeById(id)

    override fun observeParticipants(splitBillId: String): Flow<List<SplitBillParticipantEntity>> =
        participantDao.observeByBillId(splitBillId)

    /**
     * LOGIKA UTAMA SPLIT BILL
     *
     * Equal Split dengan Remainder Handling:
     * - shareAmount = totalAmount / participantCount (integer division)
     * - remainder = totalAmount % participantCount
     * - Participant PERTAMA yang bukan payer mendapatkan (shareAmount + remainder)
     *   sehingga total share selalu == totalAmount
     *
     * Contoh: Rp100.000 / 3 = Rp33.333 per orang, remainder = 1
     * - Payer: Fahmi       → share = 33.334 (+ remainder)
     * - Non-payer 1: Fahri → share = 33.333 (debt Rp33.333 ke Fahmi)
     * - Non-payer 2: Melysa → share = 33.333 (debt Rp33.333 ke Fahmi)
     *
     * Settlement yang dihasilkan:
     * - Fahri berutang Rp33.333 kepada Fahmi → RECEIVABLE bagi Fahmi
     * - Melysa berutang Rp33.333 kepada Fahmi → RECEIVABLE bagi Fahmi
     *
     * CATATAN: Sistem mencatat dari perspektif userId (user yang login).
     * - Jika user adalah payer: generated records = RECEIVABLE (orang lain berutang ke user)
     * - Jika user adalah non-payer: generated records = DEBT (user berutang ke payer)
     * Untuk MVP, semua participants dianggap "teman" — bukan user lain yang login.
     * Record dibuat sebagai RECEIVABLE dari perspektif userId karena userId = payer.
     */
    override suspend fun createSplitBill(
        userId: String,
        title: String,
        totalAmount: Long,
        payerName: String,
        participantNames: List<String>,
        date: Long
    ): Result<String> {
        return runCatching {
            require(totalAmount > 0) { "Total amount harus lebih dari 0." }
            require(participantNames.size >= 2) { "Minimal 2 peserta diperlukan." }
            require(payerName in participantNames) { "Payer harus merupakan peserta." }

            val participantCount = participantNames.size
            val baseShare = totalAmount / participantCount
            val remainder = totalAmount % participantCount

            // Buat SplitBill entity
            val splitBill = SplitBillEntity(
                userId = userId,
                title = title,
                totalAmount = totalAmount,
                payerName = payerName,
                date = date
            )
            splitBillDao.insert(splitBill)

            // Sync SplitBill ke Supabase
            runCatching {
                postgrest["split_bills"].upsert(
                    SplitBillRemote(
                        id = splitBill.id,
                        userId = splitBill.userId,
                        title = splitBill.title,
                        totalAmount = splitBill.totalAmount,
                        payerName = splitBill.payerName,
                        date = splitBill.date,
                        createdAt = splitBill.createdAt,
                        updatedAt = splitBill.updatedAt
                    )
                )
            }

            // Hitung share per orang dan assign remainder ke payer
            // Payer mendapat share + remainder (mereka sudah bayar lebih, ini adil)
            val participants = mutableListOf<SplitBillParticipantEntity>()
            val debtEntities = mutableListOf<DebtReceivableEntity>()
            var remainderAssigned = false

            participantNames.forEach { name ->
                val isPayer = name == payerName

                // Payer mendapat (baseShare + remainder) sebagai kontribusinya
                val share = if (isPayer && !remainderAssigned) {
                    remainderAssigned = true
                    baseShare + remainder
                } else {
                    baseShare
                }

                // Debt/Receivable hanya dibuat untuk non-payer
                var debtId: String? = null
                if (!isPayer) {
                    // Non-payer berutang shareAmount kepada payer
                    // Dari perspektif userId (payer): ini adalah RECEIVABLE
                    val debtEntity = DebtReceivableEntity(
                        userId = userId,
                        personName = name,
                        type = "RECEIVABLE", // User (payer) memiliki piutang kepada participant
                        amount = share,
                        paidAmount = 0L,
                        description = "Split Bill: $title",
                        date = date,
                        source = "SPLIT_BILL",
                        splitBillId = splitBill.id,
                        status = "UNPAID"
                    )
                    debtEntities.add(debtEntity)
                    debtId = debtEntity.id
                }

                participants.add(
                    SplitBillParticipantEntity(
                        splitBillId = splitBill.id,
                        name = name,
                        shareAmount = share,
                        isPayer = isPayer,
                        debtReceivableId = debtId
                    )
                )
            }

            // Insert semua participants
            participantDao.insertAll(participants)

            // Insert semua DebtReceivable yang dihasilkan
            debtEntities.forEach { debt ->
                debtRepository.insertDebt(debt)
            }

            // Sync participants ke Supabase
            runCatching {
                val remoteParticipants = participants.map { p ->
                    SplitBillParticipantRemote(
                        id = p.id,
                        splitBillId = p.splitBillId,
                        name = p.name,
                        shareAmount = p.shareAmount,
                        isPayer = p.isPayer,
                        debtReceivableId = p.debtReceivableId
                    )
                }
                postgrest["split_bill_participants"].upsert(remoteParticipants)
            }

            splitBill.id
        }
    }

    override suspend fun deleteSplitBill(id: String): Result<Unit> {
        return runCatching {
            splitBillDao.deleteById(id)
            runCatching {
                postgrest["split_bills"].delete {
                    filter { eq("id", id) }
                }
            }
        }
    }
}
