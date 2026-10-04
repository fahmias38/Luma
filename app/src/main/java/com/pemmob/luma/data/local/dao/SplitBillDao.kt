package com.pemmob.luma.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pemmob.luma.data.local.entity.SplitBillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SplitBillDao {

    @Query("SELECT * FROM split_bills WHERE userId = :userId ORDER BY date DESC")
    fun observeAllByUser(userId: String): Flow<List<SplitBillEntity>>

    @Query("SELECT * FROM split_bills WHERE id = :id")
    suspend fun getById(id: String): SplitBillEntity?

    @Query("SELECT * FROM split_bills WHERE id = :id")
    fun observeById(id: String): Flow<SplitBillEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SplitBillEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<SplitBillEntity>): List<Long>

    @Query("DELETE FROM split_bills WHERE id = :id")
    suspend fun deleteById(id: String): Int
}
