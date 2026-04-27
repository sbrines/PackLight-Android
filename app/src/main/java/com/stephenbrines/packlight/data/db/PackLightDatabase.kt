package com.stephenbrines.packlight.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.stephenbrines.packlight.data.db.dao.GearItemDao
import com.stephenbrines.packlight.data.db.dao.PackListDao
import com.stephenbrines.packlight.data.db.dao.ResupplyDao
import com.stephenbrines.packlight.data.db.dao.TripDao
import com.stephenbrines.packlight.data.model.GearItem
import com.stephenbrines.packlight.data.model.PackList
import com.stephenbrines.packlight.data.model.PackListItem
import com.stephenbrines.packlight.data.model.ResupplyItem
import com.stephenbrines.packlight.data.model.ResupplyPoint
import com.stephenbrines.packlight.data.model.Trip

@Database(
    entities = [
        GearItem::class,
        Trip::class,
        PackList::class,
        PackListItem::class,
        ResupplyPoint::class,
        ResupplyItem::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class PackLightDatabase : RoomDatabase() {
    abstract fun gearItemDao(): GearItemDao
    abstract fun tripDao(): TripDao
    abstract fun packListDao(): PackListDao
    abstract fun resupplyDao(): ResupplyDao
}
