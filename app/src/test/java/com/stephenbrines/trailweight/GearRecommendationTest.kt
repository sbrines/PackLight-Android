package com.stephenbrines.trailweight

import com.stephenbrines.trailweight.data.model.TerrainType
import com.stephenbrines.trailweight.data.model.Trip
import com.stephenbrines.trailweight.service.GearRecommendationEngine
import com.stephenbrines.trailweight.service.RecommendationPriority
import com.stephenbrines.trailweight.service.TripConditions
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class GearRecommendationTest {

    private lateinit var engine: GearRecommendationEngine

    @Before
    fun setUp() {
        engine = GearRecommendationEngine()
    }

    @Test
    fun `required gear always present on any trip`() {
        val recs = engine.recommendations(summerForestConditions())
        val requiredNames = recs.filter { it.priority == RecommendationPriority.REQUIRED }.map { it.categoryName }
        assertTrue("Shelter required", requiredNames.contains("Shelter"))
        assertTrue("Water required", requiredNames.contains("Water"))
        assertTrue("Food required", requiredNames.contains("Food"))
        assertTrue("Navigation required", requiredNames.contains("Navigation"))
        assertTrue("First Aid required", requiredNames.contains("First Aid"))
    }

    @Test
    fun `high alpine elevation adds required clothing`() {
        val conditions = TripConditions(
            maxElevationFeet = 13_000,
            minElevationFeet = 9_000,
            startDateMs = summerDateMs(),
            durationDays = 3,
            terrain = TerrainType.ALPINE,
            distanceMiles = 20.0,
        )
        val recs = engine.recommendations(conditions)
        val clothingRec = recs.firstOrNull { it.categoryName == "Clothing" }
        assertNotNull("Clothing recommended for alpine", clothingRec)
        assertEquals(RecommendationPriority.REQUIRED, clothingRec!!.priority)
        assertTrue("Mentions altitude", clothingRec.reason.contains("10,000"))
    }

    @Test
    fun `desert terrain requires extra water`() {
        val conditions = TripConditions(
            maxElevationFeet = 3_000,
            minElevationFeet = 1_000,
            startDateMs = summerDateMs(),
            durationDays = 3,
            terrain = TerrainType.DESERT,
            distanceMiles = 30.0,
        )
        val recs = engine.recommendations(conditions)
        val waterRecs = recs.filter { it.categoryName == "Water" && it.priority == RecommendationPriority.REQUIRED }
        assertTrue("Multiple water recs for desert", waterRecs.any { it.reason.contains("Desert") })
    }

    @Test
    fun `winter season adds insulation as required`() {
        val conditions = TripConditions(
            maxElevationFeet = 5_000,
            minElevationFeet = 3_000,
            startDateMs = winterDateMs(),
            durationDays = 2,
            terrain = TerrainType.FOREST,
            distanceMiles = 15.0,
        )
        val recs = engine.recommendations(conditions)
        val clothing = recs.filter { it.categoryName == "Clothing" }
        assertTrue("Winter clothing required", clothing.any { it.priority == RecommendationPriority.REQUIRED })
    }

    @Test
    fun `extended trip over 3 days adds hygiene`() {
        val conditions = TripConditions(
            maxElevationFeet = 5_000,
            minElevationFeet = 3_000,
            startDateMs = summerDateMs(),
            durationDays = 5,
            terrain = TerrainType.FOREST,
            distanceMiles = 40.0,
        )
        val recs = engine.recommendations(conditions)
        assertTrue("Hygiene for long trip", recs.any { it.categoryName == "Hygiene" })
    }

    @Test
    fun `long distance over 50 miles adds footwear`() {
        val conditions = TripConditions(
            maxElevationFeet = 5_000,
            minElevationFeet = 3_000,
            startDateMs = summerDateMs(),
            durationDays = 7,
            terrain = TerrainType.FOREST,
            distanceMiles = 80.0,
        )
        val recs = engine.recommendations(conditions)
        assertTrue("Footwear for long distance", recs.any { it.categoryName == "Footwear" })
    }

    @Test
    fun `recommendations sorted by priority ascending`() {
        val recs = engine.recommendations(summerForestConditions())
        val priorities = recs.map { it.priority.ordinal }
        assertEquals("Should be sorted by priority", priorities.sorted(), priorities)
    }

    @Test
    fun `from trip convenience constructor works`() {
        val trip = Trip(
            name = "Test Trip",
            terrain = TerrainType.ALPINE,
            maxElevationFeet = 12_000,
            minElevationFeet = 8_000,
            distanceMiles = 25.0,
            startDateMs = summerDateMs(),
            endDateMs = summerDateMs() + (4 * 24 * 60 * 60 * 1000L),
        )
        val conditions = TripConditions.from(trip)
        val recs = engine.recommendations(conditions)
        assertFalse("Should produce recommendations", recs.isEmpty())
    }

    // Helpers

    private fun summerForestConditions() = TripConditions(
        maxElevationFeet = 5_000,
        minElevationFeet = 3_000,
        startDateMs = summerDateMs(),
        durationDays = 3,
        terrain = TerrainType.FOREST,
        distanceMiles = 20.0,
    )

    private fun summerDateMs(): Long {
        val cal = Calendar.getInstance()
        cal.set(2026, Calendar.JULY, 15)
        return cal.timeInMillis
    }

    private fun winterDateMs(): Long {
        val cal = Calendar.getInstance()
        cal.set(2026, Calendar.JANUARY, 15)
        return cal.timeInMillis
    }
}
