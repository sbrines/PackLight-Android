package com.stephenbrines.packlight

import com.stephenbrines.packlight.data.model.GearCategory
import com.stephenbrines.packlight.data.model.GearItem
import com.stephenbrines.packlight.data.model.PackListItem
import com.stephenbrines.packlight.data.model.PackListItemWithGear
import com.stephenbrines.packlight.service.WeightCalculator
import org.junit.Assert.*
import org.junit.Test

class WeightCalculatorTest {

    @Test fun `empty list returns zero summary`() {
        val summary = WeightCalculator.calculate(emptyList())
        assertEquals(0.0, summary.totalWeightGrams, 0.0)
        assertEquals(0.0, summary.baseWeightGrams, 0.0)
        assertTrue(summary.byCategory.isEmpty())
    }

    @Test fun `base excludes consumables and worn`() {
        val items = listOf(
            entry(weight = 1000.0, isConsumable = false, isWorn = false),  // base
            entry(weight = 500.0, isConsumable = true, isWorn = false),    // consumable
            entry(weight = 300.0, isConsumable = false, isWorn = true),    // worn
        )
        val s = WeightCalculator.calculate(items)
        assertEquals(1000.0, s.baseWeightGrams, 0.01)
        assertEquals(500.0, s.consumableWeightGrams, 0.01)
        assertEquals(300.0, s.wornWeightGrams, 0.01)
        assertEquals(1800.0, s.totalWeightGrams, 0.01)
    }

    @Test fun `quantity multiplies weight`() {
        val s = WeightCalculator.calculate(listOf(entry(weight = 50.0, quantity = 2)))
        assertEquals(100.0, s.baseWeightGrams, 0.01)
    }

    @Test fun `SUL classification below 2270g`() {
        val s = WeightCalculator.calculate(listOf(entry(weight = 2000.0)))
        assertTrue(s.classification.contains("Super Ultralight"))
    }

    @Test fun `UL classification between 2270g and 4540g`() {
        val s = WeightCalculator.calculate(listOf(entry(weight = 3000.0)))
        assertTrue(s.classification.contains("Ultralight"))
        assertFalse(s.classification.contains("Super"))
    }

    @Test fun `category breakdown is sorted heaviest first`() {
        val items = listOf(
            entry(weight = 500.0, category = GearCategory.SLEEP),
            entry(weight = 1000.0, category = GearCategory.SHELTER),
        )
        val s = WeightCalculator.calculate(items)
        assertEquals(GearCategory.SHELTER, s.byCategory.first().category)
    }

    @Test fun `percentages sum to 100`() {
        val items = listOf(
            entry(weight = 500.0, category = GearCategory.SLEEP),
            entry(weight = 500.0, category = GearCategory.SHELTER),
        )
        val s = WeightCalculator.calculate(items)
        val total = s.byCategory.sumOf { it.percentage }
        assertEquals(100.0, total, 0.1)
    }

    // Helpers
    private fun entry(
        weight: Double,
        isConsumable: Boolean = false,
        isWorn: Boolean = false,
        quantity: Int = 1,
        category: GearCategory = GearCategory.SHELTER,
    ): PackListItemWithGear {
        val gear = GearItem(name = "Test", category = category,
                            weightGrams = weight, isConsumable = isConsumable)
        val item = PackListItem(packListId = "pl1", gearItemId = gear.id,
                                packedQuantity = quantity, isWorn = isWorn)
        return PackListItemWithGear(item, gear)
    }
}
