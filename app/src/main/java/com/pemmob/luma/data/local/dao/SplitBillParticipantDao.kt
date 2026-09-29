package com.pemmob.luma.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pemmob.luma.data.local.entity.SplitBillParticipantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SplitBillParticipantDao {

    @Query("SELECT * FROM split_bill_participants WHERE splitBillId = :splitBillId")
    fun observeByBillId(splitBillId: String): Flow<List<SplitBillParticipantEntity>>

    @Query("SELECT * FROM split_bill_participants WHERE splitBillId = :splitBillId")
    suspend fun getByBillId(splitBillId: String): List<SplitBillParticipantEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SplitBillParticipantEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<SplitBillParticipantEntity>)

    @Update
    suspend fun update(entity: SplitBillParticipantEntity): Int

    @Query("DELETE FROM split_bill_participants WHERE splitBillId = :splitBillId")
    suspend fun deleteByBillId(splitBillId: String): Int
}
