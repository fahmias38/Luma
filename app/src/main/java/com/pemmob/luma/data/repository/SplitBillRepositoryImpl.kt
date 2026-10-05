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

            val splitBill = SplitBillEntity(
                userId = userId,
                title = title,
                totalAmount = totalAmount,
                payerName = payerName,
                date = date
            )
            splitBillDao.insert(splitBill)

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

            val participants = mutableListOf<SplitBillParticipantEntity>()
            val debtEntities = mutableListOf<DebtReceivableEntity>()
            var remainderAssigned = false

            participantNames.forEach { name ->
                val isPayer = name == payerName

                val share = if (isPayer && !remainderAssigned) {
                    remainderAssigned = true
                    baseShare + remainder
                } else {
                    baseShare
                }

                var debtId: String? = null
                if (!isPayer) {
                    val debtEntity = DebtReceivableEntity(
                        userId = userId,
                        personName = name,
                        type = "RECEIVABLE",
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

            participantDao.insertAll(participants)

            debtEntities.forEach { debt ->
                debtRepository.insertDebt(debt)
            }

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

    override suspend fun syncRemoteSplitBills(userId: String): Result<Unit> {
        return runCatching {
            // 1. UPLOAD local split bills to Supabase
            val localBills = splitBillDao.getSplitBillsListByUser(userId)
            if (localBills.isNotEmpty()) {
                val remoteBillsToUpload = localBills.map { b ->
                    SplitBillRemote(
                        id = b.id,
                        userId = b.userId,
                        title = b.title,
                        totalAmount = b.totalAmount,
                        payerName = b.payerName,
                        date = b.date,
                        createdAt = b.createdAt,
                        updatedAt = b.updatedAt
                    )
                }
                runCatching {
                    postgrest["split_bills"].upsert(remoteBillsToUpload)
                }
            }

            // 2. DOWNLOAD remote split bills from Supabase
            val remoteBills = postgrest["split_bills"]
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<SplitBillRemote>()

            val fetchedBills = remoteBills.map { remote ->
                SplitBillEntity(
                    id = remote.id,
                    userId = remote.userId, // Pertahankan userId asli milik objek remote
                    title = remote.title,
                    totalAmount = remote.totalAmount,
                    payerName = remote.payerName,
                    date = remote.date,
                    createdAt = remote.createdAt,
                    updatedAt = remote.updatedAt
                )
            }

            if (fetchedBills.isNotEmpty()) {
                splitBillDao.insertAll(fetchedBills)
            }

            val remoteParticipants = postgrest["split_bill_participants"]
                .select()
                .decodeList<SplitBillParticipantRemote>()

            val localParticipants = remoteParticipants.map { p ->
                SplitBillParticipantEntity(
                    id = p.id,
                    splitBillId = p.splitBillId,
                    name = p.name,
                    shareAmount = p.shareAmount,
                    isPayer = p.isPayer,
                    debtReceivableId = p.debtReceivableId
                )
            }

            if (localParticipants.isNotEmpty()) {
                participantDao.insertAll(localParticipants)
            }
        }
    }
}
