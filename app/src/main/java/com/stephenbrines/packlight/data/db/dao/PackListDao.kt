package com.stephenbrines.packlight.data.db.dao

import androidx.room.*
import com.stephenbrines.packlight.data.model.GearItem
import com.stephenbrines.packlight.data.model.PackList
import com.stephenbrines.packlight.data.model.PackListItem
import com.stephenbrines.packlight.data.model.PackListItemWithGear
import com.stephenbrines.packlight.data.model.PackListWithItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

@Dao
interface PackListDao {
    @Query("SELECT * FROM pack_lists WHERE tripId = :tripId ORDER BY createdAt ASC")
    fun getPackListsForTrip(tripId: String): Flow<List<PackList>>

    @Query("SELECT * FROM pack_lists WHERE id = :id")
    suspend fun getById(id: String): PackList?

    @Query("SELECT * FROM pack_list_items WHERE packListId = :packListId")
    fun getItemsForPackList(packListId: String): Flow<List<PackListItem>>

    @Query("""
        SELECT gi.* FROM gear_items gi
        INNER JOIN pack_list_items pli ON gi.id = pli.gearItemId
        WHERE pli.packListId = :packListId
    """)
    fun getGearForPackList(packListId: String): Flow<List<GearItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackList(packList: PackList)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackListItem(item: PackListItem)

    @Update
    suspend fun updatePackListItem(item: PackListItem)

    @Delete
    suspend fun deletePackListItem(item: PackListItem)

    @Query("DELETE FROM pack_list_items WHERE packListId = :packListId AND gearItemId = :gearItemId")
    suspend fun removeItemByGear(packListId: String, gearItemId: String)

    @Query("SELECT * FROM pack_list_items WHERE packListId = :packListId AND gearItemId = :gearItemId LIMIT 1")
    suspend fun findItem(packListId: String, gearItemId: String): PackListItem?
}
