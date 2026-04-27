package com.stephenbrines.packlight.data.repository

import com.stephenbrines.packlight.data.db.dao.GearItemDao
import com.stephenbrines.packlight.data.model.GearItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GearRepository @Inject constructor(private val dao: GearItemDao) {
    fun getAll(): Flow<List<GearItem>> = dao.getAll()
    suspend fun getById(id: String): GearItem? = dao.getById(id)
    suspend fun insert(item: GearItem) = dao.insert(item)
    suspend fun update(item: GearItem) = dao.update(item.copy(updatedAt = System.currentTimeMillis()))
    suspend fun delete(item: GearItem) = dao.delete(item)
    suspend fun deleteMany(ids: List<String>) = dao.deleteByIds(ids)
}
