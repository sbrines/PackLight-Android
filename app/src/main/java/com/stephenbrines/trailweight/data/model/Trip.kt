package com.stephenbrines.trailweight.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

enum class TerrainType(val displayName: String) {
    ALPINE("Alpine"),
    DESERT("Desert"),
    FOREST("Forest"),
    COASTAL("Coastal"),
    CANYON("Canyon"),
    TUNDRA("Tundra"),
    MIXED("Mixed");

    companion object {
        fun fromString(value: String) = entries.firstOrNull { it.name == value } ?: MIXED
    }
}

enum class TripStatus(val displayName: String) {
    PLANNING("Planning"),
    UPCOMING("Upcoming"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    companion object {
        fun fromString(value: String) = entries.firstOrNull { it.name == value } ?: PLANNING
    }
}

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val notes: String = "",
    val trailName: String = "",
    val startLocation: String = "",
    val endLocation: String = "",
    val startDateMs: Long? = null,
    val endDateMs: Long? = null,
    val distanceMiles: Double = 0.0,
    val maxElevationFeet: Int = 0,
    val minElevationFeet: Int = 0,
    val terrain: TerrainType = TerrainType.MIXED,
    val status: TripStatus = TripStatus.PLANNING,
    val createdAt: Long = System.currentTimeMillis(),
) {
    val durationDays: Int get() {
        val start = startDateMs ?: return 1
        val end = endDateMs ?: return 1
        return maxOf(1, TimeUnit.MILLISECONDS.toDays(end - start).toInt())
    }

    val formattedDateRange: String get() {
        val fmt = SimpleDateFormat("MMM d, yyyy", Locale.US)
        return when {
            startDateMs != null && endDateMs != null ->
                "${fmt.format(Date(startDateMs))} – ${fmt.format(Date(endDateMs))}"
            startDateMs != null -> "Starts ${fmt.format(Date(startDateMs))}"
            else -> "Dates TBD"
        }
    }
}
