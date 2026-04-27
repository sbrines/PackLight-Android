package com.stephenbrines.packlight.data.db

import androidx.room.TypeConverter
import com.stephenbrines.packlight.data.model.GearCategory
import com.stephenbrines.packlight.data.model.TerrainType
import com.stephenbrines.packlight.data.model.TripStatus

class Converters {
    @TypeConverter fun fromGearCategory(v: GearCategory): String = v.name
    @TypeConverter fun toGearCategory(v: String): GearCategory = GearCategory.fromString(v)

    @TypeConverter fun fromTerrainType(v: TerrainType): String = v.name
    @TypeConverter fun toTerrainType(v: String): TerrainType = TerrainType.fromString(v)

    @TypeConverter fun fromTripStatus(v: TripStatus): String = v.name
    @TypeConverter fun toTripStatus(v: String): TripStatus = TripStatus.fromString(v)
}
