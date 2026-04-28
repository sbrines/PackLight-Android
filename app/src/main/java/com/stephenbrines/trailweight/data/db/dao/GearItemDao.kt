package com.stephenbrines.trailweight.data.db.dao

import androidx.room.*
import com.stephenbrines.trailweight.data.model.GearItem
import kotlinx.coroutines.flow.Flow

@Dao
interface GearItemDao {
    @Query("SELECT * FROM gear_items ORDER BY name ASC")
    fun getAll(): Flow<List<GearItem>>

    @Query("SELECT * FROM gear_items WHERE id = :id")
    suspend fun getById(id: String): GearItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: GearItem)

    @Update
    suspend fun update(item: GearItem)

    @Delete
    suspend fun delete(item: GearItem)

    @Query("DELETE FROM gear_items WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)
}
