package com.stephenbrines.packlight.data.db.dao

import androidx.room.*
import com.stephenbrines.packlight.data.model.ResupplyItem
import com.stephenbrines.packlight.data.model.ResupplyPoint
import kotlinx.coroutines.flow.Flow

@Dao
interface ResupplyDao {
    @Query("SELECT * FROM resupply_points WHERE tripId = :tripId ORDER BY mileMarker ASC")
    fun getPointsForTrip(tripId: String): Flow<List<ResupplyPoint>>

    @Query("SELECT * FROM resupply_items WHERE resupplyPointId = :pointId")
    fun getItemsForPoint(pointId: String): Flow<List<ResupplyItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoint(point: ResupplyPoint)

    @Update
    suspend fun updatePoint(point: ResupplyPoint)

    @Delete
    suspend fun deletePoint(point: ResupplyPoint)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ResupplyItem)

    @Delete
    suspend fun deleteItem(item: ResupplyItem)
}
