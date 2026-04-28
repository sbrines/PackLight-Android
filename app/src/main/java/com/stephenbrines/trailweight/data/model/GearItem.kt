package com.stephenbrines.trailweight.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "gear_items")
data class GearItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val brand: String = "",
    val category: GearCategory = GearCategory.OTHER,
    val weightGrams: Double = 0.0,
    val quantityOwned: Int = 1,
    val isConsumable: Boolean = false,
    val notes: String = "",
    val purchaseUrl: String = "",
    val imageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    val weightOunces: Double get() = weightGrams * 0.035274
    val weightPounds: Double get() = weightGrams * 0.00220462

    val displayWeight: String get() = when {
        weightGrams >= 1000 -> "%.2f kg".format(weightGrams / 1000)
        else -> "%.0f g".format(weightGrams)
    }
}
