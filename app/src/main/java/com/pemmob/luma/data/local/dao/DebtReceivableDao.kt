package com.pemmob.luma.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pemmob.luma.data.local.entity.DebtReceivableEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtReceivableDao {

    @Query("SELECT * FROM debt_receivables WHERE userId = :userId ORDER BY date DESC")
    fun observeAllByUser(userId: String): Flow<List<DebtReceivableEntity>>

    @Query("SELECT * FROM debt_receivables WHERE userId = :userId AND type = :type ORDER BY date DESC")
    fun observeByType(userId: String, type: String): Flow<List<DebtReceivableEntity>>

    @Query("SELECT * FROM debt_receivables WHERE id = :id")
    suspend fun getById(id: String): DebtReceivableEntity?

    @Query("SELECT * FROM debt_receivables WHERE id = :id")
    fun observeById(id: String): Flow<DebtReceivableEntity?>

    @Query("SELECT * FROM debt_receivables WHERE splitBillId = :splitBillId")
    fun observeBySplitBillId(splitBillId: String): Flow<List<DebtReceivableEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DebtReceivableEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<DebtReceivableEntity>): List<Long>

    @Update
    suspend fun update(entity: DebtReceivableEntity): Int

    @Delete
    suspend fun delete(entity: DebtReceivableEntity): Int

    @Query("DELETE FROM debt_receivables WHERE id = :id")
    suspend fun deleteById(id: String): Int
}
