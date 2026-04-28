package com.stephenbrines.trailweight.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.stephenbrines.trailweight.data.db.dao.GearItemDao
import com.stephenbrines.trailweight.data.db.dao.PackListDao
import com.stephenbrines.trailweight.data.db.dao.ResupplyDao
import com.stephenbrines.trailweight.data.db.dao.TripDao
import com.stephenbrines.trailweight.data.db.dao.WeightSnapshotDao
import com.stephenbrines.trailweight.data.model.GearItem
import com.stephenbrines.trailweight.data.model.PackList
import com.stephenbrines.trailweight.data.model.PackListItem
import com.stephenbrines.trailweight.data.model.ResupplyItem
import com.stephenbrines.trailweight.data.model.ResupplyPoint
import com.stephenbrines.trailweight.data.model.Trip
import com.stephenbrines.trailweight.data.model.WeightSnapshot

@Database(
    entities = [
        GearItem::class,
        Trip::class,
        PackList::class,
        PackListItem::class,
        ResupplyPoint::class,
        ResupplyItem::class,
        WeightSnapshot::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class TrailWeightDatabase : RoomDatabase() {
    abstract fun gearItemDao(): GearItemDao
    abstract fun tripDao(): TripDao
    abstract fun packListDao(): PackListDao
    abstract fun resupplyDao(): ResupplyDao
    abstract fun weightSnapshotDao(): WeightSnapshotDao
}
