package com.stephenbrines.packlight.data.repository

import com.stephenbrines.packlight.data.db.dao.PackListDao
import com.stephenbrines.packlight.data.db.dao.ResupplyDao
import com.stephenbrines.packlight.data.db.dao.TripDao
import com.stephenbrines.packlight.data.model.PackList
import com.stephenbrines.packlight.data.model.PackListItem
import com.stephenbrines.packlight.data.model.ResupplyPoint
import com.stephenbrines.packlight.data.model.Trip
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepository @Inject constructor(
    private val tripDao: TripDao,
    private val packListDao: PackListDao,
    private val resupplyDao: ResupplyDao,
) {
    fun getAll(): Flow<List<Trip>> = tripDao.getAll()
    suspend fun getById(id: String): Trip? = tripDao.getById(id)

    suspend fun createTrip(trip: Trip): String {
        tripDao.insert(trip)
        val packList = PackList(tripId = trip.id, name = "${trip.name} — Pack List")
        packListDao.insertPackList(packList)
        return trip.id
    }

    suspend fun update(trip: Trip) = tripDao.update(trip)
    suspend fun delete(trip: Trip) = tripDao.delete(trip)

    fun getPackLists(tripId: String) = packListDao.getPackListsForTrip(tripId)
    fun getPackListItems(packListId: String) = packListDao.getItemsForPackList(packListId)
    fun getGearForPackList(packListId: String) = packListDao.getGearForPackList(packListId)

    suspend fun addGearToPackList(packListId: String, gearItemId: String,
                                   quantity: Int = 1, isWorn: Boolean = false) {
        val existing = packListDao.findItem(packListId, gearItemId)
        if (existing != null) {
            packListDao.updatePackListItem(existing.copy(packedQuantity = existing.packedQuantity + quantity))
        } else {
            packListDao.insertPackListItem(
                PackListItem(packListId = packListId, gearItemId = gearItemId,
                             packedQuantity = quantity, isWorn = isWorn)
            )
        }
    }

    suspend fun updatePackListItem(item: PackListItem) = packListDao.updatePackListItem(item)
    suspend fun removePackListItem(item: PackListItem) = packListDao.deletePackListItem(item)

    fun getResupplyPoints(tripId: String) = resupplyDao.getPointsForTrip(tripId)
    fun getResupplyItems(pointId: String) = resupplyDao.getItemsForPoint(pointId)
    suspend fun insertResupplyPoint(point: ResupplyPoint) = resupplyDao.insertPoint(point)
    suspend fun updateResupplyPoint(point: ResupplyPoint) = resupplyDao.updatePoint(point)
    suspend fun deleteResupplyPoint(point: ResupplyPoint) = resupplyDao.deletePoint(point)
}
