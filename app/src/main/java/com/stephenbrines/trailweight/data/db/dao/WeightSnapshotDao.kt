package com.stephenbrines.trailweight.data.db.dao

import androidx.room.*
import com.stephenbrines.trailweight.data.model.WeightSnapshot
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightSnapshotDao {
    @Query("SELECT * FROM weight_snapshots ORDER BY recordedAt ASC")
    fun getAll(): Flow<List<WeightSnapshot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snapshot: WeightSnapshot)

    @Delete
    suspend fun delete(snapshot: WeightSnapshot)
}
