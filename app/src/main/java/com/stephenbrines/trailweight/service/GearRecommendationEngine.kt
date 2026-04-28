package com.stephenbrines.trailweight.service

import com.stephenbrines.trailweight.data.model.TerrainType
import com.stephenbrines.trailweight.data.model.Trip
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

enum class RecommendationPriority(val label: String) {
    REQUIRED("Required"),
    STRONGLY("Strongly Recommended"),
    SUGGESTED("Suggested"),
    OPTIONAL("Optional"),
}

data class GearRecommendation(
    val categoryName: String,
    val icon: String,
    val reason: String,
    val priority: RecommendationPriority,
)

data class TripConditions(
    val maxElevationFeet: Int,
    val minElevationFeet: Int,
    val startDateMs: Long?,
    val durationDays: Int,
    val terrain: TerrainType,
    val distanceMiles: Double,
) {
    val season: Season get() {
        if (startDateMs == null) return Season.SUMMER
        val cal = Calendar.getInstance().also { it.timeInMillis = startDateMs }
        return when (cal.get(Calendar.MONTH) + 1) {
            12, 1, 2 -> Season.WINTER
            3, 4, 5 -> Season.SPRING
            6, 7, 8 -> Season.SUMMER
            else -> Season.FALL
        }
    }
    val isHighAlpine: Boolean get() = maxElevationFeet > 10_000
    val isExtendedTrip: Boolean get() = durationDays > 3
    val isLongDistance: Boolean get() = distanceMiles > 50

    companion object {
        fun from(trip: Trip) = TripConditions(
            maxElevationFeet = trip.maxElevationFeet,
            minElevationFeet = trip.minElevationFeet,
            startDateMs = trip.startDateMs,
            durationDays = trip.durationDays,
            terrain = trip.terrain,
            distanceMiles = trip.distanceMiles,
        )
    }
}

enum class Season { WINTER, SPRING, SUMMER, FALL }

@Singleton
class GearRecommendationEngine @Inject constructor() {

    fun recommendations(conditions: TripConditions): List<GearRecommendation> {
        val recs = mutableListOf<GearRecommendation>()

        // Always required
        recs += listOf(
            GearRecommendation("Shelter", "home", "Essential protection from the elements.", RecommendationPriority.REQUIRED),
            GearRecommendation("Sleep System", "bed", "You need sleep to keep moving.", RecommendationPriority.REQUIRED),
            GearRecommendation("Water", "water_drop", "Filter and carry sufficient water.", RecommendationPriority.REQUIRED),
            GearRecommendation("Navigation", "map", "Map, compass, or GPS for your route.", RecommendationPriority.REQUIRED),
            GearRecommendation("First Aid", "medical_services", "Basic first aid for emergencies.", RecommendationPriority.REQUIRED),
            GearRecommendation("Food", "restaurant", "${conditions.durationDays} days of meals needed.", RecommendationPriority.REQUIRED),
        )

        // Elevation
        if (conditions.isHighAlpine) {
            recs += GearRecommendation("Clothing", "checkroom",
                "Above 10,000 ft: cold temps, wind, and afternoon thunderstorms. Bring insulation.",
                RecommendationPriority.REQUIRED)
            recs += GearRecommendation("Sun Protection", "wb_sunny",
                "High UV at altitude. Sunscreen, sunglasses, and sun hat essential.",
                RecommendationPriority.STRONGLY)
        }

        // Season
        when (conditions.season) {
            Season.WINTER -> {
                recs += GearRecommendation("Clothing", "checkroom",
                    "Winter requires insulated layers, waterproof shell, and warm accessories.",
                    RecommendationPriority.REQUIRED)
                recs += GearRecommendation("Tools & Repair", "build",
                    "Traction devices (microspikes/crampons) may be needed.",
                    RecommendationPriority.STRONGLY)
            }
            Season.SPRING -> recs += GearRecommendation("Clothing", "checkroom",
                "Spring weather is unpredictable — pack rain gear and a warm layer.",
                RecommendationPriority.STRONGLY)
            Season.SUMMER -> recs += GearRecommendation("Hygiene", "shower",
                "Summer heat: electrolytes, bug protection, and sun protection recommended.",
                RecommendationPriority.SUGGESTED)
            Season.FALL -> recs += GearRecommendation("Clothing", "checkroom",
                "Fall temps drop fast at night. Bring a warm sleep layer and puffy jacket.",
                RecommendationPriority.STRONGLY)
        }

        // Terrain
        when (conditions.terrain) {
            TerrainType.DESERT -> recs += GearRecommendation("Water", "water_drop",
                "Desert: carry extra water capacity (4L+). Sources may be scarce.",
                RecommendationPriority.REQUIRED)
            TerrainType.COASTAL -> recs += GearRecommendation("Clothing", "checkroom",
                "Coastal: damp, windy conditions. Wind shell and moisture-wicking layers.",
                RecommendationPriority.STRONGLY)
            TerrainType.ALPINE -> recs += GearRecommendation("Navigation", "map",
                "Alpine: trails may be faint or snow-covered. GPS and topo map essential.",
                RecommendationPriority.REQUIRED)
            else -> {}
        }

        // Trip length
        if (conditions.isExtendedTrip) {
            recs += GearRecommendation("Hygiene", "shower",
                "Multi-day: pack hygiene essentials including LNT waste kit.",
                RecommendationPriority.STRONGLY)
            recs += GearRecommendation("Electronics", "bolt",
                "Extended trip: consider a solar charger or extra battery pack.",
                RecommendationPriority.SUGGESTED)
        }
        if (conditions.isLongDistance) {
            recs += GearRecommendation("Footwear", "hiking",
                "Long distance: foot care critical. Gaiters, blister kit, and trail runners.",
                RecommendationPriority.STRONGLY)
        }

        return recs.sortedBy { it.priority.ordinal }
    }
}
