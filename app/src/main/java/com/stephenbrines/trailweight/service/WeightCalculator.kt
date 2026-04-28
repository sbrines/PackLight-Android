package com.stephenbrines.trailweight.service

import com.stephenbrines.trailweight.data.model.GearCategory
import com.stephenbrines.trailweight.data.model.PackListItemWithGear

data class CategoryWeight(
    val category: GearCategory,
    val weightGrams: Double,
    val itemCount: Int,
    val percentage: Double = 0.0,
)

data class WeightSummary(
    val baseWeightGrams: Double,
    val wornWeightGrams: Double,
    val consumableWeightGrams: Double,
    val totalWeightGrams: Double,
    val byCategory: List<CategoryWeight>,
) {
    val skinOutWeightGrams: Double get() = baseWeightGrams + wornWeightGrams

    val classification: String get() = when {
        baseWeightGrams < 2_270 -> "Super Ultralight (SUL)"
        baseWeightGrams < 4_540 -> "Ultralight (UL)"
        baseWeightGrams < 9_070 -> "Lightweight"
        else -> "Traditional"
    }

    companion object {
        val EMPTY = WeightSummary(0.0, 0.0, 0.0, 0.0, emptyList())
    }
}

object WeightCalculator {
    fun calculate(items: List<PackListItemWithGear>): WeightSummary {
        var base = 0.0; var worn = 0.0; var consumable = 0.0
        val categoryMap = mutableMapOf<GearCategory, Pair<Double, Int>>()

        for (entry in items) {
            val gear = entry.gear ?: continue
            val w = gear.weightGrams * entry.item.packedQuantity
            val cat = gear.category
            val (catW, catC) = categoryMap[cat] ?: (0.0 to 0)
            categoryMap[cat] = (catW + w) to (catC + entry.item.packedQuantity)

            when {
                entry.item.isWorn -> worn += w
                gear.isConsumable -> consumable += w
                else -> base += w
            }
        }

        val total = base + worn + consumable
        val cats = categoryMap.map { (cat, pair) ->
            CategoryWeight(
                category = cat,
                weightGrams = pair.first,
                itemCount = pair.second,
                percentage = if (total > 0) pair.first / total * 100 else 0.0,
            )
        }.sortedByDescending { it.weightGrams }

        return WeightSummary(base, worn, consumable, total, cats)
    }
}
